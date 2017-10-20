/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.exception;

public class MultiplexerClosedException extends MuxException {

    public MultiplexerClosedException() {
        super("Multiplexer closed");
    }

    public MultiplexerClosedException(final String msg) {
        super(msg);
    }

}
