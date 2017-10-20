/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
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
