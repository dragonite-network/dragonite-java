package com.vecsight.dragonite.mux.exception;

public class ConnectionNotAliveException extends Exception {

    public ConnectionNotAliveException() {
        super("Connection is not alive");
    }

    public ConnectionNotAliveException(final String msg) {
        super(msg);
    }

}
