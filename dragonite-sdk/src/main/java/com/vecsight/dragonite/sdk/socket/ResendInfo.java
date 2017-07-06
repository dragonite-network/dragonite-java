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

public class ResendInfo {

    private boolean exist = false;

    private long RTT;

    private boolean resended;

    public ResendInfo(final boolean exist, final long RTT, final boolean resended) {
        this.exist = exist;
        this.RTT = RTT;
        this.resended = resended;
    }

    public ResendInfo() {
    }

    @Override
    public String toString() {
        return "ResendInfo{" + "exist=" + exist +
                ", RTT=" + RTT +
                ", resended=" + resended +
                '}';
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(final boolean exist) {
        this.exist = exist;
    }

    public long getRTT() {
        return RTT;
    }

    public void setRTT(final long RTT) {
        this.RTT = RTT;
    }

    public boolean isResended() {
        return resended;
    }

    public void setResended(final boolean resended) {
        this.resended = resended;
    }

}
