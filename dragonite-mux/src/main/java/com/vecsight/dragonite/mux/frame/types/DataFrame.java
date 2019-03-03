/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.frame.types;

import com.vecsight.dragonite.mux.exception.IncorrectFrameException;
import com.vecsight.dragonite.mux.frame.Frame;
import com.vecsight.dragonite.mux.frame.FrameType;
import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * VERSION  1 SB
 * TYPE     1 SB
 * connID   2 SS
 * length   2 US
 * data     [length]
 */

public class DataFrame implements Frame {

    private static final byte VERSION = MuxGlobalConstants.PROTOCOL_VERSION;

    private static final FrameType TYPE = FrameType.DATA;

    public static final int FIXED_LENGTH = 6;

    private short connectionID;

    private byte[] data;

    private int expectedLength = 0;

    public DataFrame(final short connectionID, final byte[] data) {
        this.connectionID = connectionID;
        this.data = data;
    }

    public DataFrame(final byte[] frame) throws IncorrectFrameException {
        final BinaryReader reader = new BinaryReader(frame);

        try {

            final byte remoteVersion = reader.getSignedByte();
            final byte remoteType = reader.getSignedByte();

            if (remoteVersion != VERSION) {
                throw new IncorrectFrameException("Incorrect version (" + remoteVersion + ", should be " + VERSION + ")");
            }
            if (remoteType != TYPE.getValue()) {
                throw new IncorrectFrameException("Incorrect type (" + remoteType + ", should be " + TYPE + ")");
            }

            connectionID = reader.getSignedShort();

            final int length = reader.getUnsignedShort();

            if (reader.remaining() < length) {
                expectedLength = length;
            } else {
                data = new byte[length];
                reader.getBytes(data);
            }

        } catch (final BufferUnderflowException e) {
            throw new IncorrectFrameException("Incorrect frame length");
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
    public FrameType getType() {
        return TYPE;
    }

    @Override
    public byte[] toBytes() {
        final BinaryWriter writer = new BinaryWriter(getFixedLength() + data.length);

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedShort(connectionID)
                .putBytesGroupWithShortLength(data);

        return writer.toBytes();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }

    @Override
    public int getExpectedLength() {
        return expectedLength;
    }
}
