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

public class DataMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final MessageType TYPE = MessageType.DATA;

    public static final int FIXED_LENGTH = 8;

    private int sequence;

    private byte[] data;

    public DataMessage(final int sequence, final byte[] data) {
        this.sequence = sequence;
        this.data = data;
    }

    public DataMessage(final byte[] msg) throws IncorrectMessageException {
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

            data = reader.getBytesGroupWithShortLength();

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
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] toBytes() {
        final BinaryWriter writer = new BinaryWriter(getFixedLength() + data.length);

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedInt(sequence)
                .putBytesGroupWithShortLength(data);

        return writer.toBytes();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }
}
