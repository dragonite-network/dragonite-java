/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.misc.NumUtils;
import com.vecsight.dragonite.sdk.msg.Message;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;
import com.vecsight.dragonite.sdk.msg.types.ACKMessage;
import com.vecsight.dragonite.sdk.msg.types.CloseMessage;
import com.vecsight.dragonite.sdk.msg.types.DataMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ReceiveHandler {

    private final DragoniteSocket socket;

    private final ACKSender ackSender;

    private final ResendHandler resender;

    private final Map<Integer, ReliableMessage> receiveMap = new HashMap<>();

    private volatile int nextReadSequence = 0;

    private final Object receiveLock = new Object();

    private volatile int remoteConsumedSeq = 0;

    private final ConnectionState state;

    private final int windowMultiplier;

    private final int MTU;

    private volatile boolean waitForClose = false, closeACKReceived = false;

    private volatile int closeMsgSequence;

    private final Object closeWaitLock = new Object();

    private final RTTController rttController;

    private final AtomicLong readLength = new AtomicLong(0);

    private volatile long receivedRawLength = 0;

    private volatile long receivedPktCount = 0, dupPktCount = 0;

    protected ReceiveHandler(final DragoniteSocket socket, final ACKSender ackSender, final ConnectionState state,
                             final int windowMultiplier, final ResendHandler resender, final int MTU) {
        this.socket = socket;
        this.ackSender = ackSender;
        this.state = state;
        this.windowMultiplier = windowMultiplier;
        this.resender = resender;
        this.MTU = MTU;
        this.rttController = new RTTController(state);
    }

    private byte[] readRaw() throws InterruptedException, ConnectionNotAliveException {
        if (socket.isAlive()) {
            ReliableMessage reliableMessage;

            synchronized (receiveLock) {
                while ((reliableMessage = receiveMap.get(nextReadSequence)) == null && socket.isAlive()) {
                    receiveLock.wait();
                }


                if (reliableMessage == null) {
                    throw new ConnectionNotAliveException();
                }

                ackSender.updateReceivedSeq(nextReadSequence);

                nextReadSequence++;
                receiveMap.remove(reliableMessage.getSequence());
            }

            if (reliableMessage instanceof DataMessage) {
                return ((DataMessage) reliableMessage).getData();
            } else if (reliableMessage instanceof CloseMessage) {

                ackSender.waitAckLoop();
                socket.destroy();
                return null;

            } else {
                return null;
            }
        } else {
            throw new ConnectionNotAliveException();
        }
    }

    protected byte[] read() throws InterruptedException, ConnectionNotAliveException {
        byte[] tmp = null;
        while (tmp == null) {
            tmp = readRaw();
        }
        readLength.addAndGet(tmp.length);
        return tmp;
    }

    //single threaded
    protected void onHandleMessage(final Message message, final int pktLength) {
        if (socket.isAlive()) {

            socket.updateLastReceiveTime();

            receivedRawLength += pktLength;

            if (message instanceof ReliableMessage) {
                final ReliableMessage reliableMessage = (ReliableMessage) message;

                if (message instanceof CloseMessage) {

                    ackSender.sendACKArray(new int[]{reliableMessage.getSequence()});
                    socket.closeSender();

                }

                ackSender.addACK(reliableMessage.getSequence());

                synchronized (receiveLock) {

                    receivedPktCount++;

                    if (reliableMessage.getSequence() >= nextReadSequence) {
                        receiveMap.put(reliableMessage.getSequence(), reliableMessage);

                        if (receiveMap.containsKey(nextReadSequence)) {
                            receiveLock.notify();
                        }
                    } else {
                        dupPktCount++;
                    }
                }

            }

            if (message instanceof ACKMessage) {

                final ACKMessage ackMessage = (ACKMessage) message;
                if (ackMessage.getConsumedSeq() > remoteConsumedSeq) {
                    remoteConsumedSeq = ackMessage.getConsumedSeq();
                }

                final int[] seqs = ackMessage.getSequenceList();

                for (final int seq : seqs) {

                    rttController.pushInfo(resender.removeMessage(seq));

                    if (waitForClose && seq == closeMsgSequence) {
                        closeACKReceived = true;
                    }

                }

                synchronized (state.sendWindowLock) {
                    if (checkWindowAvailable()) {
                        state.sendWindowLock.notifyAll();
                    }
                }

                if (closeACKReceived && resender.queueTaskCount() == 0) {
                    synchronized (closeWaitLock) {
                        closeWaitLock.notifyAll();
                    }
                }

            }

        }
    }

    protected void setRemoteReceiveCloseSeq(final int sequence) {
        closeMsgSequence = sequence;
        waitForClose = true;
    }

    protected void waitRemoteReceiveClose() throws InterruptedException {
        synchronized (closeWaitLock) {
            if (!closeACKReceived) {
                closeWaitLock.wait(NumUtils.max(state.getEstimatedRTT() * DragoniteGlobalConstants.CLOSE_WAIT_RTT_MULT, DragoniteGlobalConstants.MIN_CLOSE_WAIT_MS));
            }
        }
    }

    private int getProperWindow() {
        final float targetPPS = socket.getSendSpeed() / (float) MTU;
        final long currentRTT = state.getEstimatedRTT();
        final int wnd = (int) (targetPPS * (currentRTT / 1000f) * windowMultiplier);

        return NumUtils.max(wnd, DragoniteGlobalConstants.MIN_SEND_WINDOW_SIZE);
    }

    protected boolean checkWindowAvailable() {
        final int delta = state.getSendSequence() - remoteConsumedSeq;
        return delta < getProperWindow();
    }

    protected long getReceivedPktCount() {
        return receivedPktCount;
    }

    protected long getDupPktCount() {
        return dupPktCount;
    }

    protected void close() {
        synchronized (receiveLock) {
            receiveMap.clear();
            receiveLock.notifyAll();
        }
        synchronized (closeWaitLock) {
            closeWaitLock.notifyAll();
        }
    }

    protected long getReadLength() {
        return readLength.get();
    }

    protected long getReceivedRawLength() {
        return receivedRawLength;
    }
}
