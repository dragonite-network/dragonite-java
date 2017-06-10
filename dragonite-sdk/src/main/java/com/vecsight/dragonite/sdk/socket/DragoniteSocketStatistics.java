package com.vecsight.dragonite.sdk.socket;

import java.net.SocketAddress;

public class DragoniteSocketStatistics {

    private final SocketAddress remoteAddress;

    private final String description;

    private final long sendLength, sendRawLength, readLength, receiveRawLength, estimatedRTT, devRTT;

    private final long sendCount, resendCount, receiveCount, dupCount;

    public DragoniteSocketStatistics(SocketAddress remoteAddress, String description, long sendLength, long sendRawLength, long readLength, long receiveRawLength, long estimatedRTT, long devRTT, long sendCount, long resendCount, long receiveCount, long dupCount) {
        this.remoteAddress = remoteAddress;
        this.description = description;
        this.sendLength = sendLength;
        this.sendRawLength = sendRawLength;
        this.readLength = readLength;
        this.receiveRawLength = receiveRawLength;
        this.estimatedRTT = estimatedRTT;
        this.devRTT = devRTT;
        this.sendCount = sendCount;
        this.resendCount = resendCount;
        this.receiveCount = receiveCount;
        this.dupCount = dupCount;
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

    public long getSendCount() {
        return sendCount;
    }

    public long getResendCount() {
        return resendCount;
    }

    public long getReceiveCount() {
        return receiveCount;
    }

    public long getDupCount() {
        return dupCount;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getDescription() {
        return description;
    }
}
