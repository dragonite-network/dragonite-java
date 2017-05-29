package com.vecsight.dragonite.mux.frame;

public interface Frame {

    byte getVersion();

    byte getType();

    byte[] toBytes();

    int getFixedLength();

}
