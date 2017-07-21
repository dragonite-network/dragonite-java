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
