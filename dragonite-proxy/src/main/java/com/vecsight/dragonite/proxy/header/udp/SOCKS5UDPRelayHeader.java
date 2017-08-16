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

package com.vecsight.dragonite.proxy.header.udp;

import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
        https://www.ietf.org/rfc/rfc1928.txt

      +----+------+------+----------+----------+----------+
      |RSV | FRAG | ATYP | DST.ADDR | DST.PORT |   DATA   |
      +----+------+------+----------+----------+----------+
      | 2  |  1   |  1   | Variable |    2     | Variable |
      +----+------+------+----------+----------+----------+

     The fields in the UDP request header are:

          o  RSV            Reserved X'0000'
          o  FRAG           Current fragment number
          o  ATYP           address type of following addresses:
             o  IP V4 address: X'01'
             o  DOMAINNAME: X'03'
             o  IP V6 address: X'04'
          o  DST.ADDR       desired destination address
          o  DST.PORT       desired destination port
          o  DATA           user data
 */

public class SOCKS5UDPRelayHeader {

    private AddressType type;

    private byte[] addr;

    private int port;

    private byte[] payload;

    public SOCKS5UDPRelayHeader(final AddressType type, final byte[] addr, final int port, final byte[] payload) {
        this.type = type;
        this.addr = addr;
        this.port = port;
        this.payload = payload;
    }

    public SOCKS5UDPRelayHeader(final byte[] header) throws IncorrectHeaderException {
        final BinaryReader reader = new BinaryReader(header);

        try {

            final short rsv = reader.getSignedShort();

            final byte frag = reader.getSignedByte();

            if (frag != 0) {
                throw new IncorrectHeaderException("UDP fragmentation is not supported");
            }

            final byte rawType = reader.getSignedByte();

            if (rawType == 1) {
                type = AddressType.IPv4;
            } else if (rawType == 3) {
                type = AddressType.DOMAIN;
            } else if (rawType == 4) {
                type = AddressType.IPv6;
            } else {
                throw new IncorrectHeaderException("Unknown address type");
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

            payload = new byte[reader.remaining()];
            reader.getBytes(payload);

        } catch (final BufferUnderflowException e) {
            throw new IncorrectHeaderException("Incorrect packet length");
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    public byte[] toBytes() {
        final int addrTotalLength = (type == AddressType.IPv4 ? 4 : (type == AddressType.IPv6 ? 16 : 1 + addr.length));

        final BinaryWriter writer = new BinaryWriter(6 + addrTotalLength + payload.length);

        writer.putSignedShort((short) 0); //RSV
        writer.putSignedByte((byte) 0); //FRAG

        if (type == AddressType.DOMAIN) {
            writer.putSignedByte((byte) 3);
            writer.putBytesGroupWithByteLength(addr);
        } else {
            if (type == AddressType.IPv4) {
                writer.putSignedByte((byte) 1);
            } else {
                writer.putSignedByte((byte) 4);
            }
            writer.putBytes(addr);
        }
        writer.putUnsignedShort(port);
        writer.putBytes(payload);

        return writer.toBytes();
    }

}
