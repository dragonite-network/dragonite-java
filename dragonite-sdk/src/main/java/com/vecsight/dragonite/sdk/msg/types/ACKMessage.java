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
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.MessageType;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

public class ACKMessage implements Message {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final MessageType TYPE = MessageType.ACK;

    public static final int FIXED_LENGTH = 8;

    private int[] sequenceList;

    private int consumedSeq;

    public ACKMessage(final int[] sequenceList, final int consumedSeq) {
        this.sequenceList = sequenceList;
        this.consumedSeq = consumedSeq;
    }

    public ACKMessage(final byte[] msg) throws IncorrectMessageException {
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

            consumedSeq = reader.getSignedInt();

            final int seqCount = reader.getUnsignedShort();

            sequenceList = new int[seqCount];
            for (int i = 0; i < seqCount; i++) {
                sequenceList[i] = reader.getSignedInt();
            }

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
        final BinaryWriter writer = new BinaryWriter(getFixedLength() + sequenceList.length * Integer.BYTES);

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedInt(consumedSeq)
                .putUnsignedShort(sequenceList.length);

        for (final int seq : sequenceList) {
            writer.putSignedInt(seq);
        }

        return writer.toBytes();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }

    public int[] getSequenceList() {
        return sequenceList;
    }

    public void setSequenceList(final int[] sequenceList) {
        this.sequenceList = sequenceList;
    }

    public int getConsumedSeq() {
        return consumedSeq;
    }

    public void setConsumedSeq(final int consumedSeq) {
        this.consumedSeq = consumedSeq;
    }
}
