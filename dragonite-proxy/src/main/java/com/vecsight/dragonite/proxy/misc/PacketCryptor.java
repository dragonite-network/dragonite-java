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

package com.vecsight.dragonite.proxy.misc;

import com.vecsight.dragonite.proxy.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PacketCryptor {

    private final byte[] encryptionKey;

    private final Cipher decryptionCipher;

    private final Cipher encryptionCipher;

    private final SecretKeySpec secretKeySpec;

    public PacketCryptor(final byte[] encryptionKey) throws EncryptionException {
        this.encryptionKey = encryptionKey;
        try {
            this.decryptionCipher = Cipher.getInstance(ProxyGlobalConstants.ENCRYPTION_ALGORITHM_WITH_MODE);
            this.encryptionCipher = Cipher.getInstance(ProxyGlobalConstants.ENCRYPTION_ALGORITHM_WITH_MODE);
            this.secretKeySpec = new SecretKeySpec(encryptionKey, ProxyGlobalConstants.ENCRYPTION_ALGORITHM);
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public byte[] decrypt(final byte[] bytes, final byte[] iv) {
        try {
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return decryptionCipher.update(bytes);
        } catch (final InvalidKeyException | InvalidAlgorithmParameterException e) {
            return null;
        }
    }

    public byte[] encrypt(final byte[] bytes, final byte[] iv) {
        try {
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return encryptionCipher.update(bytes);
        } catch (final InvalidKeyException | InvalidAlgorithmParameterException e) {
            return null;
        }
    }

}
