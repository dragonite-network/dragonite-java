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

package com.vecsight.dragonite.proxy.network.server;

import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.mux.exception.SenderClosedException;
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.mux.AddressType;
import com.vecsight.dragonite.proxy.header.mux.ConnectionStatus;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionRequestHeader;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionResponseHeader;
import com.vecsight.dragonite.proxy.misc.Cryptor;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.CryptorPipe;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;

public class ProxyMuxHandler {

    private final Multiplexer multiplexer;

    private final String clientName;

    private final SocketAddress clientAddress;

    private final byte[] encryptionKey;

    public ProxyMuxHandler(final Multiplexer multiplexer, final String clientName, final SocketAddress clientAddress, final byte[] encryptionKey) {
        this.multiplexer = multiplexer;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.encryptionKey = encryptionKey;
    }

    public void run() throws MultiplexerClosedException, InterruptedException {
        MultiplexedConnection multiplexedConnection;
        while ((multiplexedConnection = multiplexer.acceptConnection()) != null) {
            final MultiplexedConnection finalMuxConn = multiplexedConnection;
            final Thread connHandleThread = new Thread(() -> {
                try {
                    handleMuxConnection(finalMuxConn);
                } catch (final InterruptedException ignored) {
                }
            }, "PS-MuxConnHandler");
            connHandleThread.start();
        }
    }

    private void handleMuxConnection(final MultiplexedConnection muxConn) throws InterruptedException {
        Logger.debug("New connection by client \"{}\" ({})",
                clientName, clientAddress.toString());

        final Cryptor cryptor;

        //Read IV & encrypted request header
        final byte[] encryptedRequestHeader;
        try {

            final byte[] ivBytes;
            ivBytes = muxConn.read();

            if (ivBytes.length != ProxyGlobalConstants.IV_LENGTH) {
                //Invalid IV length
                Logger.error("Invalid IV length from \"{}\" ({})", clientName, clientAddress.toString());
                muxConn.close();
                return;
            }

            cryptor = new Cryptor(encryptionKey, ivBytes);

            encryptedRequestHeader = muxConn.read();

        } catch (final ConnectionNotAliveException e) {
            Logger.error(e, "Unable to read request header from \"{}\" ({})",
                    clientName, clientAddress.toString());
            muxConn.close();
            return;
        } catch (final EncryptionException e) {
            Logger.error(e, "Failed to initialize cryptor for client \"{}\" ({})", clientName, clientAddress.toString());
            muxConn.close();
            return;
        }

        final MuxConnectionRequestHeader requestHeader;
        try {
            requestHeader = new MuxConnectionRequestHeader(cryptor.decrypt(encryptedRequestHeader));
        } catch (final IncorrectHeaderException e) {
            Logger.error(e, "Incorrect request header from \"{}\" ({})",
                    clientName, clientAddress.toString());
            muxConn.close();
            return;
        }

        //Header is okay, handle address stuff
        final InetAddress remoteAddress;
        try {
            if (requestHeader.getType() == AddressType.IPv4 || requestHeader.getType() == AddressType.IPv6) {
                remoteAddress = InetAddress.getByAddress(requestHeader.getAddr());
            } else {
                remoteAddress = InetAddress.getByName(new String(requestHeader.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            }
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host from request of \"{}\" ({})",
                    clientName, clientAddress.toString());
            try {
                //Send encrypted failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, "Unknown host").toBytes();
                muxConn.send(cryptor.encrypt(header));
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }
        //Let's connect then
        final InetSocketAddress socketAddress = new InetSocketAddress(remoteAddress, requestHeader.getPort());
        Logger.debug("Connecting {} for client \"{}\" ({})",
                socketAddress.toString(), clientName, clientAddress.toString());
        final Socket tcpSocket = new Socket();
        try {
            tcpSocket.connect(socketAddress, ProxyGlobalConstants.TCP_CONNECT_TIMEOUT_MS);
        } catch (final IOException e) {
            Logger.warn(e, "Unable to establish connection with {}", socketAddress.toString());
            try {
                //Send encrypted failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, e.getMessage()).toBytes();
                muxConn.send(cryptor.encrypt(header));
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }
        try {
            //Send encrypted OK response
            final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.OK, "Connected").toBytes();
            muxConn.send(cryptor.encrypt(header));
        } catch (final SenderClosedException e) {
            Logger.error(e, "Unable to send response to client \"{}\" ({})",
                    clientName, clientAddress.toString());
            muxConn.close();
            try {
                tcpSocket.close();
            } catch (final IOException ignored) {
            }
            return;
        }

        //Alright
        startPipe(muxConn, tcpSocket, cryptor);

    }

    private void startPipe(final MultiplexedConnection muxConn, final Socket tcpSocket, final Cryptor cryptor) {
        final Thread pipeFromRemoteThread = new Thread(() -> {
            final CryptorPipe pipeFromRemotePipe = new CryptorPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE, cryptor);
            try {
                pipeFromRemotePipe.pipe(muxConn, tcpSocket.getOutputStream());
            } catch (final Exception e) {
                Logger.debug(e, "Pipe closed");
            } finally {
                try {
                    tcpSocket.close();
                } catch (final IOException ignored) {
                }
                muxConn.close();
            }
        }, "PS-R2L");
        pipeFromRemoteThread.start();

        final Thread pipeFromLocalThread = new Thread(() -> {
            final CryptorPipe pipeFromLocalPipe = new CryptorPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE, cryptor);
            try {
                pipeFromLocalPipe.pipe(tcpSocket.getInputStream(), muxConn);
            } catch (final Exception e) {
                Logger.debug(e, "Pipe closed");
            } finally {
                try {
                    tcpSocket.close();
                } catch (final IOException ignored) {
                }
                muxConn.close();
            }
        }, "PS-L2R");
        pipeFromLocalThread.start();
    }

}
