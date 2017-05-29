package com.vecsight.dragonite.sdk.msg.types;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.MessageType;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;

import java.nio.ByteBuffer;

public class HeartbeatMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = MessageType.HEARTBEAT;

    public static final int FIXED_LENGTH = 6;

    private int sequence;

    public HeartbeatMessage(int sequence) {
        this.sequence = sequence;
    }

    public HeartbeatMessage(byte[] msg) throws IncorrectMessageException {
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        byte remoteVersion = buffer.get(), remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectMessageException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectMessageException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        sequence = buffer.getInt();
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
        ByteBuffer buffer = ByteBuffer.allocate(getFixedLength());
        buffer.put(VERSION);
        buffer.put(TYPE);
        buffer.putInt(sequence);
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
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
