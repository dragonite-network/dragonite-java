/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.utils.flow;

public final class Preconditions {

    public static void checkArgument(final boolean ok, final String message) {
        if (!ok) throw new IllegalArgumentException(message);
    }

    public static boolean inPortRange(final int port) {
        return port > 0 && port <= 65535;
    }

    public static boolean inTrafficClassRange(final int trafficClass) {
        return trafficClass >= 0 && trafficClass <= 255;
    }

}
