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
import com.vecsight.dragonite.sdk.exception.InvalidValueException;

import java.net.InetSocketAddress;

public class ProxyClientConfig {

    private InetSocketAddress remoteAddress;

    private int socks5port;

    private String password;

    private short downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ProxyClientConfig(final InetSocketAddress remoteAddress, final int socks5port, final String password, final short downMbps, final short upMbps) {
        this.remoteAddress = remoteAddress;
        this.socks5port = socks5port;
        this.password = password;
        this.downMbps = downMbps;
        this.upMbps = upMbps;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getSocks5port() {
        return socks5port;
    }

    public void setSocks5port(final int socks5port) {
        this.socks5port = socks5port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public short getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(final short downMbps) {
        this.downMbps = downMbps;
    }

    public short getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(final short upMbps) {
        this.upMbps = upMbps;
    }

    public int getMTU() {
        return dragoniteSocketParameters.getPacketSize();
    }

    public void setMTU(final int mtu) throws InvalidValueException {
        dragoniteSocketParameters.setPacketSize(mtu);
    }

    public int getWindowMultiplier() {
        return dragoniteSocketParameters.getWindowMultiplier();
    }

    public void setWindowMultiplier(final int mult) throws InvalidValueException {
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
