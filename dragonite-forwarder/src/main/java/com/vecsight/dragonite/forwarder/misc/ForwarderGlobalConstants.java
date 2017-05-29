package com.vecsight.dragonite.forwarder.misc;

import com.vecsight.dragonite.ForwarderBuildConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ForwarderGlobalConstants {

    //Version shits

    public static final String APP_VERSION = ForwarderBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 1;

    public static final int DEFAULT_SERVER_PORT = 5233;

    public static final long INIT_SEND_SPEED = 100 * 1024; //100kb/s

    public static final Charset STRING_CHARSET = StandardCharsets.UTF_8;

    //Sizes

    public static final short PIPE_BUFFER_SIZE = 10240;

    public static final short MAX_FRAME_SIZE = 20480;

}
