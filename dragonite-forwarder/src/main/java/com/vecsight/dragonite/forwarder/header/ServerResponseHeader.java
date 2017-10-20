/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.header;

import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * VERSION  1 SB
 * status   1 SB
 * msgLen   2 US
 * msg      [length]
 */

public class ServerResponseHeader {

    private static final byte VERSION = ForwarderGlobalConstants.PROTOCOL_VERSION;

    public static final int FIXED_LENGTH = 4;

    private byte status;

    private String msg;

    public ServerResponseHeader(final byte status, final String msg) {
        this.status = status;
        this.msg = msg;
    }

    public ServerResponseHeader(final byte[] header) throws IncorrectHeaderException {
        final BinaryReader reader = new BinaryReader(header);

        try {

            final byte remoteVersion = reader.getSignedByte();

            if (remoteVersion != VERSION) {
                throw new IncorrectHeaderException("Incorrect version (" + remoteVersion + ", should be " + VERSION + ")");
            }

            status = reader.getSignedByte();

            msg = new String(reader.getBytesGroupWithShortLength(), ForwarderGlobalConstants.STRING_CHARSET);

        } catch (final BufferUnderflowException e) {
            throw new IncorrectHeaderException("Incorrect header length");
        }
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(final byte status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

    public static byte getVersion() {
        return VERSION;
    }

    public static int getFixedLength() {
        return FIXED_LENGTH;
    }

    public byte[] toBytes() {
        final byte[] msgBytes = msg.getBytes(ForwarderGlobalConstants.STRING_CHARSET);

        final BinaryWriter writer = new BinaryWriter(getFixedLength() + msgBytes.length);

        writer.putSignedByte(VERSION)
                .putSignedByte(status)
                .putBytesGroupWithShortLength(msgBytes);

        return writer.toBytes();
    }
}
