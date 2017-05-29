package com.vecsight.dragonite.sdk.msg;

public interface Message {

    byte getVersion();

    byte getType();

    byte[] toBytes();

    int getFixedLength();

}
