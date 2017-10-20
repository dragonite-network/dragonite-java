/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.exception;

public class DataLengthMismatchException extends MuxException {

    private final int expectedLength, currentLength;

    public DataLengthMismatchException(final int expectedLength, final int currentLength) {
        this.expectedLength = expectedLength;
        this.currentLength = currentLength;
    }

    public int getExpectedLength() {
        return expectedLength;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public int getDeltaLength() {
        return expectedLength - currentLength;
    }

}
