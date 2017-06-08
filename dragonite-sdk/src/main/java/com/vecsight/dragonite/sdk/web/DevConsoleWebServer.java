package com.vecsight.dragonite.sdk.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DevConsoleWebServer {

    private static final int SHUTDOWN_SEC = 4;

    private final HttpServer httpServer;

    private final StatisticsProvider statisticsProvider;

    public DevConsoleWebServer(final StatisticsProvider provider) throws IOException {
        this.statisticsProvider = provider;
        httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 6000), 0);
        httpServer.createContext("/statistics", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                
            }
        })
    }

    public void stop() {
        httpServer.stop(SHUTDOWN_SEC);
    }

}
