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

package com.vecsight.dragonite.mux.frame;

public final class FrameType {

    public static final byte CREATE = 0;

    public static final byte DATA = 1;

    public static final byte CLOSE = 2;

    public static final byte PAUSE = 3;

    public static final byte CONTINUE = 4;

}
