/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.network.server;

import com.vecsight.dragonite.forwarder.config.ForwarderServerConfig;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.sdk.socket.DragoniteServer;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import org.pmw.tinylog.Logger;

import java.net.InetSocketAddress;
import java.net.SocketException;

public class ForwarderServer {

    private final InetSocketAddress bindAddress;

    private final InetSocketAddress forwardingAddress;

    private final int limitMbps;

    private final String welcomeMessage;

    private final DragoniteServer dragoniteServer;

    private volatile boolean doAccept = true;

    private final Thread acceptThread;

    public ForwarderServer(final ForwarderServerConfig config) throws SocketException {
        this.bindAddress = config.getBindAddress();
        this.forwardingAddress = config.getForwardingAddress();
        this.limitMbps = config.getMbpsLimit();
        this.welcomeMessage = config.getWelcomeMessage();

        this.dragoniteServer = new DragoniteServer(bindAddress.getAddress(), bindAddress.getPort(),
                ForwarderGlobalConstants.INIT_SEND_SPEED, config.getDragoniteSocketParameters());

        acceptThread = new Thread(() -> {
            try {
                DragoniteSocket socket;
                while (doAccept && (socket = dragoniteServer.accept()) != null) {
                    Logger.debug("New client from {}", socket.getRemoteSocketAddress().toString());
                    handleClient(socket);
                }
            } catch (final InterruptedException e) {
                Logger.error(e, "Unable to accept Dragonite connections");
            }
        }, "FS-Accept");
        acceptThread.start();
    }

    private void handleClient(final DragoniteSocket socket) {
        final ForwarderClientHandler clientHandler = new ForwarderClientHandler(forwardingAddress, socket, limitMbps, welcomeMessage);
        final Thread handlerThread = new Thread(clientHandler::run, "FS-Handler");
        handlerThread.start();
    }

    public boolean isDoAccept() {
        return doAccept;
    }

    public void stopAccept() {
        acceptThread.interrupt();
        doAccept = false;
    }
}
