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

public class ConnectionAlreadyExistsException extends MuxException {

    public ConnectionAlreadyExistsException() {
        super("Connection already exists");
    }

    public ConnectionAlreadyExistsException(final String msg) {
        super(msg);
    }

}
