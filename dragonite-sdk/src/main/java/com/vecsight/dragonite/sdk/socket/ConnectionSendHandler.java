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

package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;
import com.vecsight.dragonite.sdk.msg.types.CloseMessage;
import com.vecsight.dragonite.sdk.msg.types.DataMessage;
import com.vecsight.dragonite.sdk.msg.types.HeartbeatMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionSendHandler {

    private final DragoniteSocket socket;

    private final SendAction sendAction;

    private final ConnectionReceiveHandler receiveHandler;

    private final ConnectionSharedData sharedData;

    private final ConnectionResendHandler resender;

    private final int MTU;

    private volatile int sendSequence = 0;

    private final Object sendLock = new Object();

    private volatile boolean stopSend = false;

    private final AtomicLong sendLength = new AtomicLong(0);

    protected ConnectionSendHandler(final DragoniteSocket socket, final SendAction sendAction, final ConnectionReceiveHandler receiveHandler,
                                    final ConnectionSharedData sharedData, final ConnectionResendHandler resender, final int MTU) {
        this.socket = socket;
        this.sendAction = sendAction;
        this.receiveHandler = receiveHandler;
        this.sharedData = sharedData;
        this.resender = resender;
        this.MTU = MTU;
    }

    private void addSendSequence() {
        sendSequence++;
        sharedData.setSendSequence(sendSequence);
    }

    protected void stopSend() {
        stopSend = true;
        synchronized (sharedData.sendWindowLock) {
            sharedData.sendWindowLock.notifyAll();
        }
    }

    private boolean canSend() {
        return socket.isAlive() && !stopSend;
    }

    protected void sendDataMessage_noSplit(final byte[] data) throws IncorrectSizeException, InterruptedException, IOException, SenderClosedException {
        if (canSend()) {
            final int tlength = DataMessage.FIXED_LENGTH + data.length;
            if (tlength > MTU) {
                throw new IncorrectSizeException("Packet is too big (" + tlength + ")");
            }
            synchronized (sharedData.sendWindowLock) {
                while (!receiveHandler.checkWindowAvailable() && canSend()) {
                    sharedData.sendWindowLock.wait();
                }
            }
            //have to check again after wait!
            if (!canSend()) throw new SenderClosedException();

            final DataMessage dataMessage;
            synchronized (sendLock) {
                dataMessage = new DataMessage(sendSequence, data);
                addSendSequence();
            }
            sendAction.sendPacket(dataMessage.toBytes());
            resender.addMessage(dataMessage);
            sendLength.addAndGet(data.length);
        } else {
            throw new SenderClosedException();
        }
    }

    protected boolean sendDataMessage_autoSplit(final byte[] data) throws InterruptedException, IncorrectSizeException, IOException, SenderClosedException {
        final int payloadSize = MTU - DataMessage.FIXED_LENGTH;
        int msgCount = data.length / payloadSize;
        if (data.length % payloadSize != 0) {
            msgCount += 1;
        }
        if (msgCount == 0) msgCount = 1;

        if (msgCount == 1) {
            sendDataMessage_noSplit(data);
            return false;
        } else {
            int offset = 0, nextLen = payloadSize;
            synchronized (sendLock) {
                for (int i = 0; i < msgCount; i++) {
                    final byte[] b = Arrays.copyOfRange(data, offset, offset + nextLen);
                    sendDataMessage_noSplit(b);
                    offset += nextLen;
                    if (offset + nextLen > data.length) {
                        nextLen = data.length - (msgCount - 1) * payloadSize;
                    }
                }
            }
            return true;
        }
    }

    protected void sendHeartbeatMessage() throws IOException, InterruptedException, SenderClosedException {
        if (canSend()) {
            final HeartbeatMessage heartbeatMessage;
            synchronized (sendLock) {
                heartbeatMessage = new HeartbeatMessage(sendSequence);
                addSendSequence();
            }
            sendAction.sendPacket(heartbeatMessage.toBytes());
            resender.addMessage(heartbeatMessage);
        } else {
            throw new SenderClosedException();
        }
    }

    protected void sendCloseMessage(final short status, final boolean stopLocalSend, final boolean waitRemote) throws IOException, InterruptedException, SenderClosedException {
        if (canSend()) {
            if (stopLocalSend) stopSend();
            final CloseMessage closeMessage;
            synchronized (sendLock) {
                closeMessage = new CloseMessage(sendSequence, status);
                receiveHandler.setRemoteReceiveCloseSeq(sendSequence);
                addSendSequence();
            }
            sendAction.sendPacket(closeMessage.toBytes());
            resender.addMessage(closeMessage);
            if (waitRemote) receiveHandler.waitRemoteReceiveClose();
        } else {
            throw new SenderClosedException();
        }
    }

    protected long getSendLength() {
        return sendLength.get();
    }
}
