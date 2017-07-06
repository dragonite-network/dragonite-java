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

package com.vecsight.dragonite.sdk.config;

import com.vecsight.dragonite.sdk.exception.InvalidValueException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DragoniteSocketParameters {

    private int packetSize = 1300;

    private boolean autoSplit = true;

    private int maxPacketBufferSize = 0;

    private int windowMultiplier = 4;

    private int resendMinDelayMS = 50;

    private int heartbeatIntervalSec = 5;

    private int receiveTimeoutSec = 10;

    private boolean enableWebPanel = false;

    private InetSocketAddress webPanelBindAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), DragoniteGlobalConstants.WEB_PANEL_PORT);

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(final int packetSize) throws InvalidValueException {
        if (packetSize < 100) {
            throw new InvalidValueException("Packet size is too small");
        }
        this.packetSize = packetSize;
    }

    public boolean isAutoSplit() {
        return autoSplit;
    }

    public void setAutoSplit(final boolean autoSplit) {
        this.autoSplit = autoSplit;
    }

    public int getMaxPacketBufferSize() {
        return maxPacketBufferSize;
    }

    public void setMaxPacketBufferSize(final int maxPacketBufferSize) throws InvalidValueException {
        if (maxPacketBufferSize < 1) {
            throw new InvalidValueException("Receive window must be greater than zero");
        }
        this.maxPacketBufferSize = maxPacketBufferSize;
    }

    public int getWindowMultiplier() {
        return windowMultiplier;
    }

    public void setWindowMultiplier(final int windowMultiplier) throws InvalidValueException {
        if (windowMultiplier < 1 || windowMultiplier > 10) {
            throw new InvalidValueException("Multiplier must be greater than zero (and no more than 10)");
        }
        this.windowMultiplier = windowMultiplier;
    }

    public int getResendMinDelayMS() {
        return resendMinDelayMS;
    }

    public void setResendMinDelayMS(final int resendMinDelayMS) throws InvalidValueException {
        if (resendMinDelayMS < 1) {
            throw new InvalidValueException("Resend delay must be greater than zero");
        }
        this.resendMinDelayMS = resendMinDelayMS;
    }

    public int getHeartbeatIntervalSec() {
        return heartbeatIntervalSec;
    }

    public void setHeartbeatIntervalSec(final int heartbeatIntervalSec) throws InvalidValueException {
        if (heartbeatIntervalSec < 1) {
            throw new InvalidValueException("Heartbeat interval must be greater than zero");
        }
        this.heartbeatIntervalSec = heartbeatIntervalSec;
    }

    public int getReceiveTimeoutSec() {
        return receiveTimeoutSec;
    }

    public void setReceiveTimeoutSec(final int receiveTimeoutSec) throws InvalidValueException {
        if (receiveTimeoutSec < 1) {
            throw new InvalidValueException("Receive timeout must be greater than zero");
        }
        this.receiveTimeoutSec = receiveTimeoutSec;
    }

    public boolean isEnableWebPanel() {
        return enableWebPanel;
    }

    public void setEnableWebPanel(final boolean enableWebPanel) {
        this.enableWebPanel = enableWebPanel;
    }

    public InetSocketAddress getWebPanelBindAddress() {
        return webPanelBindAddress;
    }

    public void setWebPanelBindAddress(final InetSocketAddress webPanelBindAddress) {
        this.webPanelBindAddress = webPanelBindAddress;
    }
}
