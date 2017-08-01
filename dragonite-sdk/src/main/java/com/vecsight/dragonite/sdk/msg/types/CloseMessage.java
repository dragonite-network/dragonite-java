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
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

public class CloseMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final MessageType TYPE = MessageType.CLOSE;

    public static final int FIXED_LENGTH = 8;

    private int sequence;

    private short status;

    public CloseMessage(final int sequence, final short status) {
        this.sequence = sequence;
        this.status = status;
    }

    public CloseMessage(final byte[] msg) throws IncorrectMessageException {
        final BinaryReader reader = new BinaryReader(msg);

        try {

            final byte remoteVersion = reader.getSignedByte();
            final byte remoteType = reader.getSignedByte();

            if (remoteVersion != VERSION) {
                throw new IncorrectMessageException("Incorrect version (" + remoteVersion + ", should be " + VERSION + ")");
            }
            if (remoteType != TYPE.getValue()) {
                throw new IncorrectMessageException("Incorrect type (" + remoteType + ", should be " + TYPE + ")");
            }

            sequence = reader.getSignedInt();

            status = reader.getSignedShort();

        } catch (final BufferUnderflowException e) {
            throw new IncorrectMessageException("Incorrect message length");
        }
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public MessageType getType() {
        return TYPE;
    }

    @Override
    public byte[] toBytes() {
        final BinaryWriter writer = new BinaryWriter(getFixedLength());

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedInt(sequence)
                .putSignedShort(status);

        return writer.toBytes();
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
