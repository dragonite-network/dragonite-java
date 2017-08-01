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

package com.vecsight.dragonite.proxy.header.mux;

import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * status   1 SB
 * msgLen   1 UB
 * msg      [length]
 */

public class MuxConnectionResponseHeader {

    private ConnectionStatus status;

    private String msg;

    public MuxConnectionResponseHeader(final ConnectionStatus status, final String msg) {
        this.status = status;
        this.msg = msg;
    }

    public MuxConnectionResponseHeader(final byte[] header) throws IncorrectHeaderException {
        final BinaryReader reader = new BinaryReader(header);

        try {

            final byte rawStatus = reader.getSignedByte();

            try {
                status = ConnectionStatus.fromByte(rawStatus);
            } catch (final IllegalArgumentException e) {
                throw new IncorrectHeaderException("Invalid status type " + rawStatus);
            }

            msg = new String(reader.getBytesGroupWithByteLength(), ProxyGlobalConstants.STRING_CHARSET);

        } catch (final BufferUnderflowException e) {
            throw new IncorrectHeaderException("Incorrect frame length");
        }

    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(final ConnectionStatus status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

    public byte[] toBytes() {
        final byte[] msgBytes = msg.getBytes(ProxyGlobalConstants.STRING_CHARSET);

        final BinaryWriter writer = new BinaryWriter(2 + msgBytes.length);

        writer.putSignedByte(status.getValue())
                .putBytesGroupWithByteLength(msgBytes);

        return writer.toBytes();
    }
}
