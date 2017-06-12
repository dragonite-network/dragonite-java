package com.vecsight.dragonite.forwarder.config;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.InvalidValueException;

import java.net.InetSocketAddress;

public class ForwarderClientConfig {

    private InetSocketAddress remoteAddress;

    private int localPort;

    private short downMbps, upMbps;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ForwarderClientConfig(InetSocketAddress remoteAddress, int localPort, short downMbps, short upMbps) {
        this.remoteAddress = remoteAddress;
        this.localPort = localPort;
        this.downMbps = downMbps;
        this.upMbps = upMbps;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public short getDownMbps() {
        return downMbps;
    }

    public void setDownMbps(short downMbps) {
        this.downMbps = downMbps;
    }

    public short getUpMbps() {
        return upMbps;
    }

    public void setUpMbps(short upMbps) {
        this.upMbps = upMbps;
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
