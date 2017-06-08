package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.msg.Message;

import java.io.IOException;
import java.net.SocketAddress;

public class DragoniteServerSocket extends DragoniteSocket {

    private final ConnectionReceiveHandler receiver;

    private final ConnectionResendHandler resender; //THREAD

    private final ConnectionSendHandler sender;

    private final ManagedSendAction managedSendAction;

    private final ACKMessageManager ackMessageManager; //THREAD

    private final SocketAddress remoteAddress;

    private final DragoniteServer dragoniteServer;

    private volatile boolean alive = true;

    private volatile long lastReceiveTime, lastSendHeartbeat;

    private final ConnectionSharedData sharedData = new ConnectionSharedData();

    private final Object closeLock = new Object();

    private volatile String description;

    public DragoniteServerSocket(SocketAddress remoteAddress, long sendSpeed, DragoniteServer dragoniteServer) {
        this.remoteAddress = remoteAddress;
        this.dragoniteServer = dragoniteServer;

        updateLastReceiveTime();

        managedSendAction = new ManagedSendAction(bytes -> dragoniteServer.sendPacket(bytes, remoteAddress), sendSpeed);

        ackMessageManager = new ACKMessageManager(this, managedSendAction, dragoniteServer.getAckIntervalMS(), dragoniteServer.getPacketSize());

        resender = new ConnectionResendHandler(this, managedSendAction, sharedData, dragoniteServer.getResendMinDelayMS());

        receiver = new ConnectionReceiveHandler(this, ackMessageManager, sharedData, dragoniteServer.getAggressiveWindowMultiplier(),
                dragoniteServer.getPassiveWindowMultiplier(), resender, dragoniteServer.getPacketSize());

        sender = new ConnectionSendHandler(this, managedSendAction, receiver, sharedData, resender, dragoniteServer.getPacketSize());

    }

    protected void onHandleMessage(final Message message, final int pktLength) {
        receiver.onHandleMessage(message, pktLength);
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
    public byte[] read() throws InterruptedException, ConnectionNotAliveException {
        return receiver.read();
    }

    @Override
    public void send(byte[] bytes) throws InterruptedException, IncorrectSizeException, IOException, SenderClosedException {
        if (dragoniteServer.isAutoSplit()) {
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
                resender.getResendRate(), receiver.getDuplicateRate());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return remoteAddress;
    }

    @Override
    public long getSendSpeed() {
        return managedSendAction.getSpeed();
    }

    @Override
    public void setSendSpeed(long sendSpeed) {
        managedSendAction.setSpeed(sendSpeed);
    }

    protected void sendHeartbeat() throws InterruptedException, IOException, SenderClosedException {
        sender.sendHeartbeatMessage();
        lastSendHeartbeat = System.currentTimeMillis();
    }

    protected long getLastSendHeartbeat() {
        return lastSendHeartbeat;
    }

    @Override
    protected void closeSender() {
        sender.stopSend();
    }

    @Override
    public void closeGracefully() throws InterruptedException, IOException, SenderClosedException {
        //send close msg
        //tick send
        //still receive
        //wait for close ack
        //close all
        synchronized (closeLock) {
            if (alive) {
                sender.sendCloseMessage((short) 0, true, true);
                destroy();
            }
        }
    }

    @Override
    public void destroy() {
        destroy_impl(true);
    }

    protected void destroy_NoRemove() {
        destroy_impl(false);
    }

    private void destroy_impl(boolean remove) {
        synchronized (closeLock) {
            if (alive) {
                alive = false;
                sender.stopSend();
                receiver.close();
                resender.close();
                ackMessageManager.close();
                if (remove) dragoniteServer.removeConnectionFromMap(remoteAddress);
            }
        }
    }
}
