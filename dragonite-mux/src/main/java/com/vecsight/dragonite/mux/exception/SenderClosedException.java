package com.vecsight.dragonite.mux.exception;

public class SenderClosedException extends Exception {

    public SenderClosedException() {
        super("Sender closed");
    }

    public SenderClosedException(final String msg) {
        super(msg);
    }

}
