package com.vecsight.dragonite.sdk.msg.types;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.MessageType;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;

import java.nio.ByteBuffer;

public class DataMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = MessageType.DATA;

    public static final int FIXED_LENGTH = 8;

    private int sequence;

    private byte[] data;

    public DataMessage(int sequence, byte[] data) {
        this.sequence = sequence;
        this.data = data;
    }

    public DataMessage(byte[] msg) throws IncorrectMessageException {
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        byte remoteVersion = buffer.get(), remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectMessageException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectMessageException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        sequence = buffer.getInt();

        short length = buffer.getShort();
        data = new byte[length];
        buffer.get(data);
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
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + data.length);
        buffer.put(VERSION);
        buffer.put(TYPE);
        buffer.putInt(sequence);
        buffer.putShort((short) data.length);
        buffer.put(data);
        return buffer.array();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }
}
