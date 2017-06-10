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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CLIMain {

    private static final String PRODUCT_NAME = "Dragonite Forwarder";
    private static final String CMD_NAME = "dragonite-forwarder";

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
                .desc("Enable Web Panel of underlying Dragonite sockets")
                .build());
        options.addOption(Option
                .builder("h")
                .longOpt("help")
                .desc("Help message")
                .build());
        return options;
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
            commandLine = parser.parse(options, args);
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
                    if (commandLine.hasOption("web-panel")) {
                        config.setWebPanelEnabled(true);
                    }
                    ForwarderServer forwarderServer = new ForwarderServer(config);
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
                    if (commandLine.hasOption("web-panel")) {
                        config.setWebPanelEnabled(true);
                    }
                    ForwarderClient forwarderClient = new ForwarderClient(config);
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
