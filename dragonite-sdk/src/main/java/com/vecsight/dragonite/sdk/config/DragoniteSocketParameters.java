package com.vecsight.dragonite.sdk.config;

import com.vecsight.dragonite.sdk.exception.InvalidValueException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DragoniteSocketParameters {

    private int packetSize = 1300;

    private boolean autoSplit = true;

    private int maxPacketBufferSize = 0;

    private int aggressiveWindowMultiplier = 2;

    private int passiveWindowMultiplier = 5;

    private int ackIntervalMS = 10;

    private int resendMinDelayMS = 50;

    private int heartbeatIntervalSec = 5;

    private int receiveTimeoutSec = 10;

    private boolean enableWebPanel = false;

    private InetSocketAddress webPanelBindAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), DragoniteGlobalConstants.WEB_PANEL_PORT);

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) throws InvalidValueException {
        if (packetSize < 100) {
            throw new InvalidValueException("Packet size is too small");
        }
        this.packetSize = packetSize;
    }

    public boolean isAutoSplit() {
        return autoSplit;
    }

    public void setAutoSplit(boolean autoSplit) {
        this.autoSplit = autoSplit;
    }

    public int getMaxPacketBufferSize() {
        return maxPacketBufferSize;
    }

    public void setMaxPacketBufferSize(int maxPacketBufferSize) throws InvalidValueException {
        if (maxPacketBufferSize < 1) {
            throw new InvalidValueException("Receive window must be greater than zero");
        }
        this.maxPacketBufferSize = maxPacketBufferSize;
    }

    public int getAggressiveWindowMultiplier() {
        return aggressiveWindowMultiplier;
    }

    public void setAggressiveWindowMultiplier(int aggressiveWindowMultiplier) throws InvalidValueException {
        if (aggressiveWindowMultiplier < 1) {
            throw new InvalidValueException("Aggressive window must be greater than zero");
        }
        this.aggressiveWindowMultiplier = aggressiveWindowMultiplier;
    }

    public int getPassiveWindowMultiplier() {
        return passiveWindowMultiplier;
    }

    public void setPassiveWindowMultiplier(int passiveWindowMultiplier) throws InvalidValueException {
        if (passiveWindowMultiplier < 1) {
            throw new InvalidValueException("Passive window must be greater than zero");
        }
        this.passiveWindowMultiplier = passiveWindowMultiplier;
    }

    public int getAckIntervalMS() {
        return ackIntervalMS;
    }

    public void setAckIntervalMS(int ackIntervalMS) throws InvalidValueException {
        if (ackIntervalMS < 1) {
            throw new InvalidValueException("ACK interval must be greater than zero");
        }
        this.ackIntervalMS = ackIntervalMS;
    }

    public int getResendMinDelayMS() {
        return resendMinDelayMS;
    }

    public void setResendMinDelayMS(int resendMinDelayMS) throws InvalidValueException {
        if (resendMinDelayMS < 1) {
            throw new InvalidValueException("Resend delay must be greater than zero");
        }
        this.resendMinDelayMS = resendMinDelayMS;
    }

    public int getHeartbeatIntervalSec() {
        return heartbeatIntervalSec;
    }

    public void setHeartbeatIntervalSec(int heartbeatIntervalSec) throws InvalidValueException {
        if (heartbeatIntervalSec < 1) {
            throw new InvalidValueException("Heartbeat interval must be greater than zero");
        }
        this.heartbeatIntervalSec = heartbeatIntervalSec;
    }

    public int getReceiveTimeoutSec() {
        return receiveTimeoutSec;
    }

    public void setReceiveTimeoutSec(int receiveTimeoutSec) throws InvalidValueException {
        if (receiveTimeoutSec < 1) {
            throw new InvalidValueException("Receive timeout must be greater than zero");
        }
        this.receiveTimeoutSec = receiveTimeoutSec;
    }

    public boolean isEnableWebPanel() {
        return enableWebPanel;
    }

    public void setEnableWebPanel(boolean enableWebPanel) {
        this.enableWebPanel = enableWebPanel;
    }

    public InetSocketAddress getWebPanelBindAddress() {
        return webPanelBindAddress;
    }

    public void setWebPanelBindAddress(InetSocketAddress webPanelBindAddress) {
        this.webPanelBindAddress = webPanelBindAddress;
    }
}
