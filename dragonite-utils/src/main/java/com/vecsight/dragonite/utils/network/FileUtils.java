/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
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
