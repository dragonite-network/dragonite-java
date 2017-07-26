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

package com.vecsight.dragonite.proxy;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.pmw.tinylog.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;

public final class CLIMain {

    private static final String PRODUCT_NAME = "Dragonite Proxy";
    private static final String CMD_NAME = "dragonite-proxy";

    private static final String ARGS_FILE_NAME = "args.txt";

    private static final String WEB_PANEL_URL = "http://dragonite-webdev.vecsight.com/#/?api=http%3A%2F%2Flocalhost%3A8000%2Fstatistics&tick=1000";

    private static Options getOptions() {
        final Options options = new Options();
        options.addOption(Option
                .builder("s")
                .longOpt("server-mode")
                .desc("Enable server mode")
                .build());
        options.addOption(Option
                .builder("a")
                .longOpt("address")
                .desc("Remote server address for client / Bind address for server")
                .hasArg()
                .argName("address")
                .type(String.class)
                .build());
        options.addOption(Option
                .builder("p")
                .longOpt("port")
                .desc("Remote server port for client / Bind port for server")
                .hasArg()
                .argName("port")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("x")
                .longOpt("socks5-port")
                .desc("Local SOCKS5 proxy port for client")
                .hasArg()
                .argName("port")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("m")
                .longOpt("mtu")
                .desc("MTU of underlying Dragonite sockets")
                .hasArg()
                .argName("size")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("d")
                .longOpt("download-mbps")
                .desc("Download Mbps for client")
                .hasArg()
                .argName("mbps")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("u")
                .longOpt("upload-mbps")
                .desc("Upload Mbps for client")
                .hasArg()
                .argName("mbps")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("l")
                .longOpt("limit-mbps")
                .desc("Max Mbps per client for server")
                .hasArg()
                .argName("mbps")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder("w")
                .longOpt("welcome")
                .desc("Welcome message of server")
                .hasArg()
                .argName("msg")
                .type(String.class)
                .build());
        options.addOption(Option
                .builder()
                .longOpt("window-size-multiplier")
                .desc("Send window size multiplier of underlying Dragonite sockets (1-10)")
                .hasArg()
                .argName("multiplier")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder()
                .longOpt("debug")
                .desc("Set the logging level to DEBUG")
                .build());
        options.addOption(Option
                .builder()
                .longOpt("web-panel")
                .desc("Enable Web Panel of underlying Dragonite sockets (Bind to loopback interface)")
                .build());
        options.addOption(Option
                .builder()
                .longOpt("web-panel-public")
                .desc("Enable Web Panel of underlying Dragonite sockets (Bind to all interfaces)")
                .build());
        options.addOption(Option
                .builder("h")
                .longOpt("help")
                .desc("Help message")
                .build());
        return options;
    }

    private static String[] getArgs(final String[] cmdArgs) {
        final File argsFile = new File(ARGS_FILE_NAME);
        if (argsFile.canRead()) {
            try {
                final Scanner scanner = new Scanner(argsFile);
                final ArrayList<String> argsList = new ArrayList<>();
                while (scanner.hasNext()) {
                    argsList.add(scanner.next());
                }
                scanner.close();
                Logger.info("Arguments loaded from file \"{}\"", ARGS_FILE_NAME);
                return argsList.toArray(new String[0]);
            } catch (final FileNotFoundException e) {
                Logger.warn(e, "Unable to load file \"{}\", using commandline arguments", ARGS_FILE_NAME);
                return cmdArgs;
            }
        } else {
            Logger.info("Using commandline arguments");
            return cmdArgs;
        }
    }

    private static boolean openWebPanel() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(WEB_PANEL_URL));
                return true;
            } else {
                return false;
            }
        } catch (Exception | Error e) {
            return false;
        }
    }



}
