package com.vecsight.dragonite.proxy.gui.module;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*******************************************************************************
 * Copyright (c) 2005-2017 Mritd, Inc.
 * dragonite
 * com.vecsight.dragonite.proxy.gui.module
 * Created by mritd on 17/11/28 下午9:38.
 * Description: GuiConfig
 *******************************************************************************/
@Getter
@ToString
@EqualsAndHashCode
public class GuiConfig {
    private String serverAddress;
    private String serverPassword;
    private Integer serverPort;
    private Integer localSocks5Port;
    private Integer downloadMbps;
    private Integer uploadMbps;
    private Integer MTU;

    public GuiConfig() {

    }

    public GuiConfig(String serverAddress, String serverPassword, Integer serverPort, Integer localSocks5Port, Integer downloadMbps, Integer uploadMbps, Integer MTU) {
        this.serverAddress = serverAddress;
        this.serverPassword = serverPassword;
        this.serverPort = serverPort;
        this.localSocks5Port = localSocks5Port;
        this.downloadMbps = downloadMbps;
        this.uploadMbps = uploadMbps;
        this.MTU = MTU;
    }

    public GuiConfig setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        return this;
    }

    public GuiConfig setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
        return this;
    }

    public GuiConfig setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public GuiConfig setLocalSocks5Port(Integer localSocks5Port) {
        this.localSocks5Port = localSocks5Port;
        return this;
    }

    public GuiConfig setDownloadMbps(Integer downloadMbps) {
        this.downloadMbps = downloadMbps;
        return this;
    }

    public GuiConfig setUploadMbps(Integer uploadMbps) {
        this.uploadMbps = uploadMbps;
        return this;
    }

    public GuiConfig setMTU(Integer MTU) {
        this.MTU = MTU;
        return this;
    }
}
