/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.misc;

import com.vecsight.dragonite.ProxyBuildConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ProxyGlobalConstants {

    public static final String APP_VERSION = ProxyBuildConfig.VERSION;

    public static final byte PROTOCOL_VERSION = 2;

    public static final int DEFAULT_SERVER_PORT = 5234;

    public static final long INIT_SEND_SPEED = 100 * 1024; //100kb/s

    public static final Charset STRING_CHARSET = StandardCharsets.UTF_8;

    public static final Charset HEADER_ADDRESS_CHARSET = StandardCharsets.US_ASCII;

    public static final short PIPE_BUFFER_SIZE = 10240;

    public static final short MAX_FRAME_SIZE = 20480;

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int SOCKS5_PORT = 1080;

    public static final int TCP_CONNECT_TIMEOUT_MS = 4000;

    public static final String UPDATE_API_URL = "https://github.com/dragonite-network/dragonite-java/raw/master/VERSIONS";

    public static final String UPDATE_API_PRODUCT_NAME = "proxy";

}
