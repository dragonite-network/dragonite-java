/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.frame;

import java.util.Arrays;

public class FrameBuffer {

    private final byte[] bytesBuffer;

    private int position = 0;

    public FrameBuffer(final int maxSize) {
        bytesBuffer = new byte[maxSize];
    }

    public void add(final byte[] bytes) {
        System.arraycopy(bytes, 0, bytesBuffer, position, bytes.length);
        position += bytes.length;
    }

    public byte[] get() {
        return Arrays.copyOf(bytesBuffer, position);
    }

    public void reset() {
        position = 0;
    }

    public int getSize() {
        return position;
    }

    public int getMaxSize() {
        return bytesBuffer.length;
    }

}
