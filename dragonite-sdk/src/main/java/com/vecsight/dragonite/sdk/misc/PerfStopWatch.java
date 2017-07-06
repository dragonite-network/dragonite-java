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

public class PerfStopWatch {

    private final boolean warn;

    private final long warnDelta;

    private volatile long lastTick;

    public PerfStopWatch(final boolean warn, final long warnDelta) {
        this.warn = warn;
        this.warnDelta = warnDelta;
        start();
    }

    private void start() {
        lastTick = System.currentTimeMillis();
    }

    public long tick(final String msg) {
        final long time = System.currentTimeMillis() - lastTick;
        if (warn && time >= warnDelta) {
            System.out.println("[PerfStopWatch] " + msg + " " + time + "ms");
        }
        start();
        return time;
    }

    public long tick() {
        return tick("DEFAULT");
    }
}
