package com.vecsight.dragonite.mux.frame.types;

import com.vecsight.dragonite.mux.exception.IncorrectFrameException;
import com.vecsight.dragonite.mux.frame.Frame;
import com.vecsight.dragonite.mux.frame.FrameType;
import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;

import java.nio.ByteBuffer;

/*
 * VERSION  1B
 * TYPE     1B
 * connID   2B
 */

public class PauseConnectionFrame implements Frame {

    private static final byte VERSION = MuxGlobalConstants.PROTOCOL_VERSION;

    private static final byte TYPE = FrameType.PAUSE;

    public static final int FIXED_LENGTH = 4;

    private short connectionID;

    public PauseConnectionFrame(short connectionID) {
        this.connectionID = connectionID;
    }

    public PauseConnectionFrame(byte[] frame) throws IncorrectFrameException {
        ByteBuffer buffer = ByteBuffer.wrap(frame);
        byte remoteVersion = buffer.get(), remoteType = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectFrameException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }
        if (remoteType != TYPE) {
            throw new IncorrectFrameException("Incorrect Type Field! (" + remoteType + ", should be " + TYPE + ")");
        }

        connectionID = buffer.getShort();

    }

    public short getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(short connectionID) {
        this.connectionID = connectionID;
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
        buffer.putShort(connectionID);
        return buffer.array();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }
}
