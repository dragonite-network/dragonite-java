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

package com.vecsight.dragonite.proxy.config;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;
import static com.vecsight.dragonite.utils.flow.Preconditions.inPortRange;

public class ProxyClientConfig {

    private InetSocketAddress remoteAddress;

    private int socks5port;

    private String password;

    private short downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ProxyClientConfig(final InetSocketAddress remoteAddress, final int socks5port, final String password, final short downMbps, final short upMbps) {
        setRemoteAddress(remoteAddress);
        setSocks5port(socks5port);
        setPassword(password);
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

    public int getSocks5port() {
        return socks5port;
    }

    public void setSocks5port(final int socks5port) {
        checkArgument(inPortRange(socks5port), "Invalid port");
        this.socks5port = socks5port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        checkArgument(password != null, "Invalid password");
        this.password = password;
    }

    public short getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(final short downMbps) {
        checkArgument(downMbps > 0, "Invalid Mbps");
        this.downMbps = downMbps;
    }

    public short getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(final short upMbps) {
        checkArgument(upMbps > 0, "Invalid Mbps");
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
