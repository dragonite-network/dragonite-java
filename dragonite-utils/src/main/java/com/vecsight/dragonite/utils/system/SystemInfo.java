/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.utils.system;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class SystemInfo {

    public static String getUsername() {
        final String name = System.getProperty("user.name");
        return name != null ? name : "Unknown";
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            return "Unknown";
        }
    }

    public static String getOS() {
        final String os = System.getProperty("os.name");
        return os != null ? os : "Unknown";
    }

    public static int getProcessorsCount() {
        return Runtime.getRuntime().availableProcessors();
    }

}
