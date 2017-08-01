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

    private static final byte VERSION = ProxyGlobalConstants.PROTOCOL_VERSION;

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

            msg = new String(reader.getBytesGroupWithShortLength(), ProxyGlobalConstants.STRING_CHARSET);

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
        final byte[] msgBytes = msg.getBytes(ProxyGlobalConstants.STRING_CHARSET);

        final BinaryWriter writer = new BinaryWriter(getFixedLength() + msgBytes.length);

        writer.putSignedByte(VERSION)
                .putSignedByte(status)
                .putBytesGroupWithShortLength(msgBytes);

        return writer.toBytes();
    }
}
