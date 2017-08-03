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

import com.vecsight.dragonite.proxy.exception.SOCKS5Exception;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public final class SOCKS5SocketHelper {

    private static final byte SOCKS5_VERSION = 0x05;

    private static final byte[] SOCKS5_SUCCEED = {SOCKS5_VERSION, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static final byte[] SOCKS5_NOT_ALLOWED = {SOCKS5_VERSION, 0x02, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static final byte[] SOCKS5_FAILED_REFUSED = {SOCKS5_VERSION, 0x05, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private static final byte[] SOCKS5_NOT_SUPPORTED = {SOCKS5_VERSION, 0x07, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static SOCKS5Header handleHeader(final Socket socket) throws IOException, SOCKS5Exception {
        final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        final OutputStream outputStream = socket.getOutputStream();
        /*
            Auth frame:
            VER	NMETHODS	METHODS
            1	1	        1-255
         */
        if (inputStream.readByte() != SOCKS5_VERSION) {
            throw new SOCKS5Exception("Invalid protocol version");
        }
        boolean supportNoAuth = false;
        final int methodCount = inputStream.readUnsignedByte();
        for (int i = 0; i < methodCount; i++) {
            if (inputStream.readByte() == 0x00) {
                supportNoAuth = true;
            }
        }
        if (!supportNoAuth) {
            final byte[] failResponse = {SOCKS5_VERSION, (byte) 0xFF};
            outputStream.write(failResponse);
            throw new SOCKS5Exception("The client does not support no authentication mode");
        }
        //Use no auth
        final byte[] noAuthResponse = {SOCKS5_VERSION, 0x00};
        outputStream.write(noAuthResponse);
        /*
            Request frame:
            VER	CMD	RSV	    ATYP	DST.ADDR	DST.PORT
            1	1	0x00	1	    ...         2
         */
        if (inputStream.readByte() != SOCKS5_VERSION) {
            throw new SOCKS5Exception("Invalid protocol version");
        }
        final byte cmd = inputStream.readByte();
        final byte rsv = inputStream.readByte(); //reserved, we just ignore it
        final byte atyp = inputStream.readByte();
        final byte[] addr;
        final int port;
        if (atyp == 0x01) {
            //IPv4
            addr = new byte[4];
            inputStream.readFully(addr);
        } else if (atyp == 0x03) {
            //Domain
            addr = new byte[inputStream.readUnsignedByte()];
            inputStream.readFully(addr);
        } else if (atyp == 0x04) {
            //IPv6
            addr = new byte[16];
            inputStream.readFully(addr);
        } else {
            throw new SOCKS5Exception("Unknown address type");
        }
        port = inputStream.readUnsignedShort();
        if (cmd == 0x01) {
            //CONNECT
            if (atyp == 0x03)
                return new SOCKS5Header(true, addr, port);
            else
                return new SOCKS5Header(false, addr, port);
            //No response frame yet
        } else {
            //Not supported, yet
            outputStream.write(SOCKS5_NOT_SUPPORTED);
            throw new SOCKS5Exception("Command " + cmd + " is not supported.");
        }
    }

    public static void sendSucceed(final Socket socket) throws IOException {
        socket.getOutputStream().write(SOCKS5_SUCCEED);
    }

    public static void sendFailed(final Socket socket) throws IOException {
        socket.getOutputStream().write(SOCKS5_FAILED_REFUSED);
    }

    public static void sendRejected(final Socket socket) throws IOException {
        socket.getOutputStream().write(SOCKS5_NOT_ALLOWED);
    }

}
