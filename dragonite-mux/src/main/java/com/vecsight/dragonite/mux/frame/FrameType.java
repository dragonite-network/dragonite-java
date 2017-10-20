/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.frame;

public enum FrameType {
    CREATE((byte) 0),
    DATA((byte) 1),
    CLOSE((byte) 2),
    PAUSE((byte) 3),
    CONTINUE((byte) 4);

    private final byte value;

    FrameType(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    private static final FrameType[] types = FrameType.values();

    public static FrameType fromByte(final byte type) {
        try {
            return types[type];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Type byte " + type + " not found");
        }
    }

}
