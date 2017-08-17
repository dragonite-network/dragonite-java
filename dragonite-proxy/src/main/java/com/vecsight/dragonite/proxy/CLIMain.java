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

import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;
import com.vecsight.dragonite.proxy.acl.ACLFileParser;
import com.vecsight.dragonite.proxy.config.ProxyClientConfig;
import com.vecsight.dragonite.proxy.config.ProxyServerConfig;
import com.vecsight.dragonite.proxy.exception.ACLException;
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.exception.ServerRejectedException;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.client.ProxyClient;
import com.vecsight.dragonite.proxy.network.server.ProxyServer;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.obfs.CRXObfuscator;
import org.apache.commons.cli.*;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.awt.*;
import java.io.*;
import java.net.*;
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
                .builder("k")
                .longOpt("password")
                .desc("Encryption password for both client and server")
                .hasArg()
                .argName("xxx")
                .type(String.class)
                .build());
        options.addOption(Option
                .builder()
                .longOpt("obfs")
                .desc("Enable XBC Obfuscator of underlying Dragonite sockets for both client and server")
                .build());
        options.addOption(Option
                .builder()
                .longOpt("allow-loopback")
                .desc("Allow clients to access the local loopback address of server")
                .build());
        options.addOption(Option
                .builder("r")
                .longOpt("acl")
                .desc("ACL file for client")
                .hasArg()
                .argName("path")
                .type(String.class)
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

    public static void main(final String[] args) {
        Configurator.currentConfig()
                .level(Level.INFO)
                .formatPattern("{date:HH:mm:ss(X)} [{level}] {message}")
                .maxStackTraceElements(0)
                .activate();

        Logger.info("{} Version: v{}", PRODUCT_NAME, ProxyGlobalConstants.APP_VERSION);
        Logger.info("SDK Version: v{}", DragoniteGlobalConstants.LIBRARY_VERSION);
        Logger.info("Mux Version: v{}", MuxGlobalConstants.LIBRARY_VERSION);

        final Options options = getOptions();
        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine;


        try {
            commandLine = parser.parse(options, getArgs(args));
        } catch (final ParseException e) {
            Logger.error(e, "Cannot parse arguments");
            return;
        }

        if (commandLine.hasOption("h")) {
            final HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(CMD_NAME, options);
            return;
        }

        if (commandLine.hasOption("debug")) {
            Configurator.currentConfig()
                    .level(Level.DEBUG)
                    .activate();
            Logger.debug("Debug mode enabled");
        }

        final boolean isServer = commandLine.hasOption("s");

        if (isServer) {
            if (commandLine.hasOption("k")) {
                try {
                    final InetSocketAddress bindAddress = new InetSocketAddress(commandLine.hasOption("a") ? InetAddress.getByName(commandLine.getOptionValue("a")) : null,
                            commandLine.hasOption("p") ? ((Number) commandLine.getParsedOptionValue("p")).intValue() : ProxyGlobalConstants.DEFAULT_SERVER_PORT);
                    final ProxyServerConfig config = new ProxyServerConfig(bindAddress, commandLine.getOptionValue("k"));

                    if (commandLine.hasOption("m")) {
                        config.setMTU(((Number) commandLine.getParsedOptionValue("m")).intValue());
                    }
                    if (commandLine.hasOption("l")) {
                        config.setMbpsLimit(((Number) commandLine.getParsedOptionValue("l")).intValue());
                    }
                    if (commandLine.hasOption("w")) {
                        config.setWelcomeMessage(commandLine.getOptionValue("w"));
                    }

                    boolean openWebPanel = false;
                    if (commandLine.hasOption("web-panel")) {
                        config.setWebPanelEnabled(true);
                        openWebPanel = true;
                    }
                    if (commandLine.hasOption("web-panel-public")) {
                        config.setWebPanelEnabled(true);
                        config.setWebPanelBind(new InetSocketAddress(DragoniteGlobalConstants.WEB_PANEL_PORT));
                        openWebPanel = true;
                    }
                    if (commandLine.hasOption("window-size-multiplier")) {
                        config.setWindowMultiplier(((Number) commandLine.getParsedOptionValue("window-size-multiplier")).intValue());
                    }
                    if (commandLine.hasOption("obfs")) {
                        config.setObfuscator(new CRXObfuscator(commandLine.getOptionValue("k").getBytes(ProxyGlobalConstants.STRING_CHARSET)));
                    }
                    if (commandLine.hasOption("allow-loopback")) {
                        config.setAllowLoopback(true);
                    }

                    final ProxyServer proxyServer = new ProxyServer(config);

                    if (openWebPanel) {
                        if (!openWebPanel()) {
                            Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                        }
                    }
                } catch (ParseException | IllegalArgumentException e) {
                    Logger.error(e, "Incorrect value");
                } catch (SocketException | EncryptionException | UnknownHostException e) {
                    Logger.error(e, "Unable to initialize");
                }
            } else {
                Logger.error("Missing required argument(s): -k");
            }
        } else {
            if (commandLine.hasOption("a") && commandLine.hasOption("d") && commandLine.hasOption("u") && commandLine.hasOption("k")) {
                try {
                    final ProxyClientConfig config = new ProxyClientConfig(new InetSocketAddress(commandLine.getOptionValue("a"),
                            commandLine.hasOption("p") ? ((Number) commandLine.getParsedOptionValue("p")).intValue() : ProxyGlobalConstants.DEFAULT_SERVER_PORT),
                            (commandLine.hasOption("x") ? ((Number) commandLine.getParsedOptionValue("x")).intValue() : ProxyGlobalConstants.SOCKS5_PORT),
                            commandLine.getOptionValue("k"),
                            ((Number) commandLine.getParsedOptionValue("d")).intValue(),
                            ((Number) commandLine.getParsedOptionValue("u")).intValue()
                    );
                    if (commandLine.hasOption("m")) {
                        config.setMTU(((Number) commandLine.getParsedOptionValue("m")).intValue());
                    }

                    boolean openWebPanel = false;
                    if (commandLine.hasOption("web-panel")) {
                        config.setWebPanelEnabled(true);
                        openWebPanel = true;
                    }
                    if (commandLine.hasOption("web-panel-public")) {
                        config.setWebPanelEnabled(true);
                        config.setWebPanelBind(new InetSocketAddress(DragoniteGlobalConstants.WEB_PANEL_PORT));
                        openWebPanel = true;
                    }
                    if (commandLine.hasOption("window-size-multiplier")) {
                        config.setWindowMultiplier(((Number) commandLine.getParsedOptionValue("window-size-multiplier")).intValue());
                    }
                    if (commandLine.hasOption("obfs")) {
                        config.setObfuscator(new CRXObfuscator(commandLine.getOptionValue("k").getBytes(ProxyGlobalConstants.STRING_CHARSET)));
                    }

                    if (commandLine.hasOption("r")) {
                        final String path = commandLine.getOptionValue("r");
                        final String loweredPath = path.toLowerCase();
                        final Reader reader;
                        try {
                            if (loweredPath.startsWith("http://") || loweredPath.startsWith("https://")) {
                                reader = new InputStreamReader(new URL(path).openStream());
                            } else {
                                reader = new FileReader(path);
                            }
                            config.setAcl(ACLFileParser.parse(reader));
                        } catch (IOException | ACLException e) {
                            Logger.error(e, "Failed to parse ACL file");
                        }
                    }

                    final ProxyClient proxyClient = new ProxyClient(config);

                    if (openWebPanel) {
                        if (!openWebPanel()) {
                            Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                        }
                    }
                } catch (ParseException | IllegalArgumentException e) {
                    Logger.error(e, "Incorrect value");
                } catch (InterruptedException | IOException | DragoniteException | IncorrectHeaderException | ServerRejectedException | EncryptionException e) {
                    Logger.error(e, "Unable to initialize");
                }
            } else {
                Logger.error("Missing required argument(s): -a / -k / -d / -u");
            }
        }
    }

}
