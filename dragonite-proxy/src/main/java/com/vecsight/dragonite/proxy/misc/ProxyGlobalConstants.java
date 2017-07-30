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

package com.vecsight.dragonite.proxy.misc;

import com.vecsight.dragonite.ProxyBuildConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ProxyGlobalConstants {

    //Version shits

    public static final String APP_VERSION = ProxyBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 1;

    public static final int DEFAULT_SERVER_PORT = 5234;

    public static final long INIT_SEND_SPEED = 100 * 1024; //100kb/s

    public static final Charset STRING_CHARSET = StandardCharsets.UTF_8;

    //Sizes

    public static final short PIPE_BUFFER_SIZE = 10240;

    public static final short MAX_FRAME_SIZE = 20480;

}
