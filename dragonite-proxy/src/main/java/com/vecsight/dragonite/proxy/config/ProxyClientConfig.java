/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.config;

import com.vecsight.dragonite.proxy.acl.ParsedACL;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.cryptor.AESCryptor;
import com.vecsight.dragonite.sdk.exception.EncryptionException;

import java.net.InetSocketAddress;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;
import static com.vecsight.dragonite.utils.flow.Preconditions.inPortRange;

public class ProxyClientConfig {

    private InetSocketAddress remoteAddress;

    private int socks5port;

    private String password;

    private int downMbps, upMbps;

    private ParsedACL acl;

    private final DragoniteSocketParameters dragoniteSocketParameters = new DragoniteSocketParameters();

    public ProxyClientConfig(final InetSocketAddress remoteAddress, final int socks5port, final String password, final int downMbps, final int upMbps) throws EncryptionException {
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

    public void setPassword(final String password) throws EncryptionException {
        checkArgument(password != null && password.length() >= ProxyGlobalConstants.PASSWORD_MIN_LENGTH, "Invalid password");
        dragoniteSocketParameters.setPacketCryptor(new AESCryptor(password));
        this.password = password;
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

    public ParsedACL getAcl() {
        return acl;
    }

    public void setAcl(final ParsedACL acl) {
        this.acl = acl;
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
