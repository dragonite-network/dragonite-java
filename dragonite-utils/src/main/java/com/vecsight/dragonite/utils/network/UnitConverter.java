package com.vecsight.dragonite.utils.network;

public final class UnitConverter {

    public static long mbpsToSpeed(final int mbps) {
        return mbps * 125000;
    }

    public static int speedToMbps(final long speed) {
        return (int) (speed / 125000);
    }

}
