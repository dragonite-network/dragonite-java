package com.vecsight.dragonite.utils.system;

public final class SystemInfo {

    public static String getUsername() {
        final String name = System.getProperty("user.name");
        return name != null ? name : "Unknown";
    }

    public static String getOS() {
        final String os = System.getProperty("os.name");
        return os != null ? os : "Unknown";
    }

    public static int getProcessorsCount() {
        return Runtime.getRuntime().availableProcessors();
    }

}
