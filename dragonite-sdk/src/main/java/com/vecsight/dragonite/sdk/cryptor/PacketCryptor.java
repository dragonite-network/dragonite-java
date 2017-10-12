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

package com.vecsight.dragonite.sdk.cryptor;

public interface PacketCryptor {

    /**
     * Encrypt the message and return the ciphertext.
     * This function may be called by multiple threads at the same time.
     *
     * @param rawData Content to be encrypted
     * @return Encrypted content
     */
    byte[] encrypt(final byte[] rawData);

    /**
     * Decrypt the ciphertext and return the message.
     * This function may be called by multiple threads at the same time.
     *
     * @param encryptedData Content to be decrypted
     * @return Decrypted content
     */
    byte[] decrypt(final byte[] encryptedData);

    /**
     * Many encryption methods need to add additional bytes to the original message.
     * Please specify the maximum length of additional bytes that the encryption might cause.
     *
     * @return Length of additional bytes
     */
    int getReceiveBufferOverhead();

}
