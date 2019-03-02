/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

public class MessageStat {

    private boolean exist = false;

    private long RTT;

    private boolean resended;

    public MessageStat(final boolean exist, final long RTT, final boolean resended) {
        this.exist = exist;
        this.RTT = RTT;
        this.resended = resended;
    }

    public MessageStat() {
    }

    @Override
    public String toString() {
        return "MessageStat{" + "exist=" + exist +
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
