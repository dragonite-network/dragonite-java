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

package com.vecsight.dragonite.utils.binary;

import java.nio.ByteBuffer;

public class BinaryReader {

    private final ByteBuffer byteBuffer;

    public BinaryReader(final byte[] bytes) {
        byteBuffer = ByteBuffer.wrap(bytes);
    }

    public byte getSignedByte() {
        return byteBuffer.get();
    }

    public short getUnsignedByte() {
        return (short) (byteBuffer.get() & (short) 0xff);
    }

    public short getSignedShort() {
        return byteBuffer.getShort();
    }

    public int getUnsignedShort() {
        return byteBuffer.getShort() & 0xffff;
    }

    public int getSignedInt() {
        return byteBuffer.getInt();
    }

    public long getUnsignedInt() {
        return (long) byteBuffer.getInt() & 0xffffffffL;
    }

    public void getBytes(final byte[] bytes) {
        byteBuffer.get(bytes);
    }

    public byte[] getBytesGroupWithByteLength() {
        final short len = getUnsignedByte();
        final byte[] bytes = new byte[len];
        byteBuffer.get(bytes);
        return bytes;
    }

    public byte[] getBytesGroupWithShortLength() {
        final int len = getUnsignedShort();
        final byte[] bytes = new byte[len];
        byteBuffer.get(bytes);
        return bytes;
    }

    public boolean getBoolean() {
        return byteBuffer.get() != 0;
    }

    public int remaining() {
        return byteBuffer.remaining();
    }

}
