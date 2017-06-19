package com.vecsight.dragonite.sdk.socket;

public class ResendInfo {

    private boolean exist = false;

    private long RTT;

    private boolean resended;

    public ResendInfo(boolean exist, long RTT, boolean resended) {
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

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public long getRTT() {
        return RTT;
    }

    public void setRTT(long RTT) {
        this.RTT = RTT;
    }

    public boolean isResended() {
        return resended;
    }

    public void setResended(boolean resended) {
        this.resended = resended;
    }

}
