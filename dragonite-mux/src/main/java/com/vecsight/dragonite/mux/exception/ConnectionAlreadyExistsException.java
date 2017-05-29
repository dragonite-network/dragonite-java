package com.vecsight.dragonite.mux.exception;

public class ConnectionAlreadyExistsException extends Exception {

    public ConnectionAlreadyExistsException() {
        super("Connection already exists");
    }

    public ConnectionAlreadyExistsException(String msg) {
        super(msg);
    }

}
