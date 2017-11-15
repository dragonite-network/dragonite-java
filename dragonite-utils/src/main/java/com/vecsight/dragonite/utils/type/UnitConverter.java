/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.utils.type;

public final class UnitConverter {

    public static long mbpsToSpeed(final int mbps) {
        return mbps * 125000L;
    }

    public static int speedToMbps(final long speed) {
        return (int) (speed / 125000);
    }

    public static int DSCPtoTrafficClass(final int dscp) {
        return dscp << 2;
    }

}
