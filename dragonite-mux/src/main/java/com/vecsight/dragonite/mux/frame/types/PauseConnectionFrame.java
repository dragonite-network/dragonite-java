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
 */

public class PauseConnectionFrame implements Frame {

    private static final byte VERSION = MuxGlobalConstants.PROTOCOL_VERSION;

    private static final FrameType TYPE = FrameType.PAUSE;

    public static final int FIXED_LENGTH = 4;

    private short connectionID;

    public PauseConnectionFrame(final short connectionID) {
        this.connectionID = connectionID;
    }

    public PauseConnectionFrame(final byte[] frame) throws IncorrectFrameException {
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
        final BinaryWriter writer = new BinaryWriter(getFixedLength());

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedShort(connectionID);

        return writer.toBytes();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }
}
