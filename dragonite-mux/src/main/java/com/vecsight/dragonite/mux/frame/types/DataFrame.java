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

package com.vecsight.dragonite.mux.frame.types;

import com.vecsight.dragonite.mux.exception.DataLengthMismatchException;
import com.vecsight.dragonite.mux.exception.IncorrectFrameException;
import com.vecsight.dragonite.mux.frame.Frame;
import com.vecsight.dragonite.mux.frame.FrameType;
import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/*
 * VERSION  1B
 * TYPE     1B
 * connID   2B
 * length   2B
 * data     [length]
 */

public class DataFrame implements Frame {

    private static final byte VERSION = MuxGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = FrameType.DATA;

    public static final int FIXED_LENGTH = 6;

    private short connectionID;

    private byte[] data;

    public DataFrame(final short connectionID, final byte[] data) {
        this.connectionID = connectionID;
        this.data = data;
    }

    public DataFrame(final byte[] frame) throws IncorrectFrameException, DataLengthMismatchException {
        final ByteBuffer buffer = ByteBuffer.wrap(frame);
        final byte remoteVersion = buffer.get();
        final byte remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectFrameException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectFrameException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        connectionID = buffer.getShort();

        final short length = buffer.getShort();
        data = new byte[length];
        try {
            buffer.get(data);
        } catch (final BufferUnderflowException e) {
            throw new DataLengthMismatchException("Length mismatch (" + length + ")");
        }

    }

    public short getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(final short connectionID) {
        this.connectionID = connectionID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public byte getType() {
        return TYPE;
    }

    @Override
    public byte[] toBytes() {
        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + data.length);
        buffer.put(VERSION);
        buffer.put(TYPE);
        buffer.putShort(connectionID);
        buffer.putShort((short) data.length);
        buffer.put(data);
        return buffer.array();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }
}
