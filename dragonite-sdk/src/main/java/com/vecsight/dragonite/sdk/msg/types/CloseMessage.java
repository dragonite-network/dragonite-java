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

package com.vecsight.dragonite.sdk.msg.types;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.MessageType;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;

import java.nio.ByteBuffer;

public class CloseMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = MessageType.CLOSE;

    public static final int FIXED_LENGTH = 8;

    private int sequence;

    private short status;

    public CloseMessage(final int sequence, final short status) {
        this.sequence = sequence;
        this.status = status;
    }

    public CloseMessage(final byte[] msg) throws IncorrectMessageException {
        final ByteBuffer buffer = ByteBuffer.wrap(msg);
        final byte remoteVersion = buffer.get();
        final byte remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectMessageException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectMessageException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        sequence = buffer.getInt();

        status = buffer.getShort();
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
        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength());
        buffer.put(VERSION);
        buffer.put(TYPE);
        buffer.putInt(sequence);
        buffer.putShort(status);
        return buffer.array();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(final short status) {
        this.status = status;
    }
}
