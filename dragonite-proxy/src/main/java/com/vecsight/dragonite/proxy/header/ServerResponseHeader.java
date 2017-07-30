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

package com.vecsight.dragonite.proxy.header;

import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;

import java.nio.ByteBuffer;

/*
 * VERSION  1B
 * status   1B
 * msgLen   2B
 * msg      [length]
 */

public class ServerResponseHeader {

    private static final byte VERSION = ProxyGlobalConstants.PROTOCOL_VERSION;

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
        msg = new String(rawMsg, ProxyGlobalConstants.STRING_CHARSET);

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
        final byte[] msgBytes = msg.getBytes(ProxyGlobalConstants.STRING_CHARSET);

        final ByteBuffer buffer = ByteBuffer.allocate(getFixedLength() + msgBytes.length);
        buffer.put(VERSION);
        buffer.put(status);
        buffer.putShort((short) msgBytes.length);
        buffer.put(msgBytes);
        return buffer.array();
    }
}
