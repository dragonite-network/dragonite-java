/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.frame;

public interface Frame {

    byte getVersion();

    FrameType getType();

    byte[] toBytes();

    int getFixedLength();

    int getExpectedLength();

}
