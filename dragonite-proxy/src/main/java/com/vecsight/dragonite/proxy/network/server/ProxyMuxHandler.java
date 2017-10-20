/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.network.server;

import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.mux.exception.SenderClosedException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.proxy.header.mux.ConnectionStatus;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionRequestHeader;
import com.vecsight.dragonite.proxy.header.mux.MuxConnectionResponseHeader;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.MuxPipe;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;

public class ProxyMuxHandler {

    private final Multiplexer multiplexer;

    private final String clientName;

    private final SocketAddress clientAddress;

    private final boolean allowLoopback;

    private final PacketCryptor packetCryptor;

    public ProxyMuxHandler(final Multiplexer multiplexer, final String clientName, final SocketAddress clientAddress,
                           final boolean allowLoopback, final PacketCryptor packetCryptor) {
        this.multiplexer = multiplexer;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.allowLoopback = allowLoopback;
        this.packetCryptor = packetCryptor;
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

        //Read request header
        final byte[] rawRequestHeader;
        try {
            rawRequestHeader = muxConn.read();
        } catch (final ConnectionNotAliveException e) {
            Logger.error(e, "Unable to read request header from \"{}\" ({})",
                    clientName, clientAddress.toString());
            muxConn.close();
            return;
        }

        final MuxConnectionRequestHeader requestHeader;
        try {
            requestHeader = new MuxConnectionRequestHeader(rawRequestHeader);
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
                //Send failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, 0, "Unknown host").toBytes();
                muxConn.send(header);
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }

        final InetSocketAddress socketAddress = new InetSocketAddress(remoteAddress, requestHeader.getPort());

        //Let's do connect then
        if (!requestHeader.isUdpMode()) {
            handleTCP(socketAddress, muxConn);
        } else {
            handleUDP(muxConn);
        }
    }

    private void handleTCP(final InetSocketAddress socketAddress, final MultiplexedConnection muxConn) throws InterruptedException {
        //Check for loopback
        if (!allowLoopback && socketAddress.getAddress().isLoopbackAddress()) {
            Logger.debug("Blocking client \"{}\" ({}) from accessing the loopback interface",
                    clientName, clientAddress.toString());
            try {
                //Send failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, 0, "Connection prohibited").toBytes();
                muxConn.send(header);
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }

        Logger.debug("Connecting {} for client \"{}\" ({})",
                socketAddress.toString(), clientName, clientAddress.toString());
        final Socket tcpSocket = new Socket();
        try {
            tcpSocket.connect(socketAddress, ProxyGlobalConstants.TCP_CONNECT_TIMEOUT_MS);
        } catch (final IOException e) {
            Logger.warn(e, "Unable to establish connection with {}", socketAddress.toString());
            try {
                //Send failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, 0, e.getMessage()).toBytes();
                muxConn.send(header);
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }
        try {
            //Send OK response
            final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.OK, 0, "Connected").toBytes();
            muxConn.send(header);
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
        startPipe(muxConn, tcpSocket);
    }

    private void handleUDP(final MultiplexedConnection muxConn) throws InterruptedException {
        Logger.debug("UDP relay request from client \"{}\" ({})",
                clientName, this.clientAddress.toString());
        final ProxyServerUDPRelay udpRelay;
        try {
            udpRelay = new ProxyServerUDPRelay(clientName, clientAddress, packetCryptor, allowLoopback);
        } catch (final SocketException e) {
            Logger.error(e, "Unable to initialize UDP relay for client \"{}\" ({})",
                    clientName, this.clientAddress.toString());
            try {
                //Send failed response
                final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.ERROR, 0, e.getMessage()).toBytes();
                muxConn.send(header);
            } catch (final SenderClosedException ignored) {
            }
            muxConn.close();
            return;
        }
        try {
            //Send OK response
            final byte[] header = new MuxConnectionResponseHeader(ConnectionStatus.OK, udpRelay.getLocalPort(), "Relay enabled").toBytes();
            muxConn.send(header);
        } catch (final SenderClosedException e) {
            Logger.error(e, "Unable to send response to client \"{}\" ({})",
                    clientName, this.clientAddress.toString());
            muxConn.close();
            udpRelay.stop();
            return;
        }

        try {
            while (muxConn.read() != null) {
                //We do literally nothing :(
            }
        } catch (final ConnectionNotAliveException e) {
            muxConn.close();
            udpRelay.stop();
        }
    }

    private void startPipe(final MultiplexedConnection muxConn, final Socket tcpSocket) {
        final Thread pipeFromRemoteThread = new Thread(() -> {
            final MuxPipe pipeFromRemotePipe = new MuxPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
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
            final MuxPipe pipeFromLocalPipe = new MuxPipe(ProxyGlobalConstants.PIPE_BUFFER_SIZE);
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
