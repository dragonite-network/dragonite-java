package com.vecsight.dragonite.forwarder.config;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.InvalidValueException;

import java.net.InetSocketAddress;

public class ForwarderServerConfig {

    private InetSocketAddress bindAddress;

    private int forwardingPort;

    private short mbpsLimit = 0;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ForwarderServerConfig(InetSocketAddress bindAddress, int forwardingPort) {
        this.bindAddress = bindAddress;
        this.forwardingPort = forwardingPort;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getForwardingPort() {
        return forwardingPort;
    }

    public void setForwardingPort(int forwardingPort) {
        this.forwardingPort = forwardingPort;
    }

    public short getMbpsLimit() {
        return mbpsLimit;
    }

    public void setMbpsLimit(short mbpsLimit) {
        this.mbpsLimit = mbpsLimit;
    }

    public int getMTU() {
        return dragoniteSocketParameters.getPacketSize();
    }

    public void setMTU(int mtu) throws InvalidValueException {
        dragoniteSocketParameters.setPacketSize(mtu);
    }

    public boolean getWebPanelEnabled() {
        return dragoniteSocketParameters.isEnableWebPanel();
    }

    public void setWebPanelEnabled(boolean enabled) {
        dragoniteSocketParameters.setEnableWebPanel(enabled);
    }

    public InetSocketAddress getWebPanelBind() {
        return dragoniteSocketParameters.getWebPanelBindAddress();
    }

    public void setWebPanelBind(InetSocketAddress address) {
        dragoniteSocketParameters.setWebPanelBindAddress(address);
    }

    public DragoniteSocketParameters getDragoniteSocketParameters() {
        return dragoniteSocketParameters;
    }
}
