/*
 * VECTORSIGHT CONFIDENTIAL
 * ------------------------
 * Copyright (c) [2015] - [2017]
 * VectorSight Systems Co., Ltd.
 * All Rights Reserved.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Toby Huang <t@vecsight.com>, June 2017
 */

package com.vecsight.dragonite.proxy.network.client;

import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.ConnectionAlreadyExistsException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ParsedACL;
import com.vecsight.dragonite.proxy.config.ProxyClientConfig;
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.exception.SOCKS5Exception;
import com.vecsight.dragonite.proxy.exception.ServerRejectedException;
import com.vecsight.dragonite.proxy.header.ClientInfoHeader;
import com.vecsight.dragonite.proxy.header.ServerResponseHeader;
import com.vecsight.dragonite.proxy.misc.EncryptionKeyGenerator;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.StreamPipe;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5Header;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5SocketHelper;
import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteClientSocket;
import com.vecsight.dragonite.utils.system.SystemInfo;
import com.vecsight.dragonite.utils.type.UnitConverter;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;

public class ProxyClient {

    private final InetSocketAddress remoteAddress;

    private final int socks5port;

    private final String password;

    private final byte[] encryptionKey;

    private final int downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters;

    private volatile boolean doAccept = true;

    private final ServerSocket serverSocket;

    private volatile DragoniteClientSocket dragoniteClientSocket;

    private volatile Multiplexer multiplexer;

    private final Thread acceptThread;

    private volatile Thread muxReceiveThread;

    private short nextConnID = 0; //single-threaded internal

    private final Object connectLock = new Object();

    private final ParsedACL acl;

    public ProxyClient(final ProxyClientConfig config) throws EncryptionException, IOException, InterruptedException, DragoniteException, ServerRejectedException, IncorrectHeaderException {
        this.remoteAddress = config.getRemoteAddress();
        this.socks5port = config.getSocks5port();
        this.password = config.getPassword();
        this.downMbps = config.getDownMbps();
        this.upMbps = config.getUpMbps();
        this.acl = config.getAcl();
        this.dragoniteSocketParameters = config.getDragoniteSocketParameters();

        if (acl != null) {
            Logger.info("ACL loaded: {} by {}", acl.getTitle(), acl.getAuthor());
        }

        this.encryptionKey = EncryptionKeyGenerator.getKey(password);

        serverSocket = new ServerSocket(socks5port);

        prepareUnderlyingConnection(dragoniteSocketParameters);

        acceptThread = new Thread(() -> {
            Socket socket;
            try {
                while (doAccept && (socket = serverSocket.accept()) != null) {
                    final Socket finalSocket = socket;
                    final Thread socketHandleThread = new Thread(() -> handleConnection(finalSocket), "PC-ConnHandler");
                    socketHandleThread.start();
                }
            } catch (final IOException e) {
                Logger.error(e, "Unable to accept TCP connections");
            }
        }, "PC-Accept");
        acceptThread.start();
    }

    private void prepareUnderlyingConnection(final DragoniteSocketParameters dragoniteSocketParameters) throws IOException, InterruptedException, DragoniteException, ServerRejectedException, IncorrectHeaderException {
        dragoniteClientSocket = new DragoniteClientSocket(remoteAddress, UnitConverter.mbpsToSpeed(upMbps), dragoniteSocketParameters);
        dragoniteClientSocket.setDescription("Proxy");

        try {
            //Send info header
            final byte[] infoHeaderBytes = new ClientInfoHeader(downMbps, upMbps, SystemInfo.getUsername(), ProxyGlobalConstants.APP_VERSION, SystemInfo.getOS()).toBytes();
            dragoniteClientSocket.send(infoHeaderBytes);

            //Receive response
            final byte[] response = dragoniteClientSocket.read();
            final ServerResponseHeader responseHeader = new ServerResponseHeader(response);

            //Check response
            if (responseHeader.getStatus() != 0) {
                Logger.error("The server has rejected this connection (Error code {}): {}", responseHeader.getStatus(), responseHeader.getMsg());
                throw new ServerRejectedException(responseHeader.getMsg());
            } else if (responseHeader.getMsg().length() > 0) {
                Logger.info("Server welcome message: {}", responseHeader.getMsg());
            }

        } catch (final InterruptedException | IOException | DragoniteException | IncorrectHeaderException | ServerRejectedException e) {
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
        }, ProxyGlobalConstants.MAX_FRAME_SIZE);

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
                synchronized (connectLock) {
                    try {
                        dragoniteClientSocket.closeGracefully();
                    } catch (final Exception ignored) {
                    }
                    multiplexer.close();
                }
            }
        }, "PC-MuxReceive");
        muxReceiveThread.start();

        Logger.info("Connection established with {}", remoteAddress.toString());
    }

    private void handleConnection(final Socket socket) {

        Logger.debug("New connection from {}", socket.getRemoteSocketAddress().toString());

        //Parse header
        final SOCKS5Header socks5Header;
        try {
            socks5Header = SOCKS5SocketHelper.handleHeader(socket);
        } catch (final IOException | SOCKS5Exception e) {
            Logger.error(e, "Failed to parse SOCKS5 request");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            return;
        }

        Logger.debug("Parsed SOCKS5 request: {}", socks5Header.toString());

        //Check ACL
        final ACLItemMethod method;
        if (acl != null) {
            if (socks5Header.isDomain()) {
                method = acl.checkDomain(new String(socks5Header.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            } else {
                method = acl.checkIP(socks5Header.getAddr());
            }
        } else {
            method = ACLItemMethod.PROXY;
        }

        //Connect
        if (method == ACLItemMethod.PROXY) {

            final MultiplexedConnection multiplexedConnection;

            //Check reconnect & create mux connection
            synchronized (connectLock) {
                if (!dragoniteClientSocket.isAlive()) {
                    multiplexer.close();
                    Logger.warn("The underlying connection is no longer alive, reconnecting");
                    try {
                        prepareUnderlyingConnection(dragoniteSocketParameters);
                    } catch (IOException | InterruptedException | DragoniteException | IncorrectHeaderException | ServerRejectedException e) {
                        Logger.error(e, "Unable to reconnect, there may be a network error or the server has been shut down");
                        try {
                            socket.close();
                        } catch (final IOException ignored) {
                        }
                        return;
                    }
                }

                try {
                    multiplexedConnection = multiplexer.createConnection(nextConnID++);
                } catch (ConnectionAlreadyExistsException | MultiplexerClosedException e) {
                    Logger.error(e, "Cannot create multiplexed connection");
                    try {
                        socket.close();
                    } catch (final IOException ignored) {
                    }
                    return;
                }
            }

            final ProxyConnectionHandler handler = new ProxyConnectionHandler(socks5Header, multiplexedConnection, socket, encryptionKey);

            handler.run();

        } else if (method == ACLItemMethod.DIRECT) {
            //DIRECT local
            handleDirect(socks5Header, socket);
        } else {
            try {
                SOCKS5SocketHelper.sendRejected(socket);
            } catch (final IOException ignored) {
            }
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
        }
    }

    private void handleDirect(final SOCKS5Header socks5Header, final Socket socket) {
        final InetAddress remoteAddress;
        try {
            if (socks5Header.isDomain()) {
                remoteAddress = InetAddress.getByName(new String(socks5Header.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            } else {
                remoteAddress = InetAddress.getByAddress(socks5Header.getAddr());
            }
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host");
            try {
                SOCKS5SocketHelper.sendFailed(socket);
            } catch (final IOException ignored) {
            }
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            return;
        }
        //Let's connect then
        final InetSocketAddress socketAddress = new InetSocketAddress(remoteAddress, socks5Header.getPort());
        Logger.debug("Connecting {}", socketAddress.toString());
        final Socket remoteSocket = new Socket();
        try {
            remoteSocket.connect(socketAddress, ProxyGlobalConstants.TCP_CONNECT_TIMEOUT_MS);
        } catch (final IOException e) {
            Logger.warn(e, "Unable to establish connection with {}", socketAddress.toString());
            try {
                SOCKS5SocketHelper.sendFailed(socket);
            } catch (final IOException ignored) {
            }
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            return;
        }
        //Send OK to SOCKS5 client
        try {
            SOCKS5SocketHelper.sendSucceed(socket);
        } catch (final IOException e) {
            Logger.error(e, "Unable to send response to SOCKS5 client");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            return;
        }

        startPipe(socket, remoteSocket);
    }

    private void startPipe(final Socket socket, final Socket remoteSocket) {
        final Thread pipeFromRemoteThread = new Thread(() -> {
            final StreamPipe pipeFromRemotePipe = new StreamPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
            try {
                pipeFromRemotePipe.pipe(remoteSocket.getInputStream(), socket.getOutputStream());
            } catch (final Exception e) {
                Logger.debug(e, "Pipe closed");
            } finally {
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                try {
                    remoteSocket.close();
                } catch (final IOException ignored) {
                }
            }
        }, "PC-DIRECT-R2L");
        pipeFromRemoteThread.start();

        final Thread pipeFromLocalThread = new Thread(() -> {
            final StreamPipe pipeFromLocalPipe = new StreamPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
            try {
                pipeFromLocalPipe.pipe(socket.getInputStream(), remoteSocket.getOutputStream());
            } catch (final Exception e) {
                Logger.debug(e, "Pipe closed");
            } finally {
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                try {
                    remoteSocket.close();
                } catch (final IOException ignored) {
                }
            }
        }, "PC-DIRECT-L2R");
        pipeFromLocalThread.start();
    }

    public boolean isDoAccept() {
        return doAccept;
    }

    public void stopAccept() {
        acceptThread.interrupt();
        doAccept = false;
    }

}
