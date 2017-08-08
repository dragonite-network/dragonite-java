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

package com.vecsight.dragonite.sdk.obfs;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class XBCObfuscator implements Obfuscator {

    private static final int BASE_RANDOM_BYTES_LENGTH = 10;

    private static final int VAR_RANDOM_BYTES_LENGTH = 10;

    private final Random random = new Random();

    @Override
    public byte[] obfuscate(final byte[] rawData) {
        final byte lengthByte = (byte) random.nextInt();
        final int realLength = BASE_RANDOM_BYTES_LENGTH + Math.abs(lengthByte) % VAR_RANDOM_BYTES_LENGTH;
        final byte[] key = new byte[realLength];
        random.nextBytes(key);
        final ByteBuffer buffer = ByteBuffer.allocate(1 + key.length + rawData.length);
        buffer.put(lengthByte);
        buffer.put(key);
        buffer.put(xbc(rawData, key, false));
        return buffer.array();
    }

    @Override
    public byte[] deobfuscate(final byte[] obfsData) {
        final ByteBuffer buffer = ByteBuffer.wrap(obfsData);
        try {
            final byte lengthByte = buffer.get();
            final int realLength = BASE_RANDOM_BYTES_LENGTH + Math.abs(lengthByte) % VAR_RANDOM_BYTES_LENGTH;
            final byte[] key = new byte[realLength];
            buffer.get(key);
            final byte[] content = new byte[buffer.remaining()];
            buffer.get(content);
            return xbc(content, key, true);
        } catch (final BufferUnderflowException e) {
            return null;
        }
    }

    @Override
    public int getReceiveBufferOverhead() {
        return BASE_RANDOM_BYTES_LENGTH + VAR_RANDOM_BYTES_LENGTH;
    }

    private static byte[] xor(final byte[] input, final byte[] key) {
        final byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return result;
    }

    private static byte[] xbc(final byte[] input, final byte[] key, final boolean decrypt) {
        final byte[] result = new byte[input.length];
        final byte[][] blocks = splitBytes(input, key.length);
        byte[] previousBlockResult = null;
        int pos = 0;
        for (final byte[] block : blocks) {
            if (previousBlockResult == null) previousBlockResult = key;
            final byte[] eb = xor(block, previousBlockResult);
            previousBlockResult = decrypt ? block : eb;
            System.arraycopy(eb, 0, result, pos, eb.length);
            pos += eb.length;
        }
        return result;
    }

    private static byte[][] splitBytes(final byte[] data, final int chunkSize) {
        final int length = data.length;
        final byte[][] dest = new byte[(length + chunkSize - 1) / chunkSize][];
        int destIndex = 0;
        int stopIndex = 0;

        for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize) {
            stopIndex += chunkSize;
            dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
        }

        if (stopIndex < length)
            dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);

        return dest;
    }
}
