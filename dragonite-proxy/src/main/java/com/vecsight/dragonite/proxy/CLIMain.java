/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy;

import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;
import com.vecsight.dragonite.proxy.acl.ACLFileParser;
import com.vecsight.dragonite.proxy.config.ProxyClientConfig;
import com.vecsight.dragonite.proxy.config.ProxyJSONConfigParser;
import com.vecsight.dragonite.proxy.config.ProxyServerConfig;
import com.vecsight.dragonite.proxy.exception.ACLException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.exception.JSONConfigException;
import com.vecsight.dragonite.proxy.exception.ServerRejectedException;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.proxy.network.client.ProxyClient;
import com.vecsight.dragonite.proxy.network.server.ProxyServer;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.exception.EncryptionException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.utils.misc.UpdateChecker;
import com.vecsight.dragonite.utils.network.FileUtils;
import com.vecsight.dragonite.utils.type.UnitConverter;
import org.apache.commons.cli.*;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
                .longOpt("dscp")
                .desc("Set DSCP value in the IP headers")
                .hasArg()
                .argName("value")
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
                .builder("c")
                .longOpt("config")
                .desc("JSON config file")
                .hasArg()
                .argName("path")
                .type(String.class)
                .build());
        options.addOption(Option
                .builder()
                .longOpt("skip-update")
                .desc("Skip the update check")
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

    private static void checkUpdate() {
        final UpdateChecker updateChecker = new UpdateChecker(ProxyGlobalConstants.UPDATE_API_URL);

        Logger.info("Checking for updates...");

        final String remoteVersion = updateChecker.getVersionString(ProxyGlobalConstants.UPDATE_API_PRODUCT_NAME);
        if (remoteVersion != null && remoteVersion.equals(ProxyGlobalConstants.APP_VERSION)) {
            Logger.info("You are already using the latest version.");
        } else if (remoteVersion != null && remoteVersion.length() > 0) {
            Logger.info("** New version available! v{} **", remoteVersion);
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

        if (!commandLine.hasOption("skip-update")) {
            checkUpdate();
        }

        final boolean useConfigFile = commandLine.hasOption("c");
        final boolean isServer;
        final ProxyJSONConfigParser configParser;
        if (useConfigFile) {
            try {
                configParser = new ProxyJSONConfigParser(commandLine.getOptionValue("c"));
                isServer = configParser.isServerConfig();
            } catch (IOException | JSONConfigException e) {
                Logger.error(e, "Failed to parse config");
                return;
            }
        } else {
            configParser = null;
            isServer = commandLine.hasOption("s");
        }

        if (isServer) {

            final ProxyServerConfig config;
            if (useConfigFile) {
                config = serverConfigFromConfigParser(configParser);
            } else {
                config = serverConfigFromCommandline(commandLine);
            }

            if (config != null) {
                try {
                    final ProxyServer proxyServer = new ProxyServer(config);
                } catch (final SocketException e) {
                    Logger.error(e, "Unable to initialize");
                    return;
                }

                if (config.getWebPanelEnabled()) {
                    if (!openWebPanel()) {
                        Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                    }
                }
            }

        } else {

            final ProxyClientConfig config;
            if (useConfigFile) {
                config = clientConfigFromConfigParser(configParser);
            } else {
                config = clientConfigFromCommandline(commandLine);
            }

            if (config != null) {
                try {
                    final ProxyClient proxyClient = new ProxyClient(config);
                } catch (final IOException | InterruptedException | DragoniteException | ServerRejectedException | IncorrectHeaderException e) {
                    Logger.error(e, "Unable to initialize");
                    return;
                }

                if (config.getWebPanelEnabled()) {
                    if (!openWebPanel()) {
                        Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                    }
                }
            }

        }
    }

    private static ProxyServerConfig serverConfigFromConfigParser(final ProxyJSONConfigParser configParser) {
        try {
            return configParser.getServerConfig();
        } catch (final JSONConfigException e) {
            Logger.error(e, "Failed to parse config");
            return null;
        }
    }

    private static ProxyClientConfig clientConfigFromConfigParser(final ProxyJSONConfigParser configParser) {
        try {
            return configParser.getClientConfig();
        } catch (final JSONConfigException e) {
            Logger.error(e, "Failed to parse config");
            return null;
        }
    }

    private static ProxyServerConfig serverConfigFromCommandline(final CommandLine commandLine) {
        try {
            if (commandLine.hasOption("k")) {
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

                if (commandLine.hasOption("web-panel")) {
                    config.setWebPanelEnabled(true);
                }
                if (commandLine.hasOption("web-panel-public")) {
                    config.setWebPanelEnabled(true);
                    config.setWebPanelBind(new InetSocketAddress(DragoniteGlobalConstants.WEB_PANEL_PORT));
                }
                if (commandLine.hasOption("window-size-multiplier")) {
                    config.setWindowMultiplier(((Number) commandLine.getParsedOptionValue("window-size-multiplier")).intValue());
                }
                if (commandLine.hasOption("dscp")) {
                    config.setTrafficClass(UnitConverter.DSCPtoTrafficClass(((Number) commandLine.getParsedOptionValue("dscp")).intValue()));
                }
                if (commandLine.hasOption("allow-loopback")) {
                    config.setAllowLoopback(true);
                }
                return config;
            } else {
                Logger.error("Missing required argument(s): -k");
            }
        } catch (final ParseException | IllegalArgumentException e) {
            Logger.error(e, "Incorrect value");
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host");
        } catch (final EncryptionException e) {
            Logger.error(e, "Encryption error");
        }
        return null;
    }

    private static ProxyClientConfig clientConfigFromCommandline(final CommandLine commandLine) {
        try {
            if (commandLine.hasOption("a") && commandLine.hasOption("d") && commandLine.hasOption("u") && commandLine.hasOption("k")) {
                final ProxyClientConfig config = new ProxyClientConfig(new InetSocketAddress(InetAddress.getByName(commandLine.getOptionValue("a")),
                        commandLine.hasOption("p") ? ((Number) commandLine.getParsedOptionValue("p")).intValue() : ProxyGlobalConstants.DEFAULT_SERVER_PORT),
                        (commandLine.hasOption("x") ? ((Number) commandLine.getParsedOptionValue("x")).intValue() : ProxyGlobalConstants.SOCKS5_PORT),
                        commandLine.getOptionValue("k"),
                        ((Number) commandLine.getParsedOptionValue("d")).intValue(),
                        ((Number) commandLine.getParsedOptionValue("u")).intValue()
                );
                if (commandLine.hasOption("m")) {
                    config.setMTU(((Number) commandLine.getParsedOptionValue("m")).intValue());
                }

                if (commandLine.hasOption("web-panel")) {
                    config.setWebPanelEnabled(true);
                }
                if (commandLine.hasOption("web-panel-public")) {
                    config.setWebPanelEnabled(true);
                    config.setWebPanelBind(new InetSocketAddress(DragoniteGlobalConstants.WEB_PANEL_PORT));
                }
                if (commandLine.hasOption("window-size-multiplier")) {
                    config.setWindowMultiplier(((Number) commandLine.getParsedOptionValue("window-size-multiplier")).intValue());
                }
                if (commandLine.hasOption("dscp")) {
                    config.setTrafficClass(UnitConverter.DSCPtoTrafficClass(((Number) commandLine.getParsedOptionValue("dscp")).intValue()));
                }

                if (commandLine.hasOption("r")) {
                    try {
                        config.setAcl(ACLFileParser.parse(FileUtils.pathToReader(commandLine.getOptionValue("r"))));
                    } catch (IOException | ACLException e) {
                        Logger.error(e, "Failed to parse ACL file");
                    }
                }
                return config;
            } else {
                Logger.error("Missing required argument(s): -a / -k / -d / -u");
            }
        } catch (final ParseException | IllegalArgumentException e) {
            Logger.error(e, "Incorrect value");
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host");
        } catch (final EncryptionException e) {
            Logger.error(e, "Encryption error");
        }
        return null;
    }

}
