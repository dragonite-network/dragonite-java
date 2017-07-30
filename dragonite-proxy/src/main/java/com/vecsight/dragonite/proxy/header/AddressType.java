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

package com.vecsight.dragonite.proxy.header;

public enum AddressType {
    IPv4(0),
    IPv6(1),
    DOMAIN(2);

    private final int value;

    AddressType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final AddressType[] types = AddressType.values();

    public static AddressType fromInteger(int group) {
        return types[group];
    }
}
