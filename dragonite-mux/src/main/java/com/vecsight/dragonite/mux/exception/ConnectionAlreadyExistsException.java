package com.vecsight.dragonite.mux.exception;

public class ConnectionAlreadyExistsException extends Exception {

    public ConnectionAlreadyExistsException() {
        super("Connection already exists");
    }

    public ConnectionAlreadyExistsException(final String msg) {
        super(msg);
    }

}
