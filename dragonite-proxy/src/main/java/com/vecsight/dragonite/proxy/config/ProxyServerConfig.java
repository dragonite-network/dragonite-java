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
import com.vecsight.dragonite.utils.system.SystemInfo;

import java.net.InetSocketAddress;

public class ProxyServerConfig {

    private InetSocketAddress bindAddress;

    private String password;

    private short mbpsLimit = 0;

    private String welcomeMessage = "Welcome to " + SystemInfo.getHostname();

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ProxyServerConfig(final InetSocketAddress bindAddress, final String password) {
        this.bindAddress = bindAddress;
        this.password = password;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public short getMbpsLimit() {
        return mbpsLimit;
    }

    public void setMbpsLimit(final short mbpsLimit) {
        this.mbpsLimit = mbpsLimit;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(final String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
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
