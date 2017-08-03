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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public final class EncryptionKeyGenerator {

    private static SecretKeyFactory getFactory() throws NoSuchAlgorithmException {
        return SecretKeyFactory.getInstance(ProxyGlobalConstants.PASSWORD_HASH_ALGORITHM);
    }

    public static byte[] getKey(final String password) throws EncryptionException {
        final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), ProxyGlobalConstants.PASSWORD_HASH_SALT,
                ProxyGlobalConstants.PASSWORD_HASH_ITERATION_COUNT, ProxyGlobalConstants.PASSWORD_HASH_LENGTH_BITS);
        try {
            return getFactory().generateSecret(keySpec).getEncoded();
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

}
