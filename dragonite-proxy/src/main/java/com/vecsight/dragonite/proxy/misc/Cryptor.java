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

public class Cryptor {

    private final byte[] encryptionKey;

    private final byte[] iv;

    private final Cipher decryptionCipher;

    private final Cipher encryptionCipher;

    public Cryptor(final byte[] encryptionKey, final byte[] iv) throws EncryptionException {
        this.encryptionKey = encryptionKey;
        this.iv = iv;
        final SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionKey, ProxyGlobalConstants.ENCRYPTION_ALGORITHM);
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        try {
            decryptionCipher = Cipher.getInstance(ProxyGlobalConstants.ENCRYPTION_ALGORITHM_WITH_MODE);
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            encryptionCipher = Cipher.getInstance(ProxyGlobalConstants.ENCRYPTION_ALGORITHM_WITH_MODE);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] decrypt(final byte[] bytes) {
        return decryptionCipher.update(bytes);
    }

    public byte[] encrypt(final byte[] bytes) {
        return encryptionCipher.update(bytes);
    }

}
