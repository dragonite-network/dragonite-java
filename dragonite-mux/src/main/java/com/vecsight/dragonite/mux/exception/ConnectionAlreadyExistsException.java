/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.exception;

public class ConnectionAlreadyExistsException extends MuxException {

    public ConnectionAlreadyExistsException() {
        super("Connection already exists");
    }

    public ConnectionAlreadyExistsException(final String msg) {
        super(msg);
    }

}
