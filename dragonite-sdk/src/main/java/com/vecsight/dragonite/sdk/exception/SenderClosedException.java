/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.exception;

public class SenderClosedException extends DragoniteException {

    public SenderClosedException() {
        super("Sender closed");
    }

    public SenderClosedException(final String msg) {
        super(msg);
    }

}
