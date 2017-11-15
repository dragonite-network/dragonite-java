/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.config;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import com.vecsight.dragonite.proxy.acl.ACLFileParser;
import com.vecsight.dragonite.proxy.acl.ParsedACL;
import com.vecsight.dragonite.proxy.exception.ACLException;
import com.vecsight.dragonite.proxy.exception.JSONConfigException;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.exception.EncryptionException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.utils.network.FileUtils;
import com.vecsight.dragonite.utils.type.UnitConverter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProxyJSONConfigParser {

    private final JsonObject jsonObject;

    public ProxyJSONConfigParser(final JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public ProxyJSONConfigParser(final String file) throws IOException, JSONConfigException {
        final String content = new String(Files.readAllBytes(Paths.get(file)));
        try {
            this.jsonObject = Json.parse(content).asObject();
        } catch (final ParseException | UnsupportedOperationException e) {
            throw new JSONConfigException("JSON Syntax Error");
        }
    }

    public boolean isServerConfig() {
        return jsonObject.getBoolean("server", false);
    }

    public ProxyServerConfig getServerConfig() throws JSONConfigException {
        final String addr = jsonObject.getString("addr", null);
        final int port = jsonObject.getInt("port", ProxyGlobalConstants.DEFAULT_SERVER_PORT);
        final String password = jsonObject.getString("password", null);
        if (password == null) throw new JSONConfigException("Field \"password\" invalid or not found");
        //that's all required

        try {
            final ProxyServerConfig config = new ProxyServerConfig(new InetSocketAddress(addr == null ? null : InetAddress.getByName(addr), port),
                    password);

            final int limit = jsonObject.getInt("limit", 0);
            if (limit != 0) config.setMbpsLimit(limit);

            final String welcome = jsonObject.getString("welcome", null);
            if (welcome != null) config.setWelcomeMessage(welcome);

            final boolean loopback = jsonObject.getBoolean("loopback", false);
            if (loopback) config.setAllowLoopback(true);

            final int mtu = jsonObject.getInt("mtu", 0);
            if (mtu != 0) config.setMTU(mtu);

            final int wndmlt = jsonObject.getInt("multiplier", 0);
            if (wndmlt != 0) config.setWindowMultiplier(wndmlt);

            final int dscp = jsonObject.getInt("dscp", 0);
            if (dscp != 0) config.setTrafficClass(UnitConverter.DSCPtoTrafficClass(dscp));

            final boolean enablePanel = jsonObject.getBoolean("webpanel", false);
            if (enablePanel) {
                config.setWebPanelEnabled(true);
                final String panelAddr = jsonObject.getString("paneladdr", null);
                final int panelPort = jsonObject.getInt("panelport", DragoniteGlobalConstants.WEB_PANEL_PORT);
                if (panelAddr == null) {
                    config.setWebPanelBind(new InetSocketAddress(InetAddress.getLoopbackAddress(), panelPort));
                } else {
                    config.setWebPanelBind(new InetSocketAddress(panelAddr, panelPort));
                }
            }

            return config;

        } catch (final IllegalArgumentException | UnknownHostException | EncryptionException e) {
            throw new JSONConfigException("Failed to create configuration: " + e.getMessage());
        }
    }

    public ProxyClientConfig getClientConfig() throws JSONConfigException {
        final String addr = jsonObject.getString("addr", null);
        if (addr == null) throw new JSONConfigException("Field \"addr\" invalid or not found");
        final int port = jsonObject.getInt("port", ProxyGlobalConstants.DEFAULT_SERVER_PORT);
        final int socks5port = jsonObject.getInt("socks5port", ProxyGlobalConstants.SOCKS5_PORT);
        final String password = jsonObject.getString("password", null);
        if (password == null) throw new JSONConfigException("Field \"password\" invalid or not found");
        final int up = jsonObject.getInt("up", 0);
        if (up == 0) throw new JSONConfigException("Field \"up\" invalid or not found");
        final int down = jsonObject.getInt("down", 0);
        if (down == 0) throw new JSONConfigException("Field \"down\" invalid or not found");
        //that's all required

        try {
            final ProxyClientConfig config = new ProxyClientConfig(new InetSocketAddress(InetAddress.getByName(addr), port), socks5port, password, down, up);

            final String aclPath = jsonObject.getString("acl", null);
            final ParsedACL parsedACL;
            if (aclPath != null) {
                try {
                    parsedACL = ACLFileParser.parse(FileUtils.pathToReader(aclPath));
                    config.setAcl(parsedACL);
                } catch (final IOException | ACLException e) {
                    throw new JSONConfigException(e.getMessage());
                }
            }

            final int mtu = jsonObject.getInt("mtu", 0);
            if (mtu != 0) config.setMTU(mtu);

            final int wndmlt = jsonObject.getInt("multiplier", 0);
            if (wndmlt != 0) config.setWindowMultiplier(wndmlt);

            final int dscp = jsonObject.getInt("dscp", 0);
            if (dscp != 0) config.setTrafficClass(UnitConverter.DSCPtoTrafficClass(dscp));

            final boolean enablePanel = jsonObject.getBoolean("webpanel", false);
            if (enablePanel) {
                config.setWebPanelEnabled(true);
                final String panelAddr = jsonObject.getString("paneladdr", null);
                final int panelPort = jsonObject.getInt("panelport", DragoniteGlobalConstants.WEB_PANEL_PORT);
                if (panelAddr == null) {
                    config.setWebPanelBind(new InetSocketAddress(InetAddress.getLoopbackAddress(), panelPort));
                } else {
                    config.setWebPanelBind(new InetSocketAddress(panelAddr, panelPort));
                }
            }

            return config;

        } catch (final IllegalArgumentException | UnknownHostException | EncryptionException e) {
            throw new JSONConfigException("Failed to create configuration: " + e.getMessage());
        }
    }


}
