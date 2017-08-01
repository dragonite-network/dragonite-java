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

package com.vecsight.dragonite.forwarder.network.server;

import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.network.Pipe;
import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ForwarderMuxHandler {

    private final Multiplexer multiplexer;

    private final String clientName;

    private final SocketAddress clientAddress;

    private final int port;

    public ForwarderMuxHandler(final Multiplexer multiplexer, final String clientName, final SocketAddress clientAddress, final int port) {
        this.multiplexer = multiplexer;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.port = port;
    }

    public void run() throws MultiplexerClosedException, InterruptedException {
        MultiplexedConnection multiplexedConnection;

        while ((multiplexedConnection = multiplexer.acceptConnection()) != null) {
            final MultiplexedConnection finalMuxConn = multiplexedConnection;

            Logger.debug("New connection by client \"{}\" ({})",
                    clientName, clientAddress.toString());
            try {
                final Socket tcpSocket = new Socket(InetAddress.getLoopbackAddress(), port);

                final Thread pipeFromRemoteThread = new Thread(() -> {
                    final Pipe pipeFromRemotePipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                    try {
                        pipeFromRemotePipe.pipe(finalMuxConn, tcpSocket.getOutputStream());
                    } catch (final Exception e) {
                        Logger.debug(e, "Pipe closed");
                    } finally {
                        try {
                            tcpSocket.close();
                        } catch (final IOException ignored) {
                        }
                        finalMuxConn.close();
                    }
                }, "FS-R2L");
                pipeFromRemoteThread.start();

                final Thread pipeFromLocalThread = new Thread(() -> {
                    final Pipe pipeFromLocalPipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                    try {
                        pipeFromLocalPipe.pipe(tcpSocket.getInputStream(), finalMuxConn);
                    } catch (final Exception e) {
                        Logger.debug(e, "Pipe closed");
                    } finally {
                        try {
                            tcpSocket.close();
                        } catch (final IOException ignored) {
                        }
                        finalMuxConn.close();
                    }
                }, "FS-L2R");
                pipeFromLocalThread.start();

            } catch (final IOException e) {
                Logger.error(e, "Unable to establish local connection");
                finalMuxConn.close();
            }

        }

    }
}