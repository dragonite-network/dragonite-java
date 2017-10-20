/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.network.server;

import com.vecsight.dragonite.mux.conn.Multiplexer;
import com.vecsight.dragonite.mux.exception.MultiplexerClosedException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.ClientInfoHeader;
import com.vecsight.dragonite.proxy.header.ServerResponseHeader;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import com.vecsight.dragonite.utils.type.UnitConverter;
import org.pmw.tinylog.Logger;

import java.io.IOException;

public class ProxyClientHandler {

    private final DragoniteSocket dragoniteSocket;

    private final int limitMbps;

    private final String welcomeMessage;

    private final boolean allowLoopback;

    private final PacketCryptor packetCryptor;

    public ProxyClientHandler(final DragoniteSocket dragoniteSocket, final int limitMbps, final String welcomeMessage,
                              final boolean allowLoopback, final PacketCryptor packetCryptor) {
        this.dragoniteSocket = dragoniteSocket;
        this.limitMbps = limitMbps;
        this.welcomeMessage = welcomeMessage;
        this.allowLoopback = allowLoopback;
        this.packetCryptor = packetCryptor;
    }

    public void run() {
        if (dragoniteSocket.isAlive()) {

            //Receive client info header
            final byte[] headerBytes;
            try {
                headerBytes = dragoniteSocket.read();
            } catch (final InterruptedException | ConnectionNotAliveException e) {
                Logger.error(e, "Unable to read header from client {}", dragoniteSocket.getRemoteSocketAddress().toString());
                dragoniteSocket.destroy();
                return;
            }
            final ClientInfoHeader infoHeader;
            try {
                infoHeader = new ClientInfoHeader(headerBytes);
            } catch (final IncorrectHeaderException e) {
                Logger.error(e, "Incorrect header from client {}", dragoniteSocket.getRemoteSocketAddress().toString());
                try {
                    dragoniteSocket.closeGracefully();
                } catch (InterruptedException | IOException | SenderClosedException ignored) {
                }
                return;
            }

            //Send response
            final byte[] responseBytes = new ServerResponseHeader((byte) 0, welcomeMessage).toBytes();
            try {
                dragoniteSocket.send(responseBytes);
            } catch (InterruptedException | DragoniteException | IOException e) {
                Logger.error(e, "Cannot send response to client {}", dragoniteSocket.getRemoteSocketAddress().toString());
                try {
                    dragoniteSocket.closeGracefully();
                } catch (InterruptedException | IOException | SenderClosedException ignored) {
                }
                return;
            }

            //Show info and set parameters
            Logger.info("Remote ({}) \"{}\" using client {} on {} (DL: {}, UL: {}) connected",
                    dragoniteSocket.getRemoteSocketAddress().toString(),
                    infoHeader.getName(), infoHeader.getAppVer(), infoHeader.getOsName(),
                    infoHeader.getDownMbps(), infoHeader.getUpMbps());

            dragoniteSocket.setDescription(infoHeader.getName());

            int realMbps = infoHeader.getDownMbps();
            if (realMbps > limitMbps && limitMbps > 0) {
                realMbps = limitMbps;
                Logger.info("The DL Mbps of client \"{}\" has been limited from {} to {}",
                        infoHeader.getName(), infoHeader.getDownMbps(), realMbps);
            }
            dragoniteSocket.setSendSpeed(UnitConverter.mbpsToSpeed(realMbps));

            //Setup mux stuff
            final Multiplexer multiplexer = new Multiplexer(bytes -> {
                try {
                    dragoniteSocket.send(bytes);
                } catch (InterruptedException | IncorrectSizeException | SenderClosedException | IOException e) {
                    Logger.error(e, "Multiplexer is unable to send data");
                }
            }, ProxyGlobalConstants.MAX_FRAME_SIZE);

            final ProxyMuxHandler muxHandler = new ProxyMuxHandler(multiplexer, infoHeader.getName(),
                    dragoniteSocket.getRemoteSocketAddress(), allowLoopback, packetCryptor);

            final Thread multiplexerAcceptThread = new Thread(() -> {
                try {
                    muxHandler.run();
                } catch (InterruptedException | MultiplexerClosedException e) {
                    if (dragoniteSocket.isAlive()) {
                        Logger.error(e, "Cannot accept multiplexed connection");
                    }
                }
            }, "PS-MuxAcceptor");
            multiplexerAcceptThread.start();

            //Do receive for multiplexer here
            byte[] buf;
            try {
                while ((buf = dragoniteSocket.read()) != null) {
                    multiplexer.onReceiveBytes(buf);
                }
            } catch (InterruptedException | ConnectionNotAliveException ignored) {
            } finally {
                try {
                    dragoniteSocket.closeGracefully();
                } catch (final Exception ignored) {
                }
                multiplexer.close();
                Logger.info("Client \"{}\" ({}) disconnected",
                        infoHeader.getName(), dragoniteSocket.getRemoteSocketAddress().toString());
            }
        }
    }

}
