package com.vecsight.dragonite.forwarder.network.client;

import com.vecsight.dragonite.forwarder.config.ForwarderClientConfig;
import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.exception.ServerRejectedException;
import com.vecsight.dragonite.forwarder.header.ClientInfoHeader;
import com.vecsight.dragonite.forwarder.header.ServerResponseHeader;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.network.Pipe;
import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.ConnectionAlreadyExistsException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteClientSocket;
import com.vecsight.dragonite.utils.network.UnitConverter;
import com.vecsight.dragonite.utils.system.SystemInfo;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferOverflowException;

public class ForwarderClient {

    private final InetSocketAddress remoteAddress;

    private final int localPort;

    private final short downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters;

    private volatile boolean doAccept = true;

    private final ServerSocket serverSocket;

    private volatile DragoniteClientSocket dragoniteClientSocket;

    private volatile Multiplexer multiplexer;

    private final Thread acceptThread;

    private volatile Thread muxReceiveThread;

    private short nextConnID = 0; //single-threaded internal

    private final Object reconnectLock = new Object();

    public ForwarderClient(final ForwarderClientConfig config) throws IOException, InterruptedException, DragoniteException, IncorrectHeaderException, ServerRejectedException {
        this.remoteAddress = config.getRemoteAddress();
        this.localPort = config.getLocalPort();
        this.downMbps = config.getDownMbps();
        this.upMbps = config.getUpMbps();
        this.dragoniteSocketParameters = config.getDragoniteSocketParameters();

        prepareUnderlyingConnection(dragoniteSocketParameters);

        serverSocket = new ServerSocket(localPort);

        acceptThread = new Thread(() -> {
            Socket socket;
            try {
                while (doAccept && (socket = serverSocket.accept()) != null) {
                    Logger.debug("New connection from {}", socket.getRemoteSocketAddress().toString());
                    handleClient(socket);
                }
            } catch (final IOException e) {
                Logger.error(e, "Unable to accept TCP connections");
            }
        }, "FC-Accept");
        acceptThread.start();
    }

    private void prepareUnderlyingConnection(final DragoniteSocketParameters dragoniteSocketParameters) throws IOException, InterruptedException, DragoniteException, IncorrectHeaderException, ServerRejectedException {
        dragoniteClientSocket = new DragoniteClientSocket(remoteAddress, UnitConverter.mbpsToSpeed(upMbps), dragoniteSocketParameters);

        dragoniteClientSocket.setDescription("Forwarder");

        try {
            dragoniteClientSocket.send(new ClientInfoHeader(downMbps, upMbps, SystemInfo.getUsername(), ForwarderGlobalConstants.APP_VERSION, SystemInfo.getOS()).toBytes());

            final byte[] response = dragoniteClientSocket.read();
            final ServerResponseHeader responseHeader = new ServerResponseHeader(response);

            if (responseHeader.getStatus() != 0) {
                Logger.error("The server has rejected this connection (Error code {}): {}", responseHeader.getStatus(), responseHeader.getMsg());
                throw new ServerRejectedException(responseHeader.getMsg());
            } else if (responseHeader.getMsg().length() > 0) {
                Logger.info("Server welcome message: {}", responseHeader.getMsg());
            }

        } catch (InterruptedException | IOException | DragoniteException | IncorrectHeaderException | ServerRejectedException | BufferOverflowException e) {
            Logger.error(e, "Unable to connect to remote server");
            try {
                dragoniteClientSocket.closeGracefully();
            } catch (InterruptedException | SenderClosedException | IOException ignored) {
            }
            throw e;
        }

        multiplexer = new Multiplexer(bytes -> {
            try {
                dragoniteClientSocket.send(bytes);
            } catch (InterruptedException | IncorrectSizeException | IOException | SenderClosedException e) {
                Logger.error(e, "Multiplexer is unable to send data");
            }
        }, ForwarderGlobalConstants.MAX_FRAME_SIZE);

        if (muxReceiveThread != null) muxReceiveThread.interrupt();

        muxReceiveThread = new Thread(() -> {
            byte[] buf;
            try {
                while ((buf = dragoniteClientSocket.read()) != null) {
                    multiplexer.onReceiveBytes(buf);
                }
            } catch (InterruptedException | ConnectionNotAliveException e) {
                Logger.error(e, "Cannot receive data from underlying socket");
            } finally {
                synchronized (reconnectLock) {
                    try {
                        dragoniteClientSocket.closeGracefully();
                    } catch (final Exception ignored) {
                    }
                    multiplexer.close();
                }
            }
        }, "FC-MuxReceive");
        muxReceiveThread.start();

        Logger.info("Connection established with {}", remoteAddress.toString());
    }

    private void handleClient(final Socket socket) {

        synchronized (reconnectLock) {
            if (!dragoniteClientSocket.isAlive()) {
                multiplexer.close();
                Logger.warn("The underlying connection is no longer alive, reconnecting");
                try {
                    prepareUnderlyingConnection(dragoniteSocketParameters);
                } catch (IOException | InterruptedException | DragoniteException | IncorrectHeaderException | ServerRejectedException e) {
                    Logger.error(e, "Unable to reconnect, there may be a network error or the server has been shut down");
                    return;
                }
            }
        }

        try {
            final MultiplexedConnection multiplexedConnection = multiplexer.createConnection(nextConnID++);

            final Thread pipeFromRemoteThread = new Thread(() -> {
                final Pipe pipeFromRemotePipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                try {
                    pipeFromRemotePipe.pipe(multiplexedConnection, socket.getOutputStream());
                } catch (final Exception e) {
                    Logger.debug(e, "Pipe closed");
                } finally {
                    try {
                        socket.close();
                    } catch (final IOException ignored) {
                    }
                    multiplexedConnection.close();
                }
            }, "FC-R2L");
            pipeFromRemoteThread.start();

            final Thread pipeFromLocalThread = new Thread(() -> {
                final Pipe pipeFromLocalPipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                try {
                    pipeFromLocalPipe.pipe(socket.getInputStream(), multiplexedConnection);
                } catch (final Exception e) {
                    Logger.debug(e, "Pipe closed");
                } finally {
                    try {
                        socket.close();
                    } catch (final IOException ignored) {
                    }
                    multiplexedConnection.close();
                }
            }, "FC-L2R");
            pipeFromLocalThread.start();

        } catch (ConnectionAlreadyExistsException | MultiplexerClosedException e) {
            Logger.error(e, "Cannot create multiplexed connection");
        }
    }

    public boolean isDoAccept() {
        return doAccept;
    }

    public void stopAccept() {
        acceptThread.interrupt();
        doAccept = false;
    }

}
