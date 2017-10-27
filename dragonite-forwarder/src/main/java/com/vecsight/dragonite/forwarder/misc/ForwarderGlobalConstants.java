/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.forwarder.misc;

import com.vecsight.dragonite.ForwarderBuildConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ForwarderGlobalConstants {

    //Version shits

    public static final String APP_VERSION = ForwarderBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 3;

    public static final int DEFAULT_SERVER_PORT = 5233;

    public static final long INIT_SEND_SPEED = 100 * 1024; //100kb/s

    public static final Charset STRING_CHARSET = StandardCharsets.UTF_8;

    //Sizes

    public static final short PIPE_BUFFER_SIZE = 10240;

    public static final short MAX_FRAME_SIZE = 20480;

    //Encryption shits

    public static final int PASSWORD_MIN_LENGTH = 4;

    //Update shits

    public static final String UPDATE_API_URL = "https://github.com/dragonite-network/dragonite-java/raw/master/VERSIONS";

    public static final String UPDATE_API_PRODUCT_NAME = "forwarder";

}
