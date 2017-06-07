package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.config.DragoniteSocketParameters;
import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.MessageParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DragoniteClientSocket extends DragoniteSocket {

    //From parameters
    private final int packetSize, maxPacketBufferSize;
    private final int ackIntervalMS;
    private final int aggressiveWindowMultiplier, passiveWindowMultiplier;
    private final int resendMinDelayMS;
    private final int heartbeatIntervalSec, receiveTimeoutSec;
    private final boolean autoSplit;
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

    private volatile long lastReceiveTime, lastSendHeartbeat;

    private final Object closeLock = new Object();

    public DragoniteClientSocket(SocketAddress remoteAddress, long sendSpeed, DragoniteSocketParameters parameters) throws SocketException {
        this.remoteAddress = remoteAddress;
        datagramSocket = new DatagramSocket();

        //set from parameters
        packetSize = parameters.getPacketSize();
        maxPacketBufferSize = parameters.getMaxPacketBufferSize();
        ackIntervalMS = parameters.getAckIntervalMS();
        aggressiveWindowMultiplier = parameters.getAggressiveWindowMultiplier();
        passiveWindowMultiplier = parameters.getPassiveWindowMultiplier();
        resendMinDelayMS = parameters.getResendMinDelayMS();
        heartbeatIntervalSec = parameters.getHeartbeatIntervalSec();
        receiveTimeoutSec = parameters.getReceiveTimeoutSec();
        autoSplit = parameters.isAutoSplit();
        //end

        if (maxPacketBufferSize == 0) {
            packetBuffer = new LinkedBlockingQueue<>();
        } else {
            packetBuffer = new LinkedBlockingQueue<>(maxPacketBufferSize);
        }

        updateLastReceiveTime();

        managedSendAction = new ManagedSendAction(bytes -> sendPacket(bytes, remoteAddress), sendSpeed);

        ackMessageManager = new ACKMessageManager(this, managedSendAction, ackIntervalMS, packetSize);

        resender = new ConnectionResendHandler(this, managedSendAction, sharedData, resendMinDelayMS);

        receiver = new ConnectionReceiveHandler(this, ackMessageManager, sharedData, aggressiveWindowMultiplier,
                passiveWindowMultiplier, resender, packetSize);

        sender = new ConnectionSendHandler(this, managedSendAction, receiver, sharedData, resender, packetSize);

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
            } catch (InterruptedException ignored) {
                //okay
            }
        }, "DC-PacketHandle");
        handleThread.start();

        aliveDetectThread = new Thread(() -> {
            try {
                while (doAliveDetect) {
                    final long current = System.currentTimeMillis();
                    if (alive) {
                        if (current - lastReceiveTime > receiveTimeoutSec * 1000) {
                            destroy();
                        } else if (current - lastSendHeartbeat > heartbeatIntervalSec * 1000) {
                            try {
                                sendHeartbeat();
                            } catch (IOException | SenderClosedException ignored) {
                            }
                        }
                    } else {
                        doAliveDetect = false;
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
                //okay
            }
        }, "DC-AliveDetect");
        aliveDetectThread.start();

    }

    private void handlePacket(final DatagramPacket packet) {

        Message message = null;
        try {
            message = MessageParser.parseMessage(packet.getData());
        } catch (IncorrectMessageException ignored) {
        }

        if (message != null) {
            receiver.onHandleMessage(message, packet.getLength());
        }

    }

    private void sendHeartbeat() throws InterruptedException, IOException, SenderClosedException {
        sender.sendHeartbeatMessage();
        lastSendHeartbeat = System.currentTimeMillis();
    }

    //SEND ALL PACKETS THROUGH THIS!!
    protected void sendPacket(byte[] bytes, SocketAddress socketAddress) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setSocketAddress(socketAddress);
        datagramSocket.send(packet);
    }

    @Override
    public byte[] read() throws InterruptedException, ConnectionNotAliveException {
        return receiver.read();
    }

    @Override
    public void send(byte[] bytes) throws InterruptedException, IncorrectSizeException, IOException, SenderClosedException {
        if (autoSplit) {
            sender.sendDataMessage_autoSplit(bytes);
        } else {
            sender.sendDataMessage_noSplit(bytes);
        }
    }

    @Override
    public DragoniteSocketStatistics getStatistics() {
        return new DragoniteSocketStatistics(sender.getSendLength(), managedSendAction.getSendRawLength(),
                receiver.getReadLength(), receiver.getReceivedRawLength(),
                sharedData.getEstimatedRTT(), sharedData.getDevRTT(),
                resender.getResendRate(), receiver.getDuplicateRate());
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
    public SocketAddress getRemoteSocketAddress() {
        return remoteAddress;
    }

    @Override
    public void setSendSpeed(long sendSpeed) {
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

    public int getAckIntervalMS() {
        return ackIntervalMS;
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
}
