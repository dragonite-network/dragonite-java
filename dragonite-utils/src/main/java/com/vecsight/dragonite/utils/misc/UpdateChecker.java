/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */

package com.vecsight.dragonite.utils.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {

    private final static int TIMEOUT = 4000;

    private final String URLString;

    public UpdateChecker(final String URL) {
        this.URLString = URL;
    }

    public String getURL() {
        return URLString;
    }

    public String getVersionString(final String productName) {
        try {
            final URL url = new URL(URLString);
            final URLConnection conn = url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("-")) {
                        final String[] kv = line.split("-");
                        if (kv[0].equalsIgnoreCase(productName))
                            return kv[1];
                    }
                }
            }
            return null;
        } catch (final IOException e) {
            return null;
        }
    }
}
