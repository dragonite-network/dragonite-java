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

import com.vecsight.dragonite.proxy.config.ProxyServerConfig;
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.misc.EncryptionKeyGenerator;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.socket.DragoniteServer;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import org.pmw.tinylog.Logger;

import java.net.InetSocketAddress;
import java.net.SocketException;

public class ProxyServer {

    private final InetSocketAddress bindAddress;

    private final String password;

    private final byte[] encryptionKey;

    private final int limitMbps;

    private final String welcomeMessage;

    private final DragoniteServer dragoniteServer;

    private volatile boolean doAccept = true;

    private final Thread acceptThread;

    public ProxyServer(final ProxyServerConfig config) throws SocketException, EncryptionException {
        this.bindAddress = config.getBindAddress();
        this.password = config.getPassword();
        this.limitMbps = config.getMbpsLimit();
        this.welcomeMessage = config.getWelcomeMessage();

        this.encryptionKey = EncryptionKeyGenerator.getKey(password);

        this.dragoniteServer = new DragoniteServer(bindAddress.getAddress(), bindAddress.getPort(),
                ProxyGlobalConstants.INIT_SEND_SPEED, config.getDragoniteSocketParameters());

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
        }, "PS-Accept");
        acceptThread.start();
    }

    private void handleClient(final DragoniteSocket socket) {
        final ProxyClientHandler clientHandler = new ProxyClientHandler(encryptionKey, socket, limitMbps, welcomeMessage);
        final Thread handlerThread = new Thread(clientHandler::run, "PS-Handler");
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
