/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.network.socks5;

import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;

import java.util.Arrays;

public class SOCKS5Header {

    private boolean isDomain;

    private byte[] addr;

    private int port;

    private boolean udp;

    public SOCKS5Header(final boolean isDomain, final byte[] addr, final int port, final boolean udp) {
        this.isDomain = isDomain;
        this.addr = addr;
        this.port = port;
        this.udp = udp;
    }

    public boolean isDomain() {
        return isDomain;
    }

    public void setDomain(final boolean domain) {
        isDomain = domain;
    }

    public byte[] getAddr() {
        return addr;
    }

    public void setAddr(final byte[] addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public boolean isUdp() {
        return udp;
    }

    public void setUdp(final boolean udp) {
        this.udp = udp;
    }

    @Override
    public String toString() {
        return "SOCKS5Header{" +
                "isDomain=" + isDomain +
                ", addr=" + (isDomain ? new String(addr, ProxyGlobalConstants.HEADER_ADDRESS_CHARSET) : Arrays.toString(addr)) +
                ", port=" + port +
                ", udp=" + udp +
                '}';
    }
}
