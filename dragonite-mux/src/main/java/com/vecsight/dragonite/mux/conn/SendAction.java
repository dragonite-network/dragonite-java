/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.conn;

public interface SendAction {

    void sendPacket(byte[] bytes);

}