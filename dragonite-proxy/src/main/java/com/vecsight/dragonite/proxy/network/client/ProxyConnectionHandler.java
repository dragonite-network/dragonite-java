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
import com.vecsight.dragonite.proxy.acl.ParsedACL;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.proxy.header.mux.ConnectionStatus;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionRequestHeader;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionResponseHeader;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.MuxPipe;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5Header;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5SocketHelper;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ProxyConnectionHandler {

    private final SOCKS5Header socks5Header;

    private final MultiplexedConnection multiplexedConnection;

    private final InetAddress serverAddress;

    private final Socket socket;

    private final ParsedACL acl;

    private final PacketCryptor packetCryptor;

    public ProxyConnectionHandler(final SOCKS5Header socks5Header, final MultiplexedConnection multiplexedConnection,
                                  final InetAddress serverAddress, final Socket socket, final ParsedACL acl,
                                  final PacketCryptor packetCryptor) {
        this.socks5Header = socks5Header;
        this.multiplexedConnection = multiplexedConnection;
        this.serverAddress = serverAddress;
        this.socket = socket;
        this.acl = acl;
        this.packetCryptor = packetCryptor;
    }

    public void run() {

        final AddressType type;
        if (socks5Header.isDomain()) {
            type = AddressType.DOMAIN;
        } else if (socks5Header.getAddr().length == 4) {
            type = AddressType.IPv4;
        } else {
            type = AddressType.IPv6;
        }

        try {
            //Send request header
            final byte[] headerRaw = new MuxConnectionRequestHeader(type, socks5Header.getAddr(), socks5Header.getPort(),
                    socks5Header.isUdp()).toBytes();
            multiplexedConnection.send(headerRaw);

        } catch (final com.vecsight.dragonite.mux.exception.SenderClosedException | InterruptedException e) {
            Logger.error(e, "Cannot send request header");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            return;
        }

        //Read response
        final byte[] responseBytes;
        try {
            responseBytes = multiplexedConnection.read();
        } catch (com.vecsight.dragonite.mux.exception.ConnectionNotAliveException | InterruptedException e) {
            Logger.error(e, "Unable to read response header");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            return;
        }

        final MuxConnectionResponseHeader responseHeader;
        try {
            responseHeader = new MuxConnectionResponseHeader(responseBytes);
        } catch (final IncorrectHeaderException e) {
            Logger.error(e, "Incorrect response header from server");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            return;
        }

        //Check status, respond to SOCKS5 client
        ProxyClientUDPRelay clientUDPRelay = null;

        try {
            if (responseHeader.getStatus() == ConnectionStatus.OK) {

                if (socks5Header.isUdp()) {
                    try {
                        clientUDPRelay = new ProxyClientUDPRelay(socket.getRemoteSocketAddress(),
                                new InetSocketAddress(serverAddress, responseHeader.getUdpPort()), packetCryptor, acl);
                    } catch (final SocketException e) {
                        Logger.error(e, "Failed to initialize local UDP relay");
                        SOCKS5SocketHelper.sendFailed(socket);
                        try {
                            socket.close();
                        } catch (final IOException ignored) {
                        }
                        multiplexedConnection.close();
                        return;
                    }
                    SOCKS5SocketHelper.sendSucceedUDP(socket, clientUDPRelay.getLocalPort());

                } else {
                    SOCKS5SocketHelper.sendSucceed(socket);
                }
            } else if (responseHeader.getStatus() == ConnectionStatus.ERROR) {
                Logger.warn("Server connection error: {}", responseHeader.getMsg());
                SOCKS5SocketHelper.sendFailed(socket);
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                multiplexedConnection.close();
                return;
            } else if (responseHeader.getStatus() == ConnectionStatus.REJECTED) {
                Logger.warn("Server rejected: {}", responseHeader.getMsg());
                SOCKS5SocketHelper.sendRejected(socket);
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                multiplexedConnection.close();
                return;
            }
        } catch (final IOException e) {
            Logger.error(e, "Failed to send SOCKS5 response");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            if (clientUDPRelay != null) {
                clientUDPRelay.stop();
            }
            return;
        }

        //Here we go
        if (socks5Header.isUdp()) {
            try {
                while (socket.getInputStream().read() != -1) {
                    //We do literally nothing :(
                }
            } catch (final IOException e) {
                Logger.debug(e, "UDP relay closed");
            } finally {
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                multiplexedConnection.close();
                if (clientUDPRelay != null) {
                    clientUDPRelay.stop();
                }
            }
        } else {
            startPipe(multiplexedConnection, socket);
        }

    }

    private void startPipe(final MultiplexedConnection multiplexedConnection, final Socket socket) {
        final Thread pipeFromRemoteThread = new Thread(() -> {
            final MuxPipe pipeFromRemotePipe = new MuxPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
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
        }, "PC-R2L");
        pipeFromRemoteThread.start();

        final Thread pipeFromLocalThread = new Thread(() -> {
            final MuxPipe pipeFromLocalPipe = new MuxPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
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
        }, "PC-L2R");
        pipeFromLocalThread.start();
    }

}
