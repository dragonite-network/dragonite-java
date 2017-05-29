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

    public DataFrame(short connectionID, byte[] data) {
        this.connectionID = connectionID;
        this.data = data;
    }

    public DataFrame(byte[] frame) throws IncorrectFrameException, DataLengthMismatchException {
        ByteBuffer buffer = ByteBuffer.wrap(frame);
        byte remoteVersion = buffer.get(), remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectFrameException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectFrameException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        connectionID = buffer.getShort();

        short length = buffer.getShort();
        data = new byte[length];
        try {
            buffer.get(data);
        } catch (BufferUnderflowException e) {
            throw new DataLengthMismatchException("Length mismatch (" + length + ")");
        }

    }

    public short getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(short connectionID) {
        this.connectionID = connectionID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
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
        ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + data.length);
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
