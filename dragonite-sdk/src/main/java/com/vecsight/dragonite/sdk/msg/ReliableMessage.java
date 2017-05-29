package com.vecsight.dragonite.sdk.msg;

public interface ReliableMessage extends Message {

    int getSequence();

    void setSequence(int sequence);

}
