/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.msg;

public interface Message {

    byte getVersion();

    MessageType getType();

    byte[] toBytes();

    int getFixedLength();

}
