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

package com.vecsight.dragonite.proxy.network.client;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ParsedACL;
import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.proxy.header.udp.ProxyUDPRelayHeader;
import com.vecsight.dragonite.proxy.header.udp.SOCKS5UDPRelayHeader;
import com.vecsight.dragonite.proxy.misc.PacketCryptor;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

public class ProxyClientUDPRelay {

    private static final int PACKET_BUFFER_SIZE = 65536;

    private final InetAddress clientAddress;

    private final SocketAddress serverUdpSocketAddress;

    private int clientPort = -1;

    private final PacketCryptor packetCryptor;

    private final DatagramSocket datagramSocket;

    private volatile boolean running = true;

    private final Thread relayThread;

    private final SecureRandom random = new SecureRandom();

    private final ParsedACL acl;

    public ProxyClientUDPRelay(final SocketAddress clientSocketAddress, final SocketAddress serverUdpSocketAddress,
                               final byte[] encryptionKey, final ParsedACL acl) throws SocketException, EncryptionException {
        this.clientAddress = ((InetSocketAddress) clientSocketAddress).getAddress();
        this.serverUdpSocketAddress = serverUdpSocketAddress;
        this.packetCryptor = new PacketCryptor(encryptionKey);
        this.acl = acl;
        this.datagramSocket = new DatagramSocket();
        this.relayThread = new Thread(() -> {
            while (running) {
                final byte[] b = new byte[PACKET_BUFFER_SIZE];
                final DatagramPacket packet = new DatagramPacket(b, b.length);
                try {
                    datagramSocket.receive(packet);
                    if (packet.getAddress().equals(clientAddress)) { //Only IP, port can be random
                        handleClientPacket(packet);
                    } else if (packet.getSocketAddress().equals(serverUdpSocketAddress)) {
                        handleProxyServerPacket(packet);
                    } else {
                        handleDirectRemotePacket(packet);
                    }
                } catch (final IOException ignored) {
                }
            }
        }, "PC-UDPRelay");
        this.relayThread.start();
    }

    private void handleClientPacket(final DatagramPacket packet) {
        final SOCKS5UDPRelayHeader socks5UDPRelayHeader;
        try {
            socks5UDPRelayHeader = new SOCKS5UDPRelayHeader(Arrays.copyOf(packet.getData(), packet.getLength()));
        } catch (final IncorrectHeaderException e) {
            Logger.error(e, "Incorrect SOCKS5 UDP relay header");
            return;
        }

        //Set client port
        clientPort = packet.getPort();

        final ACLItemMethod method;
        if (acl != null) {
            if (socks5UDPRelayHeader.getType() == AddressType.DOMAIN) {
                method = acl.checkDomain(new String(socks5UDPRelayHeader.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            } else {
                method = acl.checkIP(socks5UDPRelayHeader.getAddr());
            }
        } else {
            method = ACLItemMethod.PROXY;
        }

        if (method == ACLItemMethod.PROXY) {
            handleClientPacketThroughProxy(socks5UDPRelayHeader);
        } else if (method == ACLItemMethod.DIRECT) {
            handleClientPacketThoughLocalDirect(socks5UDPRelayHeader);
        } // or we just drop it
    }

    private void handleClientPacketThoughLocalDirect(final SOCKS5UDPRelayHeader socks5UDPRelayHeader) {
        final InetAddress remoteAddress;
        try {
            if (socks5UDPRelayHeader.getType() == AddressType.IPv4 || socks5UDPRelayHeader.getType() == AddressType.IPv6) {
                remoteAddress = InetAddress.getByAddress(socks5UDPRelayHeader.getAddr());
            } else {
                remoteAddress = InetAddress.getByName(new String(socks5UDPRelayHeader.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            }
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host from SOCKS5 UDP relay header");
            return;
        }

        final byte[] sendBytes = socks5UDPRelayHeader.getPayload();

        final DatagramPacket remotePacket = new DatagramPacket(sendBytes, sendBytes.length,
                new InetSocketAddress(remoteAddress, socks5UDPRelayHeader.getPort()));

        try {
            datagramSocket.send(remotePacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP packet to remote ({})", remotePacket.getSocketAddress());
        }
    }

    private void handleClientPacketThroughProxy(final SOCKS5UDPRelayHeader socks5UDPRelayHeader) {
        final ProxyUDPRelayHeader proxyUDPRelayHeader = new ProxyUDPRelayHeader(socks5UDPRelayHeader.getType(),
                socks5UDPRelayHeader.getAddr(), socks5UDPRelayHeader.getPort(), socks5UDPRelayHeader.getPayload());
        final byte[] proxyUDPRelayHeaderBytes = proxyUDPRelayHeader.toBytes();

        final ByteBuffer buffer = ByteBuffer.allocate(16 + proxyUDPRelayHeaderBytes.length);

        final byte[] iv = new byte[ProxyGlobalConstants.IV_LENGTH];
        random.nextBytes(iv);

        buffer.put(iv)
                .put(packetCryptor.encrypt(proxyUDPRelayHeaderBytes, iv));

        final byte[] sendBytes = buffer.array();

        final DatagramPacket remotePacket = new DatagramPacket(sendBytes, sendBytes.length, serverUdpSocketAddress);

        try {
            datagramSocket.send(remotePacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP relay packet to proxy server ({})", serverUdpSocketAddress);
        }
    }

    private void handleProxyServerPacket(final DatagramPacket packet) {
        if (clientPort == -1) {
            //We don't know client port yet
            return;
        }

        if (packet.getLength() < ProxyGlobalConstants.IV_LENGTH) return;

        final byte[] iv = Arrays.copyOf(packet.getData(), ProxyGlobalConstants.IV_LENGTH);
        final byte[] content = Arrays.copyOfRange(packet.getData(), ProxyGlobalConstants.IV_LENGTH, packet.getLength());
        final byte[] decrypted = packetCryptor.decrypt(content, iv);

        if (decrypted == null) return;

        final ProxyUDPRelayHeader relayHeader;
        try {
            relayHeader = new ProxyUDPRelayHeader(decrypted);
        } catch (final IncorrectHeaderException e) {
            Logger.error(e, "Incorrect UDP relay header from proxy server ({})", serverUdpSocketAddress);
            return;
        }

        final SOCKS5UDPRelayHeader socks5UDPRelayHeader = new SOCKS5UDPRelayHeader(relayHeader.getType(), relayHeader.getAddr(),
                relayHeader.getPort(), relayHeader.getPayload());
        final byte[] sendBytes = socks5UDPRelayHeader.toBytes();

        final DatagramPacket localPacket = new DatagramPacket(sendBytes, sendBytes.length,
                new InetSocketAddress(clientAddress, clientPort));

        try {
            datagramSocket.send(localPacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP relay packet to client");
        }
    }

    private void handleDirectRemotePacket(final DatagramPacket packet) {
        if (clientPort == -1) {
            //We don't know client port yet
            return;
        }

        final byte[] address = packet.getAddress().getAddress();
        final AddressType addressType = address.length == 4 ? AddressType.IPv4 : AddressType.IPv6;

        final SOCKS5UDPRelayHeader socks5UDPRelayHeader = new SOCKS5UDPRelayHeader(addressType, address,
                packet.getPort(), Arrays.copyOf(packet.getData(), packet.getLength()));
        final byte[] sendBytes = socks5UDPRelayHeader.toBytes();

        final DatagramPacket localPacket = new DatagramPacket(sendBytes, sendBytes.length,
                new InetSocketAddress(clientAddress, clientPort));

        try {
            datagramSocket.send(localPacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP relay packet to client");
        }
    }

    public void stop() {
        running = false;
        relayThread.interrupt();
        datagramSocket.close();
    }

    public int getLocalPort() {
        return datagramSocket.getLocalPort();
    }

}
