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
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * addrType 1 SB
 * addr     [4B/16B/1+length]
 * port     2 US
 */

public class MuxConnectionRequestHeader {

    private AddressType type;

    private byte[] addr;

    private int port;

    public MuxConnectionRequestHeader(final AddressType type, final byte[] addr, final int port) {
        this.type = type;
        this.addr = addr;
        this.port = port;
    }

    public MuxConnectionRequestHeader(final byte[] header) throws IncorrectHeaderException {
        final BinaryReader reader = new BinaryReader(header);

        try {

            final byte rawType = reader.getSignedByte();

            try {
                type = AddressType.fromByte(rawType);
            } catch (final IllegalArgumentException e) {
                throw new IncorrectHeaderException("Invalid address type " + rawType);
            }

            switch (type) {
                case IPv4:
                    addr = new byte[4];
                    reader.getBytes(addr);
                    break;
                case IPv6:
                    addr = new byte[16];
                    reader.getBytes(addr);
                    break;
                case DOMAIN:
                    addr = reader.getBytesGroupWithByteLength();
                    break;
                default:
                    throw new IncorrectHeaderException("Invalid address type " + rawType);
            }

            port = reader.getUnsignedShort();

        } catch (final BufferUnderflowException e) {
            throw new IncorrectHeaderException("Incorrect frame length");
        }
    }

    public AddressType getType() {
        return type;
    }

    public void setType(final AddressType type) {
        this.type = type;
    }

    public byte[] getAddr() {
        return addr;
    }

    public void setAddr(final byte[] addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public byte[] toBytes() {
        final int addrTotalLength = (type == AddressType.IPv4 ? 4 : (type == AddressType.IPv6 ? 16 : 1 + addr.length));

        final BinaryWriter writer = new BinaryWriter(3 + addrTotalLength);

        writer.putSignedByte(type.getValue());
        if (type == AddressType.DOMAIN) {
            writer.putBytesGroupWithByteLength(addr);
        } else {
            writer.putBytes(addr);
        }
        writer.putUnsignedShort(port);

        return writer.toBytes();
    }
}
