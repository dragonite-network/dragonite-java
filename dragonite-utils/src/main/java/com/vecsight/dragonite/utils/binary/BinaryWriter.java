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

public class BinaryWriter {

    private final ByteBuffer byteBuffer;

    public BinaryWriter(final int capacity) {
        byteBuffer = ByteBuffer.allocate(capacity);
    }

    public BinaryWriter putSignedByte(final byte sb) {
        byteBuffer.put(sb);
        return this;
    }

    public BinaryWriter putUnsignedByte(final short ub) {
        byteBuffer.put((byte) (ub & 0xff));
        return this;
    }

    public BinaryWriter putSignedShort(final short ss) {
        byteBuffer.putShort(ss);
        return this;
    }

    public BinaryWriter putUnsignedShort(final int us) {
        byteBuffer.putShort((short) (us & 0xffff));
        return this;
    }

    public BinaryWriter putSignedInt(final int si) {
        byteBuffer.putInt(si);
        return this;
    }

    public BinaryWriter putUnsignedInt(final long ui) {
        byteBuffer.putInt((int) (ui & 0xffffffffL));
        return this;
    }

    public BinaryWriter putBytes(final byte[] bytes) {
        byteBuffer.put(bytes);
        return this;
    }

    public BinaryWriter putBytesGroupWithByteLength(final byte[] bytes) {
        putUnsignedByte((short) bytes.length);
        putBytes(bytes);
        return this;
    }

    public BinaryWriter putBytesGroupWithShortLength(final byte[] bytes) {
        putUnsignedShort(bytes.length);
        putBytes(bytes);
        return this;
    }

    public byte[] toBytes() {
        return byteBuffer.array();
    }

}
