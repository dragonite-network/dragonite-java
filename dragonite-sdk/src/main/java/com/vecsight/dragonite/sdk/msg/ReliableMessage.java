/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.msg;

public interface ReliableMessage extends Message {

    int getSequence();

    void setSequence(int sequence);

}
