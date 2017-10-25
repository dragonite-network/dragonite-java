/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.network.server;

import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.network.Pipe;
import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ForwarderMuxHandler {

    private final Multiplexer multiplexer;

    private final String clientName;

    private final SocketAddress clientAddress;

    private final InetSocketAddress forwardingAddress;

    public ForwarderMuxHandler(final Multiplexer multiplexer, final String clientName, final SocketAddress clientAddress, final InetSocketAddress forwardingAddress) {
        this.multiplexer = multiplexer;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.forwardingAddress = forwardingAddress;
    }

    public void run() throws MultiplexerClosedException, InterruptedException {
        MultiplexedConnection multiplexedConnection;

        while ((multiplexedConnection = multiplexer.acceptConnection()) != null) {
            final MultiplexedConnection finalMuxConn = multiplexedConnection;

            Logger.debug("New connection by client \"{}\" ({})",
                    clientName, clientAddress.toString());
            try {
                final Socket tcpSocket = new Socket(forwardingAddress.getAddress(), forwardingAddress.getPort());

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