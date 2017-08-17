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

import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.obfs.Obfuscator;
import com.vecsight.dragonite.utils.system.SystemInfo;

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;

public class ProxyServerConfig {

    private InetSocketAddress bindAddress;

    private String password;

    private int mbpsLimit = 0;

    private String welcomeMessage = "Welcome to " + SystemInfo.getHostname();

    private boolean allowLoopback = false;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ProxyServerConfig(final InetSocketAddress bindAddress, final String password) {
        setBindAddress(bindAddress);
        setPassword(password);
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetSocketAddress bindAddress) {
        checkArgument(bindAddress != null, "Invalid bind address");
        this.bindAddress = bindAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        checkArgument(password != null && password.length() >= ProxyGlobalConstants.PASSWORD_MIN_LENGTH, "Invalid password");
        this.password = password;
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

    public boolean isAllowLoopback() {
        return allowLoopback;
    }

    public void setAllowLoopback(final boolean allowLoopback) {
        this.allowLoopback = allowLoopback;
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

    public void setObfuscator(final Obfuscator obfuscator) {
        dragoniteSocketParameters.setObfuscator(obfuscator);
    }

    public Obfuscator getObfuscator() {
        return dragoniteSocketParameters.getObfuscator();
    }
}
