/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.misc.NumUtils;

public class RTTEstimator {

    private final static float estimatedRTTUpdateFactor = 0.125f, devRTTUpdateFactor = 0.25f;

    private final ConnectionState state;

    private long estimatedRTT = DragoniteGlobalConstants.INIT_RTT_MS, devRTT;

    private long lastUpdate = 0;

    private int continuousResendCount = 0;

    public RTTEstimator(final ConnectionState state) {
        this.state = state;
    }

    public void pushStat(final MessageStat stat) {
        //System.out.println(stat);
        if (stat.isExist()) {
            final long currentTime = System.currentTimeMillis();
            /*if (currentTime - lastRefresh >= DragoniteGlobalConstants.rttRefreshIntervalMS) {
                lastRefresh = System.currentTimeMillis();
                if (stat.isResended()) {
                    long maxCRTT = (long) (estimatedRTT * DragoniteGlobalConstants.RTT_RESENDED_REFRESH_MAX_MULT);
                    setRTT(NumUtils.min(stat.getRTT(), maxCRTT), 0);
                }
            }*/
            if (currentTime - lastUpdate >= DragoniteGlobalConstants.RTT_UPDATE_INTERVAL_MS) {
                lastUpdate = currentTime;

                //System.out.println(stat.toString());

                if (!stat.isResended()) {
                    continuousResendCount = 0;
                    clampSetRTT((long) ((1 - estimatedRTTUpdateFactor) * estimatedRTT + estimatedRTTUpdateFactor * stat.getRTT()),
                            (long) ((1 - devRTTUpdateFactor) * devRTT + devRTTUpdateFactor * Math.abs(stat.getRTT() - estimatedRTT)));
                } else {
                    continuousResendCount++;
                    if (continuousResendCount > (DragoniteGlobalConstants.RTT_RESEND_CORRECTION_INTERVAL_MS / DragoniteGlobalConstants.RTT_UPDATE_INTERVAL_MS)) {
                        final long maxCRTT = (long) (estimatedRTT * DragoniteGlobalConstants.RTT_RESENDED_REFRESH_MAX_MULT);
                        final long tmpRTT = NumUtils.min(stat.getRTT(), maxCRTT);
                        clampSetRTT((long) ((1 - estimatedRTTUpdateFactor) * estimatedRTT + estimatedRTTUpdateFactor * tmpRTT),
                                (long) ((1 - devRTTUpdateFactor) * devRTT + devRTTUpdateFactor * Math.abs(tmpRTT - estimatedRTT)));
                        continuousResendCount = 0;
                    }
                }
            }
        }
    }

    private void clampSetRTT(final long estimatedRTT, final long devRTT) {
        long tempDevRTT = devRTT * DragoniteGlobalConstants.DEV_RTT_MULT;
        if (tempDevRTT > estimatedRTT && tempDevRTT > DragoniteGlobalConstants.RTT_MAX_VARIATION_MS) {
            tempDevRTT = estimatedRTT;
        }
        tempDevRTT /= DragoniteGlobalConstants.DEV_RTT_MULT;
        setRTT(estimatedRTT, tempDevRTT);
    }

    private void setRTT(final long estimatedRTT, final long devRTT) {
        this.estimatedRTT = estimatedRTT;
        this.devRTT = devRTT;
        state.setEstimatedRTT(estimatedRTT);
        state.setDevRTT(devRTT);
        //System.out.println("Update estimated RTT " + estimatedRTT + " devRTT " + devRTT);
    }

}
