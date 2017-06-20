package com.vecsight.dragonite.sdk.msg.types;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.MessageType;

import java.nio.ByteBuffer;

public class ACKMessage implements Message {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = MessageType.ACK;

    public static final int FIXED_LENGTH = 8;

    private int[] sequenceList;

    private int receiveSeq;

    public ACKMessage(final int[] sequenceList, final int receiveSeq) {
        this.sequenceList = sequenceList;
        this.receiveSeq = receiveSeq;
    }

    public ACKMessage(final byte[] msg) throws IncorrectMessageException {
        final ByteBuffer buffer = ByteBuffer.wrap(msg);
        final byte remoteVersion = buffer.get();
        final byte remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectMessageException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectMessageException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        receiveSeq = buffer.getInt();

        final short seqCount = buffer.getShort();

        sequenceList = new int[seqCount];
        for (int i = 0; i < seqCount; i++) {
            sequenceList[i] = buffer.getInt();
        }
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
        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + sequenceList.length * Integer.BYTES);
        buffer.put(VERSION);
        buffer.put(TYPE);
        buffer.putInt(receiveSeq);
        buffer.putShort((short) sequenceList.length);
        for (final int seq : sequenceList) {
            buffer.putInt(seq);
        }
        return buffer.array();
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

    public int getReceiveSeq() {
        return receiveSeq;
    }

    public void setReceiveSeq(final int receiveSeq) {
        this.receiveSeq = receiveSeq;
    }
}
