package com.vecsight.dragonite.forwarder.network.client;

import com.vecsight.dragonite.forwarder.config.ForwarderClientConfig;
import com.vecsight.dragonite.forwarder.header.ClientInfoHeader;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.misc.UnitConverter;
import com.vecsight.dragonite.forwarder.network.Pipe;
import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.ConnectionAlreadyExistsException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteClientSocket;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ForwarderClient {

    private final InetSocketAddress remoteAddress;

    private final int localPort;

    private final short downMbps, upMbps;

    private volatile boolean doAccept = true;

    private final ServerSocket serverSocket;

    private final DragoniteClientSocket dragoniteClientSocket;

    private final Multiplexer multiplexer;

    private final Thread acceptThread;

    private final Thread muxReceiveThread;

    private short nextConnID = 0; //single-threaded internal

    public ForwarderClient(final ForwarderClientConfig config) throws IOException, InterruptedException, IncorrectSizeException, SenderClosedException {
        this.remoteAddress = config.getRemoteAddress();
        this.localPort = config.getLocalPort();
        this.downMbps = config.getDownMbps();
        this.upMbps = config.getUpMbps();

        this.dragoniteClientSocket = new DragoniteClientSocket(remoteAddress, UnitConverter.mbpsToSpeed(upMbps), config.getDragoniteSocketParameters());

        try {
            dragoniteClientSocket.send(new ClientInfoHeader(downMbps, upMbps, getUsername(), ForwarderGlobalConstants.APP_VERSION, getOS()).toBytes());
        } catch (InterruptedException | IncorrectSizeException | SenderClosedException e) {
            Logger.error(e, "Unable to send client header");
            try {
                dragoniteClientSocket.closeGracefully();
            } catch (InterruptedException | SenderClosedException ignored) {
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

        muxReceiveThread = new Thread(() -> {
            byte[] buf;
            try {
                while ((buf = dragoniteClientSocket.read()) != null) {
                    multiplexer.onReceiveBytes(buf);
                }
            } catch (InterruptedException | ConnectionNotAliveException e) {
                Logger.debug(e, "Cannot receive data from underlying socket");
            } finally {
                try {
                    dragoniteClientSocket.closeGracefully();
                } catch (Exception ignored) {
                }
                multiplexer.close();
            }
        }, "FC-MuxReceive");
        muxReceiveThread.start();

        serverSocket = new ServerSocket(localPort);

        acceptThread = new Thread(() -> {
            Socket socket;
            try {
                while (doAccept && (socket = serverSocket.accept()) != null) {
                    Logger.debug("New connection from {}", socket.getRemoteSocketAddress().toString());
                    handleClient(socket);
                }
            } catch (IOException e) {
                Logger.error(e, "Unable to accept TCP connections");
            }
        }, "FC-Accept");
        acceptThread.start();

        /*new Thread(() -> {
            while (dragoniteClientSocket.isAlive()) {
                System.out.println("RAW: " + dragoniteClientSocket.getStatistics().getReceiveRawLength() + ", READ: " + dragoniteClientSocket.getStatistics().getReadLength());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();*/
    }

    private String getUsername() {
        String name = System.getProperty("user.name");
        return name != null ? name : "Unknown";
    }

    private String getOS() {
        String os = System.getProperty("os.name");
        return os != null ? os : "Unknown";
    }

    private void handleClient(Socket socket) {
        try {
            MultiplexedConnection multiplexedConnection = multiplexer.createConnection(nextConnID++);

            final Thread pipeFromRemoteThread = new Thread(() -> {
                final Pipe pipeFromRemotePipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                try {
                    pipeFromRemotePipe.pipe(multiplexedConnection, socket.getOutputStream());
                } catch (Exception e) {
                    Logger.debug(e, "Pipe closed");
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    multiplexedConnection.close();
                }
            }, "FC-R2L");
            pipeFromRemoteThread.start();

            final Thread pipeFromLocalThread = new Thread(() -> {
                final Pipe pipeFromLocalPipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                try {
                    pipeFromLocalPipe.pipe(socket.getInputStream(), multiplexedConnection);
                } catch (Exception e) {
                    Logger.debug(e, "Pipe closed");
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
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
