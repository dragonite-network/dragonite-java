package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DragoniteServer {

    //From parameters
    private final int packetSize, maxReceiveWindow;
    private final int ackIntervalMS;
    private final int aggressiveWindowMultiplier, passiveWindowMultiplier;
    private final int resendMinDelayMS;
    private final int heartbeatIntervalSec, receiveTimeoutSec;
    private final boolean autoSplit;
    private final boolean enableWebPanel;
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

    public DragoniteServer(InetAddress address, int port, long defaultSendSpeed, DragoniteSocketParameters parameters) throws SocketException {
        datagramSocket = new DatagramSocket(port, address);

        //set from parameters
        packetSize = parameters.getPacketSize();
        maxReceiveWindow = parameters.getMaxPacketBufferSize();
        ackIntervalMS = parameters.getAckIntervalMS();
        aggressiveWindowMultiplier = parameters.getAggressiveWindowMultiplier();
        passiveWindowMultiplier = parameters.getPassiveWindowMultiplier();
        resendMinDelayMS = parameters.getResendMinDelayMS();
        heartbeatIntervalSec = parameters.getHeartbeatIntervalSec();
        receiveTimeoutSec = parameters.getReceiveTimeoutSec();
        autoSplit = parameters.isAutoSplit();
        enableWebPanel = parameters.isEnableWebPanel();
        devConsoleBindAddress = parameters.getWebPanelBindAddress();
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
                    byte[] b = new byte[packetSize];
                    DatagramPacket packet = new DatagramPacket(b, b.length);
                    try {
                        datagramSocket.receive(packet);
                        packetBuffer.put(packet);
                    } catch (IOException ignored) {
                    }
                }
            } catch (InterruptedException ignored) {
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
            } catch (InterruptedException ignored) {
                //okay
            }
        }, "DS-PacketHandle");
        handleThread.start();

        aliveDetectThread = new Thread(() -> {
            try {
                while (doAliveDetect) {
                    final long current = System.currentTimeMillis();
                    Iterator<Map.Entry<SocketAddress, DragoniteServerSocket>> it = connectionMap_concurrent.entrySet().iterator();
                    while (it.hasNext()) {
                        DragoniteServerSocket socket = it.next().getValue();
                        if (socket.isAlive()) {
                            if (current - socket.getLastReceiveTime() > receiveTimeoutSec * 1000) {
                                //TIMEOUT
                                //System.out.println("TIMEOUT " + socket.getRemoteSocketAddress());
                                socket.destroy_NoRemove();
                                it.remove();
                            } else if (current - socket.getLastSendHeartbeat() > heartbeatIntervalSec * 1000) {
                                try {
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
            } catch (InterruptedException ignored) {
                //okay
            }
        }, "DS-AliveDetect");
        aliveDetectThread.start();

        DevConsoleWebServer tmpServer = null;
        if (enableWebPanel) {
            try {
                tmpServer = new DevConsoleWebServer(devConsoleBindAddress, () -> {
                    final ArrayList<DragoniteSocketStatistics> list = new ArrayList<>();
                    for (DragoniteServerSocket socket : connectionMap_concurrent.values()) {
                        list.add(socket.getStatistics());
                    }
                    return list;
                });
            } catch (IOException ignored) {
            }
        }
        devConsoleWebServer = tmpServer;
    }

    public DragoniteServer(int port, long defaultSendSpeed, DragoniteSocketParameters parameters) throws SocketException {
        this(null, port, defaultSendSpeed, parameters);
    }

    public DragoniteSocket accept() throws InterruptedException {
        return acceptQueue.take();
    }

    private void handlePacket(final DatagramPacket packet) throws InterruptedException {
        SocketAddress remoteAddress = packet.getSocketAddress();
        Message message = null;
        try {
            message = MessageParser.parseMessage(packet.getData());
        } catch (IncorrectMessageException ignored) {
        }

        if (message != null) {
            DragoniteServerSocket dragoniteServerSocket = connectionMap_concurrent.get(remoteAddress);
            if (dragoniteServerSocket == null) {
                //no connection yet
                if (message instanceof ReliableMessage) {
                    if (message instanceof CloseMessage) {
                        ACKMessage ackMessage = new ACKMessage(new int[]{((CloseMessage) message).getSequence()}, 0);
                        try {
                            sendPacket(ackMessage.toBytes(), remoteAddress);
                        } catch (IOException ignored) {
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

    private DragoniteServerSocket createConnection(SocketAddress remoteAddress, long sendSpeed) {
        DragoniteServerSocket socket = new DragoniteServerSocket(remoteAddress, sendSpeed, this);
        connectionMap_concurrent.put(remoteAddress, socket);
        return socket;
    }

    protected void removeConnectionFromMap(SocketAddress remoteAddress) {
        connectionMap_concurrent.remove(remoteAddress);
    }

    //SEND ALL PACKETS THROUGH THIS!!
    protected void sendPacket(byte[] bytes, SocketAddress socketAddress) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setSocketAddress(socketAddress);
        datagramSocket.send(packet);
    }

    public long getDefaultSendSpeed() {
        return defaultSendSpeed;
    }

    public void setDefaultSendSpeed(long defaultSendSpeed) {
        this.defaultSendSpeed = defaultSendSpeed;
    }

    public int getAckIntervalMS() {
        return ackIntervalMS;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getAggressiveWindowMultiplier() {
        return aggressiveWindowMultiplier;
    }

    public int getPassiveWindowMultiplier() {
        return passiveWindowMultiplier;
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

                Iterator<Map.Entry<SocketAddress, DragoniteServerSocket>> it = connectionMap_concurrent.entrySet().iterator();
                while (it.hasNext()) {
                    DragoniteServerSocket socket = it.next().getValue();
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
