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

import com.vecsight.dragonite.DragoniteBuildConfig;

public final class DragoniteGlobalConstants {

    //Version shits

    public static final String LIBRARY_VERSION = DragoniteBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 1;

    //Send window shits

    public static final int MIN_SEND_WINDOW_SIZE = 20;

    //RTT and resend shits

    public static final int ACK_INTERVAL_MS = 10;

    public static final int MAX_FAST_RESEND_COUNT = 3;

    public static final int MAX_SLOW_RESEND_MULT = 4;

    //public static final int MIN_RESEND_WAIT_MS = 2;
    //Replaced with ackDelayCompensation

    public static final int INIT_RTT_MS = 200, RTT_MAX_VARIATION_MS = 200, RTT_UPDATE_INTERVAL_MS = 100, RTT_RESEND_CORRECTION_INTERVAL_MS = 2000;

    public static final int DEV_RTT_MULT = 4;

    public static final float RTT_RESENDED_REFRESH_MAX_MULT = 1.5f;

    //Close shits

    public static final int MIN_CLOSE_WAIT_MS = 100, CLOSE_WAIT_RTT_MULT = 4;

    //Web panel shits

    public static final int WEB_PANEL_PORT = 8000;

}
