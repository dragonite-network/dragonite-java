/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.msg;

public enum MessageType {
    DATA((byte) 0),
    CLOSE((byte) 1),
    ACK((byte) 2),
    HEARTBEAT((byte) 3);

    private final byte value;

    MessageType(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    private static final MessageType[] types = MessageType.values();

    public static MessageType fromByte(final byte type) {
        try {
            return types[type];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Type byte " + type + " not found");
        }
    }

}
