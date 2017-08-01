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

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;
import static com.vecsight.dragonite.utils.flow.Preconditions.inPortRange;

public class ForwarderClientConfig {

    private InetSocketAddress remoteAddress;

    private int localPort;

    private int downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ForwarderClientConfig(final InetSocketAddress remoteAddress, final int localPort, final int downMbps, final int upMbps) {
        setRemoteAddress(remoteAddress);
        setLocalPort(localPort);
        setDownMbps(downMbps);
        setUpMbps(upMbps);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final InetSocketAddress remoteAddress) {
        checkArgument(remoteAddress != null, "Invalid remote address");
        this.remoteAddress = remoteAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(final int localPort) {
        checkArgument(inPortRange(localPort), "Invalid port");
        this.localPort = localPort;
    }

    public int getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(final int downMbps) {
        checkArgument(downMbps > 0 && downMbps <= 65535, "Invalid Mbps");
        this.downMbps = downMbps;
    }

    public int getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(final int upMbps) {
        checkArgument(upMbps > 0 && upMbps <= 65535, "Invalid Mbps");
        this.upMbps = upMbps;
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
