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
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.mux.AddressType;
import com.vecsight.dragonite.proxy.header.mux.ConnectionStatus;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionRequestHeader;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionResponseHeader;
import com.vecsight.dragonite.proxy.misc.Cryptor;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.CryptorPipe;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5Header;
import com.vecsight.dragonite.proxy.network.socks5.SOCKS5SocketHelper;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;

public class ProxyConnectionHandler {

    private final SOCKS5Header socks5Header;

    private final MultiplexedConnection multiplexedConnection;

    private final Socket socket;

    private final byte[] encryptionKey;

    public ProxyConnectionHandler(final SOCKS5Header socks5Header, final MultiplexedConnection multiplexedConnection, final Socket socket, final byte[] encryptionKey) {
        this.socks5Header = socks5Header;
        this.multiplexedConnection = multiplexedConnection;
        this.socket = socket;
        this.encryptionKey = encryptionKey;
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

        final Cryptor cryptor;

        try {
            //Send IV & encrypted request header
            final byte[] ivBytes = new byte[ProxyGlobalConstants.IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes);
            multiplexedConnection.send(ivBytes);

            cryptor = new Cryptor(encryptionKey, ivBytes);

            final byte[] headerRaw = new MuxConnectionRequestHeader(type, socks5Header.getAddr(), socks5Header.getPort()).toBytes();
            multiplexedConnection.send(cryptor.encrypt(headerRaw));

        } catch (final com.vecsight.dragonite.mux.exception.SenderClosedException | InterruptedException e) {
            Logger.error(e, "Cannot send request header");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            return;
        } catch (final EncryptionException e) {
            Logger.error(e, "Cryptor initialization failed");
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            multiplexedConnection.close();
            return;
        }

        //Read encrypted response
        final byte[] encryptedResponseBytes;
        try {
            encryptedResponseBytes = multiplexedConnection.read();
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
            responseHeader = new MuxConnectionResponseHeader(cryptor.decrypt(encryptedResponseBytes));
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
        try {
            if (responseHeader.getStatus() == ConnectionStatus.OK) {
                SOCKS5SocketHelper.sendSucceed(socket);
            } else if (responseHeader.getStatus() == ConnectionStatus.ERROR) {
                SOCKS5SocketHelper.sendFailed(socket);
                Logger.warn("Server connection error: {}", responseHeader.getMsg());
                try {
                    socket.close();
                } catch (final IOException ignored) {
                }
                multiplexedConnection.close();
                return;
            } else if (responseHeader.getStatus() == ConnectionStatus.REJECTED) {
                SOCKS5SocketHelper.sendRejected(socket);
                Logger.warn("Server rejected: {}", responseHeader.getMsg());
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
            return;
        }

        //Here we go
        startPipe(multiplexedConnection, socket, cryptor);

    }

    private void startPipe(final MultiplexedConnection multiplexedConnection, final Socket socket, final Cryptor cryptor) {
        final Thread pipeFromRemoteThread = new Thread(() -> {
            final CryptorPipe pipeFromRemotePipe = new CryptorPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE, cryptor);
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
            final CryptorPipe pipeFromLocalPipe = new CryptorPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE, cryptor);
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
