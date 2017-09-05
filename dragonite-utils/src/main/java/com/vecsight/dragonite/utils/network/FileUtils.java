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

package com.vecsight.dragonite.utils.network;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public final class FileUtils {

    public static Reader pathToReader(final String path) throws IOException {
        final String loweredPath = path.toLowerCase();
        final Reader reader;
        if (loweredPath.startsWith("http://") || loweredPath.startsWith("https://")) {
            reader = new InputStreamReader(new URL(path).openStream());
        } else {
            reader = new FileReader(path);
        }
        return reader;
    }

}
