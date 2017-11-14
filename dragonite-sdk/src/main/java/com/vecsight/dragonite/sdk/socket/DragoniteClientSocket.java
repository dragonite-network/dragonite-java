/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.MessageParser;
import com.vecsight.dragonite.sdk.web.DevConsoleWebServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DragoniteClientSocket extends DragoniteSocket {

    //From parameters
    private final int packetSize, maxPacketBufferSize;
    private final int windowMultiplier;
    private final int resendMinDelayMS;
    private final int heartbeatIntervalSec, receiveTimeoutSec;
    private final boolean autoSplit;
    private final boolean enableWebPanel;
    private final PacketCryptor packetCryptor;
    private final int cryptorOverhead;
    //end

    private final Thread receiveThread; //THREAD

    private final Thread handleThread; //THREAD

    private final Thread aliveDetectThread; //THREAD

    private volatile boolean doReceive = true, doHandle = true, doAliveDetect = true;

    private final DatagramSocket datagramSocket;

    private final SocketAddress remoteAddress;

    private final BlockingQueue<DatagramPacket> packetBuffer;

    private final ManagedSendAction managedSendAction;

    private final ConnectionReceiveHandler receiver;

    private final ConnectionResendHandler resender; //THREAD

    private final ConnectionSendHandler sender;

    private final ACKMessageManager ackMessageManager; //THREAD

    private final ConnectionSharedData sharedData = new ConnectionSharedData();

    private volatile boolean alive = true;

    private volatile long lastReceiveTime, lastSendTime;

    private final Object closeLock = new Object();

    private volatile String description;

    private final DevConsoleWebServer devConsoleWebServer;

    private final InetSocketAddress devConsoleBindAddress;

    public DragoniteClientSocket(final SocketAddress remoteAddress, final long sendSpeed, final DragoniteSocketParameters parameters) throws SocketException {
        this.remoteAddress = remoteAddress;
        datagramSocket = new DatagramSocket();

        //set from parameters
        packetSize = parameters.getPacketSize();
        maxPacketBufferSize = parameters.getMaxPacketBufferSize();
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

        if (maxPacketBufferSize == 0) {
            packetBuffer = new LinkedBlockingQueue<>();
        } else {
            packetBuffer = new LinkedBlockingQueue<>(maxPacketBufferSize);
        }

        updateLastReceiveTime();

        managedSendAction = new ManagedSendAction(bytes -> sendPacket(bytes, remoteAddress), sendSpeed);

        ackMessageManager = new ACKMessageManager(this, managedSendAction, DragoniteGlobalConstants.ACK_INTERVAL_MS, packetSize);

        resender = new ConnectionResendHandler(this, managedSendAction, sharedData, resendMinDelayMS, DragoniteGlobalConstants.ACK_INTERVAL_MS);

        receiver = new ConnectionReceiveHandler(this, ackMessageManager, sharedData, windowMultiplier,
                resender, packetSize);

        sender = new ConnectionSendHandler(this, managedSendAction, receiver, sharedData, resender, packetSize);

        description = "DCSocket";

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
                //okay
            }
        }, "DC-Receive");
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
        }, "DC-PacketHandle");
        handleThread.start();

        aliveDetectThread = new Thread(() -> {
            try {
                while (doAliveDetect) {
                    final long current = System.currentTimeMillis();
                    if (alive) {
                        if (current - getLastReceiveTime() > receiveTimeoutSec * 1000) {
                            destroy();
                        } else if (current - getLastSendTime() > heartbeatIntervalSec * 1000) {
                            try {
                                //TODO Fix blocking
                                sendHeartbeat();
                            } catch (IOException | SenderClosedException ignored) {
                            }
                        }
                    } else {
                        doAliveDetect = false;
                    }
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException ignored) {
                //okay
            }
        }, "DC-AliveDetect");
        aliveDetectThread.start();

        DevConsoleWebServer tmpServer = null;
        if (enableWebPanel) {
            try {
                tmpServer = new DevConsoleWebServer(devConsoleBindAddress, () -> {
                    final ArrayList<DragoniteSocketStatistics> list = new ArrayList<>(1);
                    list.add(getStatistics());
                    return list;
                });
            } catch (final IOException ignored) {
            }
        }
        devConsoleWebServer = tmpServer;

    }

    private void handlePacket(final DatagramPacket packet) {

        Message message = null;
        try {
            final byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
            final byte[] data = packetCryptor != null ? packetCryptor.decrypt(packetData) : packetData;
            if (data != null) message = MessageParser.parseMessage(data);
        } catch (final IncorrectMessageException ignored) {
        }

        if (message != null) {
            receiver.onHandleMessage(message, packet.getLength());
        }

    }

    private void sendHeartbeat() throws InterruptedException, IOException, SenderClosedException {
        sender.sendHeartbeatMessage();
    }

    //SEND ALL PACKETS THROUGH THIS!!
    private void sendPacket(final byte[] bytes, final SocketAddress socketAddress) throws IOException {
        final byte[] data = packetCryptor != null ? packetCryptor.encrypt(bytes) : bytes;
        if (data != null) {
            final DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(socketAddress);
            datagramSocket.send(packet);
            updateLastSendTime();
        }
    }

    @Override
    public byte[] read() throws InterruptedException, ConnectionNotAliveException {
        return receiver.read();
    }

    @Override
    public void send(final byte[] bytes) throws InterruptedException, IncorrectSizeException, IOException, SenderClosedException {
        if (autoSplit) {
            sender.sendDataMessage_autoSplit(bytes);
        } else {
            sender.sendDataMessage_noSplit(bytes);
        }
    }

    @Override
    public DragoniteSocketStatistics getStatistics() {
        return new DragoniteSocketStatistics(remoteAddress, description,
                sender.getSendLength(), managedSendAction.getSendRawLength(),
                receiver.getReadLength(), receiver.getReceivedRawLength(),
                sharedData.getEstimatedRTT(), sharedData.getDevRTT(),
                resender.getTotalMessageCount(), resender.getResendCount(),
                receiver.getReceivedPktCount(), receiver.getDupPktCount());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    protected void closeSender() {
        sender.stopSend();
    }

    @Override
    public void closeGracefully() throws InterruptedException, IOException, SenderClosedException {
        synchronized (closeLock) {
            if (alive) {
                sender.sendCloseMessage((short) 0, true, true);
                destroy();
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (closeLock) {
            if (alive) {
                alive = false;

                doReceive = false;
                doHandle = false;
                doAliveDetect = false;

                sender.stopSend();
                receiver.close();
                resender.close();
                ackMessageManager.close();

                receiveThread.interrupt();
                handleThread.interrupt();
                aliveDetectThread.interrupt();

                packetBuffer.clear();

                datagramSocket.close();

                if (devConsoleWebServer != null) {
                    devConsoleWebServer.stop();
                }
            }
        }
    }

    @Override
    protected void updateLastReceiveTime() {
        lastReceiveTime = System.currentTimeMillis();
    }

    @Override
    public long getLastReceiveTime() {
        return lastReceiveTime;
    }

    @Override
    protected void updateLastSendTime() {
        lastSendTime = System.currentTimeMillis();
    }

    @Override
    public long getLastSendTime() {
        return lastSendTime;
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return remoteAddress;
    }

    @Override
    public void setSendSpeed(final long sendSpeed) {
        managedSendAction.setSpeed(sendSpeed);
    }

    @Override
    public long getSendSpeed() {
        return managedSendAction.getSpeed();
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getMaxPacketBufferSize() {
        return maxPacketBufferSize;
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
}
