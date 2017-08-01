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

package com.vecsight.dragonite.utils.flow;

public final class Preconditions {

    public static void checkArgument(final boolean ok, final String message) {
        if (!ok) throw new IllegalArgumentException(message);
    }

    public static boolean inPortRange(final int port) {
        return port > 0 && port <= 65535;
    }

}
