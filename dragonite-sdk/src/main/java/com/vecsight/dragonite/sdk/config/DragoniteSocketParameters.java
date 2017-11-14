/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.config;

import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;
import static com.vecsight.dragonite.utils.flow.Preconditions.inTrafficClassRange;

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

    private PacketCryptor packetCryptor = null;

    private int trafficClass = 0;

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(final int packetSize) {
        checkArgument(packetSize >= 200, "Packet size is too small");
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

    public void setMaxPacketBufferSize(final int maxPacketBufferSize) {
        checkArgument(maxPacketBufferSize >= 1, "Receive window must be greater than zero");
        this.maxPacketBufferSize = maxPacketBufferSize;
    }

    public int getWindowMultiplier() {
        return windowMultiplier;
    }

    public void setWindowMultiplier(final int windowMultiplier) {
        checkArgument(windowMultiplier >= 1 && windowMultiplier <= 10, "Multiplier must be greater than zero (and no more than 10)");
        this.windowMultiplier = windowMultiplier;
    }

    public int getResendMinDelayMS() {
        return resendMinDelayMS;
    }

    public void setResendMinDelayMS(final int resendMinDelayMS) {
        checkArgument(resendMinDelayMS >= 1, "Resend delay must be greater than zero");
        this.resendMinDelayMS = resendMinDelayMS;
    }

    public int getHeartbeatIntervalSec() {
        return heartbeatIntervalSec;
    }

    public void setHeartbeatIntervalSec(final int heartbeatIntervalSec) {
        checkArgument(heartbeatIntervalSec >= 1, "Heartbeat interval must be greater than zero");
        this.heartbeatIntervalSec = heartbeatIntervalSec;
    }

    public int getReceiveTimeoutSec() {
        return receiveTimeoutSec;
    }

    public void setReceiveTimeoutSec(final int receiveTimeoutSec) {
        checkArgument(receiveTimeoutSec >= 1, "Receive timeout must be greater than zero");
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
        checkArgument(webPanelBindAddress != null, "Bind address cannot be null");
        this.webPanelBindAddress = webPanelBindAddress;
    }

    public PacketCryptor getPacketCryptor() {
        return packetCryptor;
    }

    public void setPacketCryptor(final PacketCryptor packetCryptor) {
        this.packetCryptor = packetCryptor;
    }

    public int getTrafficClass() {
        return trafficClass;
    }

    public void setTrafficClass(final int trafficClass) {
        checkArgument(inTrafficClassRange(trafficClass), "TC must be in the range 0 <= tc <= 255");
        this.trafficClass = trafficClass;
    }
}
