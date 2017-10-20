/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
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
