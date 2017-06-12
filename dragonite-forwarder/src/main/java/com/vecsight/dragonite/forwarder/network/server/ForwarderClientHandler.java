package com.vecsight.dragonite.forwarder.network.server;

import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.header.ClientInfoHeader;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.misc.UnitConverter;
import com.vecsight.dragonite.forwarder.network.Pipe;
import com.vecsight.dragonite.mux.conn.MultiplexedConnection;
import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.BufferUnderflowException;

public class ForwarderClientHandler {

    private final int forwardingPort;

    private final DragoniteSocket dragoniteSocket;

    private final short limitMbps;

    public ForwarderClientHandler(int forwardingPort, DragoniteSocket dragoniteSocket, short limitMbps) {
        this.forwardingPort = forwardingPort;
        this.dragoniteSocket = dragoniteSocket;
        this.limitMbps = limitMbps;
    }

    public void run() {
        if (dragoniteSocket.isAlive()) {
            byte[] headerBytes;
            try {
                headerBytes = dragoniteSocket.read();
            } catch (InterruptedException | ConnectionNotAliveException e) {
                Logger.error(e, "Unable to read header from client {}", dragoniteSocket.getRemoteSocketAddress().toString());

                dragoniteSocket.destroy();
                return;
            }
            if (headerBytes != null) {
                final ClientInfoHeader infoHeader;

                try {
                    infoHeader = new ClientInfoHeader(headerBytes);
                } catch (IncorrectHeaderException | BufferUnderflowException e) {

                    Logger.error(e, "Incorrect header from client {}", dragoniteSocket.getRemoteSocketAddress().toString());

                    try {
                        dragoniteSocket.closeGracefully();
                    } catch (InterruptedException | IOException | SenderClosedException ignored) {
                        //okay
                    }
                    return;
                }

                Logger.info("Remote ({}) \"{}\" using client {} on {} (DL: {}, UL: {}) connected",
                        dragoniteSocket.getRemoteSocketAddress().toString(),
                        infoHeader.getName(), infoHeader.getAppVer(), infoHeader.getOsName(),
                        infoHeader.getDownMbps(), infoHeader.getUpMbps());

                dragoniteSocket.setDescription(infoHeader.getName());

                short realMbps = infoHeader.getDownMbps();
                if (realMbps > limitMbps && limitMbps > 0) {
                    realMbps = limitMbps;
                    Logger.info("The DL Mbps of client \"{}\" has been limited from {} to {}",
                            infoHeader.getName(), infoHeader.getDownMbps(), realMbps);
                }

                dragoniteSocket.setSendSpeed(UnitConverter.mbpsToSpeed(realMbps));

                final Multiplexer multiplexer = new Multiplexer(bytes -> {
                    try {
                        dragoniteSocket.send(bytes);
                    } catch (InterruptedException | IncorrectSizeException | SenderClosedException | IOException e) {
                        Logger.error(e, "Multiplexer is unable to send data");
                    }
                }, ForwarderGlobalConstants.MAX_FRAME_SIZE);

                final Thread multiplexerAcceptThread = new Thread(() -> {
                    try {
                        MultiplexedConnection multiplexedConnection;

                        while ((multiplexedConnection = multiplexer.acceptConnection()) != null) {
                            final MultiplexedConnection tmpMuxConn = multiplexedConnection;

                            Logger.debug("New connection by client \"{}\" ({})",
                                    infoHeader.getName(), dragoniteSocket.getRemoteSocketAddress().toString());
                            try {
                                final Socket tcpSocket = new Socket(InetAddress.getLoopbackAddress(), forwardingPort);

                                final Thread pipeFromRemoteThread = new Thread(() -> {
                                    final Pipe pipeFromRemotePipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                                    try {
                                        pipeFromRemotePipe.pipe(tmpMuxConn, tcpSocket.getOutputStream());
                                    } catch (Exception e) {
                                        Logger.debug(e, "Pipe closed");
                                    } finally {
                                        try {
                                            tcpSocket.close();
                                        } catch (IOException ignored) {
                                        }
                                        tmpMuxConn.close();
                                    }
                                }, "FS-R2L");
                                pipeFromRemoteThread.start();

                                final Thread pipeFromLocalThread = new Thread(() -> {
                                    final Pipe pipeFromLocalPipe = new Pipe(ForwarderGlobalConstants.PIPE_BUFFER_SIZE);
                                    try {
                                        pipeFromLocalPipe.pipe(tcpSocket.getInputStream(), tmpMuxConn);
                                    } catch (Exception e) {
                                        Logger.debug(e, "Pipe closed");
                                    } finally {
                                        try {
                                            tcpSocket.close();
                                        } catch (IOException ignored) {
                                        }
                                        tmpMuxConn.close();
                                    }
                                }, "FS-L2R");
                                pipeFromLocalThread.start();

                            } catch (IOException e) {
                                Logger.error(e, "Unable to establish local connection");
                                tmpMuxConn.close();
                            }
                        }

                    } catch (InterruptedException | MultiplexerClosedException e) {
                        if (dragoniteSocket.isAlive()) {
                            Logger.error(e, "Cannot accept multiplexed connection");
                        }
                    }
                }, "FS-MuxAcceptor");
                multiplexerAcceptThread.start();

                byte[] buf;
                try {
                    while ((buf = dragoniteSocket.read()) != null) {
                        multiplexer.onReceiveBytes(buf);
                    }
                } catch (InterruptedException | ConnectionNotAliveException ignored) {
                } finally {
                    try {
                        dragoniteSocket.closeGracefully();
                    } catch (Exception ignored) {
                    }
                    multiplexer.close();
                    Logger.info("Client \"{}\" ({}) disconnected",
                            infoHeader.getName(), dragoniteSocket.getRemoteSocketAddress().toString());
                }

            }
        }
    }


}
