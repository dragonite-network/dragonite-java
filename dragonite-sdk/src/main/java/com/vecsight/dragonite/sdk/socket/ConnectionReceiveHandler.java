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

public class ConnectionReceiveHandler {

    private final DragoniteSocket socket;

    private final ACKMessageManager ackMessageManager;

    private final ConnectionResendHandler resender;

    private final Map<Integer, ReliableMessage> receiveMap = new HashMap<>();

    private volatile int nextReadSequence = 0;

    private final Object receiveLock = new Object();

    private volatile int remoteAckedMaxSeq = 0;

    private volatile int remoteAckedConsecutiveSeq = 0;

    private final ConnectionSharedData sharedData;

    private final int aggressiveWindowMultiplier, passiveWindowMultiplier;

    private final int MTU;

    private volatile boolean waitForClose = false, closeACKReceived = false;

    private volatile int closeMsgSequence;

    private final Object closeWaitLock = new Object();

    private final RTTController rttController;

    private final AtomicLong readLength = new AtomicLong(0);

    private volatile long receivedRawLength = 0;

    private volatile long receivedPktCount = 0, dupPktCount = 0;

    protected ConnectionReceiveHandler(DragoniteSocket socket, ACKMessageManager ackMessageManager, ConnectionSharedData sharedData,
                                       int aggressiveWindowMultiplier, int passiveWindowMultiplier, ConnectionResendHandler resender,
                                       int MTU) {
        this.socket = socket;
        this.ackMessageManager = ackMessageManager;
        this.sharedData = sharedData;
        this.aggressiveWindowMultiplier = aggressiveWindowMultiplier;
        this.passiveWindowMultiplier = passiveWindowMultiplier;
        this.resender = resender;
        this.MTU = MTU;
        this.rttController = new RTTController(sharedData);
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

                ackMessageManager.updateReceivedSeq(nextReadSequence);

                nextReadSequence++;
                receiveMap.remove(reliableMessage.getSequence());
            }

            if (reliableMessage instanceof DataMessage) {
                return ((DataMessage) reliableMessage).getData();
            } else if (reliableMessage instanceof CloseMessage) {

                ackMessageManager.waitAckLoop();
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
                ReliableMessage reliableMessage = (ReliableMessage) message;

                if (message instanceof CloseMessage) {

                    ackMessageManager.sendACKArray(new int[]{reliableMessage.getSequence()});
                    socket.closeSender();

                }

                ackMessageManager.addACK(reliableMessage.getSequence());

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

                ACKMessage ackMessage = (ACKMessage) message;
                if (ackMessage.getReceiveSeq() > remoteAckedConsecutiveSeq) {
                    remoteAckedConsecutiveSeq = ackMessage.getReceiveSeq();
                }

                int[] seqs = ackMessage.getSequenceList();

                for (int seq : seqs) {
                    if (seq > remoteAckedMaxSeq) {
                        remoteAckedMaxSeq = seq;
                    }

                    rttController.pushInfo(resender.removeMessage(seq));

                    if (waitForClose && seq == closeMsgSequence) {
                        closeACKReceived = true;
                    }

                }

                synchronized (sharedData.sendWindowLock) {
                    if (checkWindowAvailable()) {
                        sharedData.sendWindowLock.notifyAll();
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

    protected void setRemoteReceiveCloseSeq(int sequence) {
        closeMsgSequence = sequence;
        waitForClose = true;
    }

    protected void waitRemoteReceiveClose() throws InterruptedException {
        synchronized (closeWaitLock) {
            if (!closeACKReceived) {
                closeWaitLock.wait(NumUtils.max(sharedData.getEstimatedRTT() * DragoniteGlobalConstants.CLOSE_WAIT_RTT_MULT, DragoniteGlobalConstants.MIN_CLOSE_WAIT_MS));
            }
        }
    }

    private int getProperWindow(boolean passive) {
        int mult = passive ? passiveWindowMultiplier : aggressiveWindowMultiplier;

        float targetPPS = socket.getSendSpeed() / (float) MTU;
        long currentRTT = sharedData.getEstimatedRTT();
        int wnd = (int) (targetPPS * (currentRTT / 1000f) * mult);

        return NumUtils.max(wnd, DragoniteGlobalConstants.MIN_SEND_WINDOW);
    }

    protected boolean checkWindowAvailable() {
        int aggressiveDelta = sharedData.getSendSequence() - remoteAckedMaxSeq;
        int passiveDelta = sharedData.getSendSequence() - remoteAckedConsecutiveSeq;
        boolean agressiveOK = aggressiveDelta < getProperWindow(false),
                passiveOK = passiveDelta < getProperWindow(true);
        //if (!agressiveOK) System.out.println("NO AGG!");
        //if (!passiveOK) System.out.println("NO PSV!");
        return agressiveOK && passiveOK;
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
