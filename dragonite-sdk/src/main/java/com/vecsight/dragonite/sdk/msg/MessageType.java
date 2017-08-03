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
