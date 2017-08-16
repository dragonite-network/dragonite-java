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

package com.vecsight.dragonite.proxy.network.server;

import com.vecsight.dragonite.proxy.exception.EncryptionException;
import com.vecsight.dragonite.proxy.exception.IncorrectHeaderException;
import com.vecsight.dragonite.proxy.header.AddressType;
import com.vecsight.dragonite.proxy.header.udp.ProxyUDPRelayHeader;
import com.vecsight.dragonite.proxy.misc.PacketCryptor;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

public class ProxyServerUDPRelay {

    private static final int PACKET_BUFFER_SIZE = 65536;

    private final String clientName;

    private final InetAddress clientAddress;

    private int clientPort = -1;

    private final PacketCryptor packetCryptor;

    private final DatagramSocket datagramSocket;

    private volatile boolean running = true;

    private final Thread relayThread;

    private final SecureRandom random = new SecureRandom();

    public ProxyServerUDPRelay(final String clientName, final SocketAddress clientSocketAddress, final byte[] encryptionKey) throws SocketException, EncryptionException {
        this.clientName = clientName;
        this.clientAddress = ((InetSocketAddress) clientSocketAddress).getAddress();
        this.packetCryptor = new PacketCryptor(encryptionKey);
        this.datagramSocket = new DatagramSocket();
        this.relayThread = new Thread(() -> {
            while (running) {
                final byte[] b = new byte[PACKET_BUFFER_SIZE];
                final DatagramPacket packet = new DatagramPacket(b, b.length);
                try {
                    datagramSocket.receive(packet);
                    if (packet.getAddress().equals(clientAddress)) { //Only IP, port can be random
                        handleClientPacket(packet);
                    } else {
                        handleRemotePacket(packet);
                    }
                } catch (final IOException ignored) {
                }
            }
        }, "PS-UDPRelay");
        this.relayThread.start();
    }

    private void handleClientPacket(final DatagramPacket packet) {
        if (packet.getLength() < ProxyGlobalConstants.IV_LENGTH) return;

        final byte[] iv = Arrays.copyOf(packet.getData(), ProxyGlobalConstants.IV_LENGTH);
        final byte[] content = Arrays.copyOfRange(packet.getData(), ProxyGlobalConstants.IV_LENGTH, packet.getLength());
        final byte[] decrypted = packetCryptor.decrypt(content, iv);

        if (decrypted == null) return;

        final ProxyUDPRelayHeader relayHeader;
        try {
            relayHeader = new ProxyUDPRelayHeader(decrypted);
        } catch (final IncorrectHeaderException e) {
            Logger.error(e, "Incorrect UDP relay header from \"{}\" ({})",
                    clientName, clientAddress.toString());
            return;
        }

        //Set client port
        clientPort = packet.getPort();

        final InetAddress remoteAddress;
        try {
            if (relayHeader.getType() == AddressType.IPv4 || relayHeader.getType() == AddressType.IPv6) {
                remoteAddress = InetAddress.getByAddress(relayHeader.getAddr());
            } else {
                remoteAddress = InetAddress.getByName(new String(relayHeader.getAddr(), ProxyGlobalConstants.HEADER_ADDRESS_CHARSET));
            }
        } catch (final UnknownHostException e) {
            Logger.error(e, "Unknown host from UDP relay header of \"{}\" ({})",
                    clientName, clientAddress.toString());
            return;
        }

        final DatagramPacket remotePacket = new DatagramPacket(relayHeader.getPayload(), relayHeader.getPayload().length,
                remoteAddress, relayHeader.getPort());
        try {
            datagramSocket.send(remotePacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP relay packet to remote server for \"{}\" ({})",
                    clientName, clientAddress.toString());
        }
    }

    private void handleRemotePacket(final DatagramPacket packet) {
        if (clientPort == -1) {
            //We don't know client port yet
            return;
        }

        final byte[] address = packet.getAddress().getAddress();
        final AddressType addressType = address.length == 4 ? AddressType.IPv4 : AddressType.IPv6;

        final ProxyUDPRelayHeader relayHeader = new ProxyUDPRelayHeader(addressType, address, packet.getPort(),
                Arrays.copyOf(packet.getData(), packet.getLength()));
        final byte[] relayHeaderBytes = relayHeader.toBytes();

        final ByteBuffer buffer = ByteBuffer.allocate(16 + relayHeaderBytes.length);

        final byte[] iv = new byte[ProxyGlobalConstants.IV_LENGTH];
        random.nextBytes(iv);

        buffer.put(iv)
                .put(packetCryptor.encrypt(relayHeaderBytes, iv));

        final byte[] sendBytes = buffer.array();

        final DatagramPacket localPacket = new DatagramPacket(sendBytes, sendBytes.length,
                new InetSocketAddress(clientAddress, clientPort));

        try {
            datagramSocket.send(localPacket);
        } catch (final IOException e) {
            Logger.error(e, "Failed to send UDP relay packet to \"{}\" ({})",
                    clientName, clientAddress.toString());
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
