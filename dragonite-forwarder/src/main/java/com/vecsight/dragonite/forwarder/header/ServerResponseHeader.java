package com.vecsight.dragonite.forwarder.header;

import com.vecsight.dragonite.forwarder.exception.IncorrectHeaderException;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;

import java.nio.ByteBuffer;

/*
 * VERSION  1B
 * status   1B
 * msgLen   2B
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
        final ByteBuffer buffer = ByteBuffer.wrap(header);
        final byte remoteVersion = buffer.get();

        if (remoteVersion != VERSION) {
            throw new IncorrectHeaderException("Incorrect Version Field! (" + remoteVersion + ", should be " + VERSION + ")");
        }

        status = buffer.get();

        final short msgLen = buffer.getShort();
        final byte[] rawMsg = new byte[msgLen];
        buffer.get(rawMsg);
        msg = new String(rawMsg, ForwarderGlobalConstants.STRING_CHARSET);

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

        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + msgBytes.length);
        buffer.put(VERSION);
        buffer.put(status);
        buffer.putShort((short) msgBytes.length);
        buffer.put(msgBytes);
        return buffer.array();
    }
}
