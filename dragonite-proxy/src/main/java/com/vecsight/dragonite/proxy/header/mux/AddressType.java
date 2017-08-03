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
