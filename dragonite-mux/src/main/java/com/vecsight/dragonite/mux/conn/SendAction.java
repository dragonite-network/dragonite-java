package com.vecsight.dragonite.mux.conn;

public interface SendAction {

    void sendPacket(byte[] bytes);

}