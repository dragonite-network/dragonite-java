package com.vecsight.dragonite.sdk.exception;

public class ConnectionNotAliveException extends Exception {

    public ConnectionNotAliveException() {
        super("Connection is not alive");
    }

    public ConnectionNotAliveException(String msg) {
        super(msg);
    }

}
