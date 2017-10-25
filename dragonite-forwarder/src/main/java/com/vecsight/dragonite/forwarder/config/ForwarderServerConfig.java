/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.config;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.utils.system.SystemInfo;

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;

public class ForwarderServerConfig {

    private InetSocketAddress bindAddress;

    private InetSocketAddress forwardingAddress;

    private int mbpsLimit = 0;

    private String welcomeMessage = "Welcome to " + SystemInfo.getHostname();

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ForwarderServerConfig(final InetSocketAddress bindAddress, final InetSocketAddress forwardingAddress) {
        setBindAddress(bindAddress);
        setForwardingAddress(forwardingAddress);
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetSocketAddress bindAddress) {
        checkArgument(bindAddress != null, "Invalid bind address");
        this.bindAddress = bindAddress;
    }

    public InetSocketAddress getForwardingAddress() {
        return forwardingAddress;
    }

    public void setForwardingAddress(final InetSocketAddress forwardingAddress) {
        this.forwardingAddress = forwardingAddress;
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
