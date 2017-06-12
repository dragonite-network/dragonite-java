package com.vecsight.dragonite.forwarder;

import com.vecsight.dragonite.forwarder.config.ForwarderClientConfig;
import com.vecsight.dragonite.forwarder.config.ForwarderServerConfig;
import com.vecsight.dragonite.forwarder.misc.ForwarderGlobalConstants;
import com.vecsight.dragonite.forwarder.network.client.ForwarderClient;
import com.vecsight.dragonite.forwarder.network.server.ForwarderServer;
import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.InvalidValueException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
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

public class CLIMain {

    private static final String PRODUCT_NAME = "Dragonite Forwarder";
    private static final String CMD_NAME = "dragonite-forwarder";

    private static final String ARGS_FILE_NAME = "args.txt";

    private static final String WEB_PANEL_URL = "http://dragonite-webdev.vecsight.com/#/?api=http%3A%2F%2Flocalhost%3A8000%2Fstatistics&tick=1000";

    private static Options getOptions() {
        Options options = new Options();
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
                .builder("f")
                .longOpt("forwarding-port")
                .desc("Local port for client / Forwarding port for server")
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
                Logger.info("Arguments loaded from file \"{}\"", ARGS_FILE_NAME);
                return argsList.toArray(new String[0]);
            } catch (FileNotFoundException e) {
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
        } catch (URISyntaxException | IOException e) {
            return false;
        }
    }

    public static void main(String[] args) {

        Configurator.currentConfig()
                .level(Level.INFO)
                .formatPattern("{date:HH:mm:ss(X)} {{level}|min-size=6} {message}")
                .maxStackTraceElements(0)
                .activate();

        Logger.info("{} Version: v{}", PRODUCT_NAME, ForwarderGlobalConstants.APP_VERSION);
        Logger.info("SDK Version: v{}", DragoniteGlobalConstants.LIBRARY_VERSION);
        Logger.info("Mux Version: v{}", MuxGlobalConstants.LIBRARY_VERSION);

        final Options options = getOptions();
        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine;


        try {
            commandLine = parser.parse(options, getArgs(args));
        } catch (ParseException e) {
            Logger.error(e, "Cannot parse arguments");
            return;
        }

        if (commandLine.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
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
            if (commandLine.hasOption("f")) {
                try {
                    InetSocketAddress bindAddress = new InetSocketAddress(commandLine.hasOption("a") ? InetAddress.getByName(commandLine.getOptionValue("a")) : null,
                            commandLine.hasOption("p") ? ((Number) commandLine.getParsedOptionValue("p")).intValue() : ForwarderGlobalConstants.DEFAULT_SERVER_PORT);
                    ForwarderServerConfig config = new ForwarderServerConfig(bindAddress, ((Number) commandLine.getParsedOptionValue("f")).intValue());
                    if (commandLine.hasOption("m")) {
                        config.setMTU(((Number) commandLine.getParsedOptionValue("m")).intValue());
                    }
                    if (commandLine.hasOption("l")) {
                        config.setMbpsLimit(((Number) commandLine.getParsedOptionValue("l")).shortValue());
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

                    ForwarderServer forwarderServer = new ForwarderServer(config);

                    if (openWebPanel) {
                        if (!openWebPanel()) {
                            Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                        }
                    }
                } catch (ParseException | InvalidValueException e) {
                    Logger.error(e, "Incorrect value");
                } catch (SocketException | UnknownHostException e) {
                    Logger.error(e, "Unable to initialize");
                }
            } else {
                Logger.error("Missing required argument(s): -f");
            }
        } else {
            if (commandLine.hasOption("a") && commandLine.hasOption("f") && commandLine.hasOption("d") && commandLine.hasOption("u")) {
                try {
                    ForwarderClientConfig config = new ForwarderClientConfig(new InetSocketAddress(commandLine.getOptionValue("a"),
                            commandLine.hasOption("p") ? ((Number) commandLine.getParsedOptionValue("p")).intValue() : ForwarderGlobalConstants.DEFAULT_SERVER_PORT),
                            ((Number) commandLine.getParsedOptionValue("f")).intValue(),
                            ((Number) commandLine.getParsedOptionValue("d")).shortValue(),
                            ((Number) commandLine.getParsedOptionValue("u")).shortValue());
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

                    ForwarderClient forwarderClient = new ForwarderClient(config);

                    if (openWebPanel) {
                        if (!openWebPanel()) {
                            Logger.info("Unable to start the web browser on current platform, URL: {}", WEB_PANEL_URL);
                        }
                    }
                } catch (ParseException | InvalidValueException e) {
                    Logger.error(e, "Incorrect value");
                } catch (InterruptedException | IncorrectSizeException | SenderClosedException | IOException e) {
                    Logger.error(e, "Unable to initialize");
                }
            } else {
                Logger.error("Missing required argument(s): -a / -f / -d / -u");
            }
        }
    }

}
