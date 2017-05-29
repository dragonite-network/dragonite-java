package com.vecsight.dragonite.mux.exception;

public class MultiplexerClosedException extends Exception {

    public MultiplexerClosedException() {
        super("Multiplexer closed");
    }

    public MultiplexerClosedException(String msg) {
        super(msg);
    }

}
