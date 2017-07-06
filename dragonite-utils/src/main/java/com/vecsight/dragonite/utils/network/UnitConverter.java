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

package com.vecsight.dragonite.utils.network;

public final class UnitConverter {

    public static long mbpsToSpeed(final int mbps) {
        return mbps * 125000;
    }

    public static int speedToMbps(final long speed) {
        return (int) (speed / 125000);
    }

}
