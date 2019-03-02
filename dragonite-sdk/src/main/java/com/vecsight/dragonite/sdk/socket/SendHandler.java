/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
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

public class SendHandler {

    private final DragoniteSocket socket;

    private final PacketSender packetSender;

    private final ReceiveHandler receiveHandler;

    private final ConnectionState state;

    private final ResendHandler resender;

    private final int MTU;

    private volatile int sendSequence = 0;

    private final Object sendLock = new Object();

    private volatile boolean stopSend = false;

    private final AtomicLong sendLength = new AtomicLong(0);

    protected SendHandler(final DragoniteSocket socket, final PacketSender packetSender, final ReceiveHandler receiveHandler,
                          final ConnectionState state, final ResendHandler resender, final int MTU) {
        this.socket = socket;
        this.packetSender = packetSender;
        this.receiveHandler = receiveHandler;
        this.state = state;
        this.resender = resender;
        this.MTU = MTU;
    }

    private void addSendSequence() {
        sendSequence++;
        state.setSendSequence(sendSequence);
    }

    protected void stopSend() {
        stopSend = true;
        synchronized (state.sendWindowLock) {
            state.sendWindowLock.notifyAll();
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
            synchronized (state.sendWindowLock) {
                while (!receiveHandler.checkWindowAvailable() && canSend()) {
                    state.sendWindowLock.wait();
                }
            }
            //have to check again after wait!
            if (!canSend()) throw new SenderClosedException();

            final DataMessage dataMessage;
            synchronized (sendLock) {
                dataMessage = new DataMessage(sendSequence, data);
                addSendSequence();
            }
            packetSender.sendPacket(dataMessage.toBytes());
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
            packetSender.sendPacket(heartbeatMessage.toBytes());
            resender.addMessage(heartbeatMessage);
        } else {
            throw new SenderClosedException();
        }
    }

    protected void sendCloseMessage(final int status, final boolean stopLocalSend, final boolean waitRemote) throws IOException, InterruptedException, SenderClosedException {
        if (canSend()) {
            if (stopLocalSend) stopSend();
            final CloseMessage closeMessage;
            synchronized (sendLock) {
                closeMessage = new CloseMessage(sendSequence, (short) status);
                receiveHandler.setRemoteReceiveCloseSeq(sendSequence);
                addSendSequence();
            }
            packetSender.sendPacket(closeMessage.toBytes());
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
