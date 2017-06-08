package com.vecsight.dragonite.sdk.socket;

import java.net.SocketAddress;

public class DragoniteSocketStatistics {

    private final SocketAddress remoteAddress;

    private final String description;

    private final long sendLength, sendRawLength, readLength, receiveRawLength, estimatedRTT, devRTT;

    private final float resendRate, duplicateRate;

    public DragoniteSocketStatistics(SocketAddress remoteAddress, String description, long sendLength, long sendRawLength, long readLength, long receiveRawLength, long estimatedRTT, long devRTT, float resendRate, float duplicateRate) {
        this.remoteAddress = remoteAddress;
        this.description = description;
        this.sendLength = sendLength;
        this.sendRawLength = sendRawLength;
        this.readLength = readLength;
        this.receiveRawLength = receiveRawLength;
        this.estimatedRTT = estimatedRTT;
        this.devRTT = devRTT;
        this.resendRate = resendRate;
        this.duplicateRate = duplicateRate;
    }

    public long getSendLength() {
        return sendLength;
    }

    public long getSendRawLength() {
        return sendRawLength;
    }

    public long getReadLength() {
        return readLength;
    }

    public long getReceiveRawLength() {
        return receiveRawLength;
    }

    public long getEstimatedRTT() {
        return estimatedRTT;
    }

    public long getDevRTT() {
        return devRTT;
    }

    public float getResendRate() {
        return resendRate;
    }

    public float getDuplicateRate() {
        return duplicateRate;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getDescription() {
        return description;
    }
}
