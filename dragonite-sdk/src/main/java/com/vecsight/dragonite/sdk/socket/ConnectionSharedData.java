package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;

public class ConnectionSharedData {

    private volatile long estimatedRTT = DragoniteGlobalConstants.INIT_RTT;

    private volatile long devRTT = 0;

    private volatile int sendSequence = 0;

    protected final Object sendWindowLock = new Object();

    public long getEstimatedRTT() {
        return estimatedRTT;
    }

    protected void setEstimatedRTT(long estimatedRTT) {
        this.estimatedRTT = estimatedRTT;
    }

    public long getDevRTT() {
        return devRTT;
    }

    public void setDevRTT(long devRTT) {
        this.devRTT = devRTT;
    }

    public int getSendSequence() {
        return sendSequence;
    }

    public void setSendSequence(int sendSequence) {
        this.sendSequence = sendSequence;
    }
}
