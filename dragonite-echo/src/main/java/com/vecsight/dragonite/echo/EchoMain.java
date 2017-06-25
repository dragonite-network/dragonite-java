package com.vecsight.dragonite.echo;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.socket.DragoniteServer;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

public final class EchoMain {

    public static void main(final String[] args) throws SocketException, InterruptedException {
        final DragoniteSocketParameters parameters = new DragoniteSocketParameters();
        parameters.setEnableWebPanel(true);
        parameters.setWebPanelBindAddress(new InetSocketAddress(8001));
        final DragoniteServer dragoniteServer = new DragoniteServer(9225, 102400, parameters);
        DragoniteSocket tmpDragoniteSocket;
        while ((tmpDragoniteSocket = dragoniteServer.accept()) != null) {
            final DragoniteSocket dragoniteSocket = tmpDragoniteSocket;
            print("New connection from " + dragoniteSocket.getRemoteSocketAddress().toString());
            new Thread(() -> {
                try {
                    while (dragoniteSocket.isAlive()) {
                        final byte[] bytes = dragoniteSocket.read();
                        print(new String(bytes));
                        dragoniteSocket.send(bytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        dragoniteSocket.closeGracefully();
                    } catch (InterruptedException | IOException | SenderClosedException ignored) {
                    }
                    print(dragoniteSocket.getRemoteSocketAddress().toString() + " connection closed");
                }
            }).start();
        }
    }

    private static void print(final String msg) {
        System.out.println(msg);
    }

}
