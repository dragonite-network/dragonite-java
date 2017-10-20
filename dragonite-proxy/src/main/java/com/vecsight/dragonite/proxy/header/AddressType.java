/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.header;

public enum AddressType {
    IPv4((byte) 0),
    IPv6((byte) 1),
    DOMAIN((byte) 2);

    private final byte value;

    AddressType(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    private static final AddressType[] types = AddressType.values();

    public static AddressType fromByte(final byte type) {
        try {
            return types[type];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Type byte " + type + " not found");
        }
    }
}
