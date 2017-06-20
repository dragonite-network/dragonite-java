package com.vecsight.dragonite.sdk.socket;

import java.net.SocketAddress;

public class DragoniteSocketStatistics {

    private final SocketAddress remoteAddress;

    private final String description;

    private final long sendLength, sendRawLength, readLength, receiveRawLength, estimatedRTT, devRTT;

    private final long sendCount, resendCount, receiveCount, dupCount;

    public DragoniteSocketStatistics(final SocketAddress remoteAddress, final String description, final long sendLength, final long sendRawLength, final long readLength, final long receiveRawLength, final long estimatedRTT, final long devRTT, final long sendCount, final long resendCount, final long receiveCount, final long dupCount) {
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
