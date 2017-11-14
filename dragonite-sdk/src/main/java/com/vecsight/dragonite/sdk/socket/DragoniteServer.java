/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.MessageParser;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;
import com.vecsight.dragonite.sdk.msg.types.ACKMessage;
import com.vecsight.dragonite.sdk.msg.types.CloseMessage;
import com.vecsight.dragonite.sdk.web.DevConsoleWebServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DragoniteServer {

    //From parameters
    private final int packetSize, maxReceiveWindow;
    private final int windowMultiplier;
    private final int resendMinDelayMS;
    private final int heartbeatIntervalSec, receiveTimeoutSec;
    private final boolean autoSplit;
    private final boolean enableWebPanel;
    private final PacketCryptor packetCryptor;
    private final int cryptorOverhead;
    //end

    private volatile long defaultSendSpeed;

    private final DatagramSocket datagramSocket;

    private final BlockingQueue<DatagramPacket> packetBuffer;

    private final Thread receiveThread; //THREAD

    private final Thread handleThread; //THREAD

    private final Thread aliveDetectThread; //THREAD

    private volatile boolean doReceive = true, doHandle = true, doAliveDetect = true;

    private final ConcurrentMap<SocketAddress, DragoniteServerSocket> connectionMap_concurrent = new ConcurrentHashMap<>();

    private final BlockingQueue<DragoniteSocket> acceptQueue = new LinkedBlockingQueue<>();

    private volatile boolean alive = true;

    private final Object closeLock = new Object();

    private final DevConsoleWebServer devConsoleWebServer;

    private final InetSocketAddress devConsoleBindAddress;

    public DragoniteServer(final InetAddress address, final int port, final long defaultSendSpeed, final DragoniteSocketParameters parameters) throws SocketException {
        datagramSocket = new DatagramSocket(port, address);

        //set from parameters
        packetSize = parameters.getPacketSize();
        maxReceiveWindow = parameters.getMaxPacketBufferSize();
        windowMultiplier = parameters.getWindowMultiplier();
        resendMinDelayMS = parameters.getResendMinDelayMS();
        heartbeatIntervalSec = parameters.getHeartbeatIntervalSec();
        receiveTimeoutSec = parameters.getReceiveTimeoutSec();
        autoSplit = parameters.isAutoSplit();
        enableWebPanel = parameters.isEnableWebPanel();
        devConsoleBindAddress = parameters.getWebPanelBindAddress();
        packetCryptor = parameters.getPacketCryptor();
        cryptorOverhead = packetCryptor != null ? packetCryptor.getMaxAdditionalBytesLength() : 0;

        datagramSocket.setTrafficClass(parameters.getTrafficClass());
        //end

        this.defaultSendSpeed = defaultSendSpeed;

        if (maxReceiveWindow == 0) {
            packetBuffer = new LinkedBlockingQueue<>();
        } else {
            packetBuffer = new LinkedBlockingQueue<>(maxReceiveWindow);
        }

        receiveThread = new Thread(() -> {
            try {
                while (doReceive) {
                    final byte[] b = new byte[packetSize + cryptorOverhead];
                    final DatagramPacket packet = new DatagramPacket(b, b.length);
                    try {
                        datagramSocket.receive(packet);
                        packetBuffer.put(packet);
                    } catch (final IOException ignored) {
                    }
                }
            } catch (final InterruptedException ignored) {
            }
        }, "DS-Receive");
        receiveThread.start();

        handleThread = new Thread(() -> {
            try {
                while (doHandle) {
                    final DatagramPacket packet = packetBuffer.take();
                    if (packet != null) {
                        handlePacket(packet);
                    }
                }
            } catch (final InterruptedException ignored) {
                //okay
            }
        }, "DS-PacketHandle");
        handleThread.start();

        aliveDetectThread = new Thread(() -> {
            try {
                while (doAliveDetect) {
                    final long current = System.currentTimeMillis();
                    final Iterator<Map.Entry<SocketAddress, DragoniteServerSocket>> it = connectionMap_concurrent.entrySet().iterator();
                    while (it.hasNext()) {
                        final DragoniteServerSocket socket = it.next().getValue();
                        if (socket.isAlive()) {
                            if (current - socket.getLastReceiveTime() > receiveTimeoutSec * 1000) {
                                //TIMEOUT
                                //System.out.println("TIMEOUT " + socket.getRemoteSocketAddress());
                                socket.destroy_NoRemove();
                                it.remove();
                            } else if (current - socket.getLastSendTime() > heartbeatIntervalSec * 1000) {
                                try {
                                    //TODO Fix blocking
                                    socket.sendHeartbeat();
                                } catch (IOException | SenderClosedException ignored) {
                                }
                            }
                        } else {
                            it.remove();
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException ignored) {
                //okay
            }
        }, "DS-AliveDetect");
        aliveDetectThread.start();

        DevConsoleWebServer tmpServer = null;
        if (enableWebPanel) {
            try {
                tmpServer = new DevConsoleWebServer(devConsoleBindAddress, () -> {
                    final ArrayList<DragoniteSocketStatistics> list = new ArrayList<>();
                    for (final DragoniteServerSocket socket : connectionMap_concurrent.values()) {
                        list.add(socket.getStatistics());
                    }
                    return list;
                });
            } catch (final IOException ignored) {
            }
        }
        devConsoleWebServer = tmpServer;
    }

    public DragoniteServer(final int port, final long defaultSendSpeed, final DragoniteSocketParameters parameters) throws SocketException {
        this(null, port, defaultSendSpeed, parameters);
    }

    public DragoniteSocket accept() throws InterruptedException {
        return acceptQueue.take();
    }

    private void handlePacket(final DatagramPacket packet) throws InterruptedException {
        final SocketAddress remoteAddress = packet.getSocketAddress();
        Message message = null;
        try {
            final byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
            final byte[] data = packetCryptor != null ? packetCryptor.decrypt(packetData) : packetData;
            if (data != null) message = MessageParser.parseMessage(data);
        } catch (final IncorrectMessageException ignored) {
        }

        if (message != null) {
            DragoniteServerSocket dragoniteServerSocket = connectionMap_concurrent.get(remoteAddress);
            if (dragoniteServerSocket == null) {
                //no connection yet
                if (message instanceof ReliableMessage) {
                    if (message instanceof CloseMessage) {
                        final ACKMessage ackMessage = new ACKMessage(new int[]{((CloseMessage) message).getSequence()}, 0);
                        try {
                            sendPacket(ackMessage.toBytes(), remoteAddress);
                        } catch (final IOException ignored) {
                        }
                    } else if (((ReliableMessage) message).getSequence() == 0) {
                        dragoniteServerSocket = createConnection(remoteAddress, defaultSendSpeed);
                        dragoniteServerSocket.onHandleMessage(message, packet.getLength());

                        acceptQueue.put(dragoniteServerSocket);
                    }
                }
            } else {
                dragoniteServerSocket.onHandleMessage(message, packet.getLength());
            }
        }
    }

    private DragoniteServerSocket createConnection(final SocketAddress remoteAddress, final long sendSpeed) {
        final DragoniteServerSocket socket = new DragoniteServerSocket(remoteAddress, sendSpeed, this);
        connectionMap_concurrent.put(remoteAddress, socket);
        return socket;
    }

    protected void removeConnectionFromMap(final SocketAddress remoteAddress) {
        connectionMap_concurrent.remove(remoteAddress);
    }

    //SEND ALL PACKETS THROUGH THIS!!
    protected void sendPacket(final byte[] bytes, final SocketAddress socketAddress) throws IOException {
        final byte[] data = packetCryptor != null ? packetCryptor.encrypt(bytes) : bytes;
        if (data != null) {
            final DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(socketAddress);
            datagramSocket.send(packet);
        }
    }

    public long getDefaultSendSpeed() {
        return defaultSendSpeed;
    }

    public void setDefaultSendSpeed(final long defaultSendSpeed) {
        this.defaultSendSpeed = defaultSendSpeed;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getWindowMultiplier() {
        return windowMultiplier;
    }

    public int getResendMinDelayMS() {
        return resendMinDelayMS;
    }

    public int getHeartbeatIntervalSec() {
        return heartbeatIntervalSec;
    }

    public int getReceiveTimeoutSec() {
        return receiveTimeoutSec;
    }

    public boolean isAutoSplit() {
        return autoSplit;
    }

    public boolean isEnableWebPanel() {
        return enableWebPanel;
    }

    public void destroy() {
        synchronized (closeLock) {
            if (alive) {

                alive = false;

                doReceive = false;
                doHandle = false;
                doAliveDetect = false;

                final Iterator<Map.Entry<SocketAddress, DragoniteServerSocket>> it = connectionMap_concurrent.entrySet().iterator();
                while (it.hasNext()) {
                    final DragoniteServerSocket socket = it.next().getValue();
                    socket.destroy_NoRemove();
                    it.remove();
                }

                receiveThread.interrupt();
                handleThread.interrupt();
                aliveDetectThread.interrupt();

                packetBuffer.clear();

                datagramSocket.close();

            }
        }
    }

}
