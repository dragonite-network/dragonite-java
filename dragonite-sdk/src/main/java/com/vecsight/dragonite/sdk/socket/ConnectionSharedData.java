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

package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;

public class ConnectionSharedData {

    private volatile long estimatedRTT = DragoniteGlobalConstants.INIT_RTT_MS;

    private volatile long devRTT = 0;

    private volatile int sendSequence = 0;

    protected final Object sendWindowLock = new Object();

    public long getEstimatedRTT() {
        return estimatedRTT;
    }

    protected void setEstimatedRTT(final long estimatedRTT) {
        this.estimatedRTT = estimatedRTT;
    }

    public long getDevRTT() {
        return devRTT;
    }

    public void setDevRTT(final long devRTT) {
        this.devRTT = devRTT;
    }

    public int getSendSequence() {
        return sendSequence;
    }

    public void setSendSequence(final int sendSequence) {
        this.sendSequence = sendSequence;
    }
}
