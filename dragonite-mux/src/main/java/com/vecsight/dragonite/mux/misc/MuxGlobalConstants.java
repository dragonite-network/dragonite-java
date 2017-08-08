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

package com.vecsight.dragonite.mux.misc;


import com.vecsight.dragonite.MuxBuildConfig;

public final class MuxGlobalConstants {

    //Version shits

    public static final String LIBRARY_VERSION = MuxBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 2;

    //Buffer shits

    public static final int CONNECTION_MAX_DATA_BUFFER_SIZE = 1048576; //1M

}
