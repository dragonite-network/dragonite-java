/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.header.mux;

public enum ConnectionStatus {
    OK((byte) 0),
    ERROR((byte) 1),
    REJECTED((byte) 2);

    private final byte value;

    ConnectionStatus(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    private static final ConnectionStatus[] types = ConnectionStatus.values();

    public static ConnectionStatus fromByte(final byte type) {
        try {
            return types[type];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Type byte " + type + " not found");
        }
    }
}
