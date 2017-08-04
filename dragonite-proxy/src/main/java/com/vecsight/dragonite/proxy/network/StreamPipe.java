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

package com.vecsight.dragonite.proxy.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPipe {

    private final short bufferSize;

    public StreamPipe(final short bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void pipe(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        int len;
        final byte[] buf = new byte[bufferSize];
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }
    }

}
