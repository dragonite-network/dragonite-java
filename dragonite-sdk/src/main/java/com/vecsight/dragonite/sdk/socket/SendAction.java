package com.vecsight.dragonite.sdk.socket;

import java.io.IOException;

public interface SendAction {

    void sendPacket(byte[] bytes) throws InterruptedException, IOException;

}
