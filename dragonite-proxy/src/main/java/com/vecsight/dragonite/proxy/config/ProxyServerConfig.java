/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.config;

import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.cryptor.AESCryptor;
import com.vecsight.dragonite.sdk.exception.EncryptionException;
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

    public ProxyServerConfig(final InetSocketAddress bindAddress, final String password) throws EncryptionException {
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

    public void setPassword(final String password) throws EncryptionException {
        checkArgument(password != null && password.length() >= ProxyGlobalConstants.PASSWORD_MIN_LENGTH, "Invalid password");
        dragoniteSocketParameters.setPacketCryptor(new AESCryptor(password));
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

    public int getTrafficClass() {
        return dragoniteSocketParameters.getTrafficClass();
    }

    public void setTrafficClass(final int tc) {
        dragoniteSocketParameters.setTrafficClass(tc);
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
