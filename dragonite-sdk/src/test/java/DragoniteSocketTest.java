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

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.socket.DragoniteClientSocket;
import com.vecsight.dragonite.sdk.socket.DragoniteServer;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class DragoniteSocketTest {

    private final DragoniteServer dragoniteServer = new DragoniteServer(9876, 100 * 1024, new DragoniteSocketParameters());

    public DragoniteSocketTest() throws SocketException {
    }

    @Test
    public void basicAcceptReadSend() throws IOException, InterruptedException, DragoniteException {
        final String text = "By design, JUnit does not specify the execution order of test method invocations. " +
                "Until now, the methods were simply invoked in the order returned by the reflection API. " +
                "However, using the JVM order is unwise since the Java platform does not specify any particular order, " +
                "and in fact JDK 7 returns a more or less random order.";
        final DragoniteClientSocket clientSocket = new DragoniteClientSocket(new InetSocketAddress("localhost", 9876), 100 * 1024, new DragoniteSocketParameters());
        final DragoniteSocket socket = dragoniteServer.accept();
        clientSocket.send(text.getBytes());
        final byte[] sr = socket.read();
        assertEquals(text, new String(sr));
        socket.send(text.getBytes());
        final byte[] cr = clientSocket.read();
        assertEquals(text, new String(cr));
    }

}
