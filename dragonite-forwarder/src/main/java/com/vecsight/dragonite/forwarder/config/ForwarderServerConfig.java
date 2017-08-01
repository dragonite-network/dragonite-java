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

package com.vecsight.dragonite.forwarder.config;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.utils.system.SystemInfo;

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;
import static com.vecsight.dragonite.utils.flow.Preconditions.inPortRange;

public class ForwarderServerConfig {

    private InetSocketAddress bindAddress;

    private int forwardingPort;

    private int mbpsLimit = 0;

    private String welcomeMessage = "Welcome to " + SystemInfo.getHostname();

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ForwarderServerConfig(final InetSocketAddress bindAddress, final int forwardingPort) {
        setBindAddress(bindAddress);
        setForwardingPort(forwardingPort);
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetSocketAddress bindAddress) {
        checkArgument(bindAddress != null, "Invalid bind address");
        this.bindAddress = bindAddress;
    }

    public int getForwardingPort() {
        return forwardingPort;
    }

    public void setForwardingPort(final int forwardingPort) {
        checkArgument(inPortRange(forwardingPort), "Invalid port");
        this.forwardingPort = forwardingPort;
    }

    public int getMbpsLimit() {
        return mbpsLimit;
    }

    public void setMbpsLimit(final int mbpsLimit) {
        checkArgument(mbpsLimit > 0 && mbpsLimit <= 65535, "Invalid Mbps");
        this.mbpsLimit = mbpsLimit;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(final String welcomeMessage) {
        checkArgument(welcomeMessage != null, "Null welcome message");
        this.welcomeMessage = welcomeMessage;
    }

    public int getMTU() {
        return dragoniteSocketParameters.getPacketSize();
    }

    public void setMTU(final int mtu) {
        dragoniteSocketParameters.setPacketSize(mtu);
    }

    public int getWindowMultiplier() {
        return dragoniteSocketParameters.getWindowMultiplier();
    }

    public void setWindowMultiplier(final int mult) {
        dragoniteSocketParameters.setWindowMultiplier(mult);
    }

    public boolean getWebPanelEnabled() {
        return dragoniteSocketParameters.isEnableWebPanel();
    }

    public void setWebPanelEnabled(final boolean enabled) {
        dragoniteSocketParameters.setEnableWebPanel(enabled);
    }

    public InetSocketAddress getWebPanelBind() {
        return dragoniteSocketParameters.getWebPanelBindAddress();
    }

    public void setWebPanelBind(final InetSocketAddress address) {
        dragoniteSocketParameters.setWebPanelBindAddress(address);
    }

    public DragoniteSocketParameters getDragoniteSocketParameters() {
        return dragoniteSocketParameters;
    }
}
