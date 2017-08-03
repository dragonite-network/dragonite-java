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

package com.vecsight.dragonite.proxy.network.socks5;

import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;

import java.util.Arrays;

public class SOCKS5Header {

    private boolean isDomain;

    private byte[] addr;

    private int port;

    public SOCKS5Header(final boolean isDomain, final byte[] addr, final int port) {
        this.isDomain = isDomain;
        this.addr = addr;
        this.port = port;
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

    @Override
    public String toString() {
        return "SOCKS5Header{" +
                "isDomain=" + isDomain +
                ", addr=" + (isDomain ? new String(addr, ProxyGlobalConstants.HEADER_ADDRESS_CHARSET) : Arrays.toString(addr)) +
                ", port=" + port +
                '}';
    }
}
