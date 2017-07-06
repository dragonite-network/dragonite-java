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

package com.vecsight.dragonite.sdk.misc;

public final class NumUtils {

    public static long min(final long l1, final long l2) {
        return l1 < l2 ? l1 : l2;
    }

    public static long max(final long l1, final long l2) {
        return l1 > l2 ? l1 : l2;
    }

    public static int min(final int i1, final int i2) {
        return i1 < i2 ? i1 : i2;
    }

    public static int max(final int i1, final int i2) {
        return i1 > i2 ? i1 : i2;
    }

}
