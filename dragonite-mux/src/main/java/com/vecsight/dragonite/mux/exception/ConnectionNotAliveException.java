/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.exception;

public class ConnectionNotAliveException extends MuxException {

    public ConnectionNotAliveException() {
        super("Connection is not alive");
    }

    public ConnectionNotAliveException(final String msg) {
        super(msg);
    }

}
