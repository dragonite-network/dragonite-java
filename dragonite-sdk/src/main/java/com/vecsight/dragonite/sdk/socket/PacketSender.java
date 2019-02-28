/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

import java.io.IOException;

public interface PacketSender {

    void sendPacket(byte[] bytes) throws InterruptedException, IOException;

}
