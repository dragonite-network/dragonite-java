/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.misc;


import com.vecsight.dragonite.MuxBuildConfig;

public final class MuxGlobalConstants {

    //Version shits

    public static final String LIBRARY_VERSION = MuxBuildConfig.VERSION;

    //Protocol shits

    public static final byte PROTOCOL_VERSION = 2;

    //Buffer shits

    public static final int CONNECTION_MAX_DATA_BUFFER_SIZE = 1048576; //1M

}
