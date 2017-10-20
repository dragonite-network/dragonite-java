/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.cryptor;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

public class SimpleXORCryptor implements PacketCryptor {

    private static final int BASE_RANDOM_BYTES_LENGTH = 20;

    private static final int VAR_RANDOM_BYTES_LENGTH = 50;

    private final Random random = new Random();

    private final byte[] psk;

    public SimpleXORCryptor(final byte[] psk) {
        this.psk = psk;
    }

    @Override
    public byte[] encrypt(final byte[] rawData) {
        final byte lengthByte = (byte) random.nextInt();
        final int realLength = BASE_RANDOM_BYTES_LENGTH + Math.abs(lengthByte) % VAR_RANDOM_BYTES_LENGTH;
        final byte[] key = new byte[realLength];
        random.nextBytes(key);
        final ByteBuffer buffer = ByteBuffer.allocate(1 + key.length + rawData.length);
        buffer.put(lengthByte);
        buffer.put(key);
        buffer.put(xor(rawData, xor(key, psk)));
        return buffer.array();
    }

    @Override
    public byte[] decrypt(final byte[] encryptedData) {
        final ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        try {
            final byte lengthByte = buffer.get();
            final int realLength = BASE_RANDOM_BYTES_LENGTH + Math.abs(lengthByte) % VAR_RANDOM_BYTES_LENGTH;
            final byte[] key = new byte[realLength];
            buffer.get(key);
            final byte[] content = new byte[buffer.remaining()];
            buffer.get(content);
            return xor(content, xor(key, psk));
        } catch (final BufferUnderflowException e) {
            return null;
        }
    }

    @Override
    public int getMaxAdditionalBytesLength() {
        return BASE_RANDOM_BYTES_LENGTH + VAR_RANDOM_BYTES_LENGTH;
    }

    private static byte[] xor(final byte[] input, final byte[] key) {
        final byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return result;
    }

}
