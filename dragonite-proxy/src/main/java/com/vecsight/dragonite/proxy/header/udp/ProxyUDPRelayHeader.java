/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.header.udp;

import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

/*
 * addrType 1 SB
 * addr     [4B/16B/1+length]
 * port     2 US
 * payload  [REMAINING]
 */

public class ProxyUDPRelayHeader {

    private AddressType type;

    private byte[] addr;

    private int port;

    private byte[] payload;

    public ProxyUDPRelayHeader(final AddressType type, final byte[] addr, final int port, final byte[] payload) {
        this.type = type;
        this.addr = addr;
        this.port = port;
        this.payload = payload;
    }

    public ProxyUDPRelayHeader(final byte[] header) throws IncorrectHeaderException {
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

        final BinaryWriter writer = new BinaryWriter(3 + addrTotalLength + payload.length);

        writer.putSignedByte(type.getValue());
        if (type == AddressType.DOMAIN) {
            writer.putBytesGroupWithByteLength(addr);
        } else {
            writer.putBytes(addr);
        }
        writer.putUnsignedShort(port);
        writer.putBytes(payload);

        return writer.toBytes();
    }

}
