package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.misc.NumUtils;

public class RTTController {

    private final static float estimatedRTTUpdateFactor = 0.125f, devRTTUpdateFactor = 0.25f;

    private final ConnectionSharedData sharedData;

    private long estimatedRTT = DragoniteGlobalConstants.INIT_RTT_MS, devRTT;

    private long lastUpdate = 0;

    private int continuousResendCount = 0;

    public RTTController(final ConnectionSharedData sharedData) {
        this.sharedData = sharedData;
    }

    public void pushInfo(final ResendInfo info) {
        //System.out.println(info);
        if (info.isExist()) {
            final long currentTime = System.currentTimeMillis();
            /*if (currentTime - lastRefresh >= DragoniteGlobalConstants.rttRefreshIntervalMS) {
                lastRefresh = System.currentTimeMillis();
                if (info.isResended()) {
                    long maxCRTT = (long) (estimatedRTT * DragoniteGlobalConstants.RTT_RESENDED_REFRESH_MAX_MULT);
                    setRTT(NumUtils.min(info.getRTT(), maxCRTT), 0);
                }
            }*/
            if (currentTime - lastUpdate >= DragoniteGlobalConstants.RTT_UPDATE_INTERVAL_MS) {
                lastUpdate = currentTime;

                //System.out.println(info.toString());

                if (!info.isResended()) {
                    continuousResendCount = 0;
                    setRTT_limited((long) ((1 - estimatedRTTUpdateFactor) * estimatedRTT + estimatedRTTUpdateFactor * info.getRTT()),
                            (long) ((1 - devRTTUpdateFactor) * devRTT + devRTTUpdateFactor * Math.abs(info.getRTT() - estimatedRTT)));
                } else {
                    continuousResendCount++;
                    if (continuousResendCount > (DragoniteGlobalConstants.RTT_RESEND_CORRECTION_INTERVAL_MS / DragoniteGlobalConstants.RTT_UPDATE_INTERVAL_MS)) {
                        final long maxCRTT = (long) (estimatedRTT * DragoniteGlobalConstants.RTT_RESENDED_REFRESH_MAX_MULT);
                        final long tmpRTT = NumUtils.min(info.getRTT(), maxCRTT);
                        setRTT_limited((long) ((1 - estimatedRTTUpdateFactor) * estimatedRTT + estimatedRTTUpdateFactor * tmpRTT),
                                (long) ((1 - devRTTUpdateFactor) * devRTT + devRTTUpdateFactor * Math.abs(tmpRTT - estimatedRTT)));
                        continuousResendCount = 0;
                    }
                }
            }
        }
    }

    private void setRTT_limited(final long estimatedRTT, final long devRTT) {
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
        sharedData.setEstimatedRTT(estimatedRTT);
        sharedData.setDevRTT(devRTT);
        //System.out.println("Update estimated RTT " + estimatedRTT + " devRTT " + devRTT);
    }

}
