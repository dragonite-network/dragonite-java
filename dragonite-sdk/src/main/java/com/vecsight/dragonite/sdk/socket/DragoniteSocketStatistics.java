package com.vecsight.dragonite.sdk.socket;

public class DragoniteSocketStatistics {

    private final long sendLength, sendRawLength, readLength, receiveRawLength, estimatedRTT, devRTT;

    public DragoniteSocketStatistics(long sendLength, long sendRawLength, long readLength, long receiveRawLength, long estimatedRTT, long devRTT) {
        this.sendLength = sendLength;
        this.sendRawLength = sendRawLength;
        this.readLength = readLength;
        this.receiveRawLength = receiveRawLength;
        this.estimatedRTT = estimatedRTT;
        this.devRTT = devRTT;
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
}
