/*
 * VECTORSIGHT CONFIDENTIAL
 * ------------------------
 * Copyright (c) [2015] - [2017]
 * VectorSight Systems Co., Ltd.
 * All Rights Reserved.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Toby Huang <t@vecsight.com>, June 2017
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
