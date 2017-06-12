package com.vecsight.dragonite.sdk.web;

import com.sun.net.httpserver.HttpServer;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.socket.DragoniteSocketStatistics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*

sample:
{
  "title": "Dragonite Developer Web Interface",
  "version": "0.2.0",
  "connections": [
    {
      "remote": "123.123.123.123:54321",
      "description": "someEvilConnection",
      "rtt": 90,
      "devrtt": 5,
      "send": 10240,
      "sendraw": 12680,
      "sendpkt": 400,
      "resend": 60,
      "recv": 204800,
      "recvraw": 219400,
      "recvcount": 3800,
      "dup": 280
    }
  ]
}

 */

public class DevConsoleWebServer {

    private static final String WEB_TITLE = "Dragonite Developer Web Interface";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int SHUTDOWN_SEC = 2;

    private final HttpServer httpServer;

    private final StatisticsProvider statisticsProvider;

    public DevConsoleWebServer(final InetSocketAddress bindAddress, final StatisticsProvider provider) throws IOException {
        this.statisticsProvider = provider;

        httpServer = HttpServer.create(bindAddress, 0);
        httpServer.createContext("/statistics", httpExchange -> {
            final byte[] jsonBytes = getJSON(statisticsProvider.getLatest()).getBytes(CHARSET);
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            httpExchange.sendResponseHeaders(200, jsonBytes.length);
            httpExchange.getResponseBody().write(jsonBytes);
            httpExchange.close();
        });

        httpServer.start();
    }

    private String getJSON(final List<DragoniteSocketStatistics> statisticsList) {
        return "{\"title\":\"" + WEB_TITLE +
                "\",\"version\":\"" + DragoniteGlobalConstants.LIBRARY_VERSION +
                "\",\"connections\":" + getConnectionsJSONArray(statisticsList) + "}";
    }

    private String getConnectionsJSONArray(final List<DragoniteSocketStatistics> statisticsList) {
        String json = "[";
        int i = 0;
        for (DragoniteSocketStatistics statistics : statisticsList) {
            String cinfo = "{\"remote\":\"" + statistics.getRemoteAddress().toString() +
                    "\",\"description\":\"" + statistics.getDescription() +
                    "\",\"rtt\":" + statistics.getEstimatedRTT() +
                    ",\"devrtt\":" + statistics.getDevRTT() +
                    ",\"send\":" + statistics.getSendLength() +
                    ",\"sendraw\":" + statistics.getSendRawLength() +
                    ",\"sendpkt\":" + statistics.getSendCount() +
                    ",\"resend\":" + statistics.getResendCount() +
                    ",\"recv\":" + statistics.getReadLength() +
                    ",\"recvraw\":" + statistics.getReceiveRawLength() +
                    ",\"recvcount\":" + statistics.getReceiveCount() +
                    ",\"dup\":" + statistics.getDupCount() + "}";
            json += cinfo;
            if (++i != statisticsList.size()) {
                json += ",";
            }
        }
        json += "]";
        return json;
    }

    public void stop() {
        httpServer.stop(SHUTDOWN_SEC);
    }

}
