package com.vecsight.dragonite.proxy.gui.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import com.vecsight.dragonite.proxy.config.ProxyClientConfig;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.exception.ServerRejectedException;
import com.vecsight.dragonite.proxy.gui.module.GuiConfig;
import com.vecsight.dragonite.proxy.network.client.ProxyClient;
import com.vecsight.dragonite.sdk.exception.DragoniteException;
import com.vecsight.dragonite.sdk.exception.EncryptionException;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.datafx.controller.ViewController;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import lombok.Cleanup;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/*******************************************************************************
 * Copyright (c) 2005-2017 Mritd, Inc.
 * dragonite
 * com.vecsight.dragonite.proxy.gui.controller
 * Created by mritd on 17/11/28 下午9:40.
 * Description: DragoniteController
 *******************************************************************************/
public class DragoniteController {
    @FXML
    private JFXTextField tfServer;
    @FXML
    private JFXTextField tfServerPort;
    @FXML
    private JFXTextField tfLocalPort;
    @FXML
    private JFXPasswordField pfPassword;
    @FXML
    private JFXTextField tfDownloadMbps;
    @FXML
    private JFXTextField tfUploadMbps;
    @FXML
    private JFXTextField tfMTU;
    @FXML
    private JFXProgressBar pbGoogleStatus;
    @FXML
    private LineChart<Number, Number> lcSystemLoad;
    @FXML
    private LineChart<Number, Number> lcMemory;
    @FXML
    private FontAwesomeIconView faiConfigSaved;
    @FXML
    private Label lbConfigSaved;
    @FXML
    private FontAwesomeIconView faiStarted;
    @FXML
    private Label lbStarted;

    public boolean isClosed;
    private static boolean isStarted;
    private ProxyClient proxyClient;
    private ExecutorService proxyExecutor;
    private static final String CONFIG_PATH = ".dragonite/dragonite-proxy-gui.json";

    @FXML
    public void dragoniteProxyStart() {

        if (isStarted) {
            Logger.warn("Proxy started!");
            return;
        }

        if (StringUtils.isBlank(tfServer.getText())) {
            Logger.error("Server address is blank!");
            return;
        }
        if (StringUtils.isBlank(pfPassword.getText())) {
            Logger.error("Server password is blank! ");
            return;
        }
        if (StringUtils.isBlank(tfServerPort.getText()) || isNotNumeric(tfServerPort.getText())) {
            Logger.error("Server port is blank or format is incorrect!");
            return;
        }
        if (StringUtils.isBlank(tfLocalPort.getText()) || isNotNumeric(tfLocalPort.getText())) {
            Logger.error("Local socks5 port is blank or format is incorrect!");
            return;
        }
        if (StringUtils.isBlank(tfDownloadMbps.getText()) || isNotNumeric(tfDownloadMbps.getText())) {
            Logger.error("Download mbps is blank or format is incorrect!");
            return;
        }
        if (StringUtils.isBlank(tfUploadMbps.getText()) || isNotNumeric(tfUploadMbps.getText())) {
            Logger.error("Upload mbps is blank or format is incorrect!");
            return;
        }

        if (StringUtils.isNotBlank(tfMTU.getText()) && isNotNumeric(tfMTU.getText())) {
            Logger.error("MTU format is incorrect!");
            return;
        }

        isStarted = true;

        Task<Boolean> proxyTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(tfServer.getText());
                    int serverPort = Integer.parseInt(tfServerPort.getText());
                    int localSocks5Port = Integer.parseInt(tfLocalPort.getText());
                    String serverPassword = pfPassword.getText();
                    int downloadMbps = Integer.parseInt(tfDownloadMbps.getText());
                    int uploadMbps = Integer.parseInt(tfUploadMbps.getText());
                    ProxyClientConfig clientConfig = new ProxyClientConfig(new InetSocketAddress(serverAddress, serverPort), localSocks5Port, serverPassword, downloadMbps, uploadMbps);
                    clientConfig.setMTU(StringUtils.isNotBlank(tfMTU.getText()) ? Integer.parseInt(tfMTU.getText()) : 1300);
                    proxyClient = new ProxyClient(clientConfig);
                    return true;
                } catch (EncryptionException | IOException | ServerRejectedException | InterruptedException | DragoniteException | IncorrectHeaderException e) {
                    Logger.error(e, "DragoniteProxy Start Failed");
                    return false;
                }
            }
        };


        proxyTask.valueProperty().addListener((observableValue, oldData, newData) -> {
            if(newData){
                faiStarted.setGlyphName("PLAY_CIRCLE");
                lbStarted.setText("Proxy Started ...");
            }else {
                faiStarted.setGlyphName("STOP_CIRCLE");
                lbStarted.setText("Proxy Stopped ...");
            }
        });

        proxyExecutor = Executors.newSingleThreadExecutor();
        proxyExecutor.submit(proxyTask);

    }

    @FXML
    public void dragoniteProxyStop() {

        if (proxyClient != null) {
            proxyClient.close();
            proxyExecutor.shutdown();
            proxyClient = null;
            proxyExecutor = null;
        }
        isStarted = false;
        faiStarted.setGlyphName("STOP_CIRCLE");
        lbStarted.setText("Proxy Stopped ...");
        Logger.info("DragoniteProxy stopped!");
    }

    @FXML
    public void dragoniteProxySave() {
        saveConfig();
        Logger.info("Config Saved ...");
    }


    public void init() {
        initValidate();
        initSystemMonitor();
        initGoogleMonitor();
        loadConfig();
    }

    private void initValidate() {

        tfServer.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                tfServer.validate();
            }
        });
        tfServerPort.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                tfServerPort.validate();
            }
        });
        tfLocalPort.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                tfLocalPort.validate();
            }
        });
        pfPassword.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                pfPassword.validate();
            }
        });
        tfDownloadMbps.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                tfDownloadMbps.validate();
            }
        });
        tfUploadMbps.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                tfUploadMbps.validate();
            }
        });
    }


    private void initSystemMonitor() {

        XYChart.Series cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("systemload");
        lcSystemLoad.getData().add(cpuSeries);
        ObservableList<XYChart.Data<String, Object>> cpuInfoList = cpuSeries.getData();

        XYChart.Series speedSeries = new XYChart.Series<>();
        speedSeries.setName("memory");
        lcMemory.getData().add(speedSeries);
        ObservableList<XYChart.Data<String, Object>> speedInfoList = speedSeries.getData();

        Task<List<XYChart.Data<String, Object>>> monitorTask = new Task<List<XYChart.Data<String, Object>>>() {
            @Override
            protected List<XYChart.Data<String, Object>> call() {

                while (!isClosed) {
                    try {
                        Thread.sleep(1000);
                        List<XYChart.Data<String, Object>> data = new ArrayList<>(2);
                        // Windows 下返回 -1，具体参见  http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6336608
                        double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
                        long memoryUse = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
                        data.add(new XYChart.Data<>(new SimpleDateFormat("ss").format(new Date()), systemLoad));
                        data.add(new XYChart.Data<>(new SimpleDateFormat("ss").format(new Date()), memoryUse));
                        updateValue(data);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }

                }
                return null;
            }
        };

        monitorTask.valueProperty().addListener((observableValue, oldData, newData) -> {

            if (cpuInfoList.size() - 10 > 0) {
                cpuInfoList.remove(0);
            }
            cpuInfoList.add(newData.get(0));

            if (speedInfoList.size() - 10 > 0) {
                speedInfoList.remove(0);
            }
            speedInfoList.add(newData.get(1));

        });


        ExecutorService systemMonitorExecutor = Executors.newSingleThreadExecutor();
        systemMonitorExecutor.submit(monitorTask);

    }

    private void initGoogleMonitor() {

        Task<Boolean> checkGoogleConnect = new Task<Boolean>() {
            @Override
            protected Boolean call() {

                while (!isClosed) {
                    try {
                        Thread.sleep(30 * 1000);

                        Proxy dragoniteProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", Integer.parseInt(tfLocalPort.getText())));
                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .proxy(dragoniteProxy)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://www.google.com")
                                .get()
                                .build();
                        Response response = client.newCall(request).execute();
                        updateValue(response.isSuccessful());
                        response.close();

                    } catch (IOException | InterruptedException ignore) {
                        updateValue(false);
                    }

                }

                return false;
            }
        };

        checkGoogleConnect.valueProperty().addListener((observableValue, oldData, newData) -> {

            Logger.info("Google connection status ==> " + newData);

            if (newData) pbGoogleStatus.setProgress(1.0);
            else pbGoogleStatus.setProgress(-1.0);
        });

        ExecutorService googleMonitorExecutor = Executors.newSingleThreadExecutor();
        googleMonitorExecutor.submit(checkGoogleConnect);

    }

    private void loadConfig() {


        try {

            File configFile = new File(CONFIG_PATH);

            if (!configFile.exists()) {
                Logger.warn("Config file not found!");
                return;
            }

            faiConfigSaved.setGlyphName("CHECK");
            lbConfigSaved.setText("Config Saved ...");

            @Cleanup InputStream input = new FileInputStream(configFile);
            @Cleanup JsonReader reader = new JsonReader(new InputStreamReader(input));
            GuiConfig config = new Gson().fromJson(reader, GuiConfig.class);
            if (config == null) return;

            Logger.info(config);

            tfServer.setText(config.getServerAddress());
            tfServerPort.setText(config.getServerPort() + "");
            pfPassword.setText(config.getServerPassword());
            tfLocalPort.setText(config.getLocalSocks5Port() + "");
            tfDownloadMbps.setText(config.getDownloadMbps() + "");
            tfUploadMbps.setText(config.getUploadMbps() + "");
            tfMTU.setText(config.getMTU() != null ? config.getMTU() + "" : "");

        } catch (IOException e) {
            Logger.error(e, "Load Config Error");
        }

    }

    public void saveConfig() {

        try {

            File configFile = new File(CONFIG_PATH);
            if (!configFile.exists()) {
                Logger.warn("Config file not found!");
                if (configFile.createNewFile()) {
                    Logger.info("Config file crate success!");
                } else {
                    Logger.error("Config file create failed!");
                    return;
                }
            }

            @Cleanup OutputStream out = new FileOutputStream(configFile);
            @Cleanup JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            GuiConfig config = new GuiConfig()
                    .setServerAddress(StringUtils.isNotBlank(tfServer.getText()) ? tfServer.getText() : "google.com")
                    .setServerPort(StringUtils.isNotBlank(tfServerPort.getText()) ? Integer.parseInt(tfServerPort.getText()) : 5234)
                    .setServerPassword(StringUtils.isNotBlank(pfPassword.getText()) ? pfPassword.getText() : "jFThJnp2hppzzPJy")
                    .setLocalSocks5Port(StringUtils.isNotBlank(tfLocalPort.getText()) ? Integer.parseInt(tfLocalPort.getText()) : 1080)
                    .setDownloadMbps(StringUtils.isNotBlank(tfDownloadMbps.getText()) ? Integer.parseInt(tfDownloadMbps.getText()) : 100)
                    .setUploadMbps(StringUtils.isNotBlank(tfUploadMbps.getText()) ? Integer.parseInt(tfUploadMbps.getText()) : 10)
                    .setMTU(StringUtils.isNotBlank(tfMTU.getText()) ? Integer.parseInt(tfMTU.getText()) : 1300);

            Logger.info(config);

            new Gson().toJson(config, new TypeToken<GuiConfig>() {
            }.getType(), writer);
            writer.flush();
            faiConfigSaved.setGlyphName("CHECK");
            lbConfigSaved.setText("Config Saved ...");

        } catch (IOException e) {
            Logger.error(e, "Config save Failed");
        }
    }


    private boolean isNotNumeric(String str) {
        String regEx = "^[0-9]+$";
        return !Pattern.compile(regEx).matcher(str).find();
    }
}


