package com.vecsight.dragonite.sdk.socket;

import com.vecsight.dragonite.sdk.msg.types.ACKMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ACKMessageManager {

    private final DragoniteSocket socket;

    private final int MTU;

    private final SendAction action;

    private final Thread sendThread;

    private final int delayMS;

    private final Set<Integer> ackList = new HashSet<>();

    private volatile int receivedSeq = 0;

    private volatile boolean receivedSeqChanged = false;

    private volatile boolean running = true;

    private volatile boolean enableLoopLock = false;

    private final Object ackLoopLock = new Object();

    protected ACKMessageManager(final DragoniteSocket socket, final SendAction action, final int delayMS, final int MTU) {
        this.socket = socket;
        this.MTU = MTU;
        this.action = action;
        this.delayMS = delayMS;
        sendThread = new Thread(() -> {
            try {
                while (running && socket.isAlive()) {
                    int[] ackarray = null;
                    synchronized (ackList) {
                        if (ackList.size() > 0) {
                            ackarray = toIntArray(ackList);
                            ackList.clear();
                        }
                    }
                    if (ackarray != null && ackarray.length > 0) {
                        receivedSeqChanged = false;

                        if (ACKMessage.FIXED_LENGTH + ackarray.length * Integer.BYTES > MTU) {
                            final int payloadIntSize = (MTU - ACKMessage.FIXED_LENGTH) / Integer.BYTES;
                            int msgCount = ackarray.length / payloadIntSize;
                            if (ackarray.length % payloadIntSize != 0) {
                                msgCount += 1;
                            }
                            if (msgCount == 0) msgCount = 1;

                            if (msgCount == 1) {
                                sendACKArray(ackarray);
                            } else {
                                int offset = 0, nextLen = payloadIntSize;
                                for (int i = 0; i < msgCount; i++) {
                                    final int[] acks = new int[nextLen];
                                    System.arraycopy(ackarray, offset, acks, 0, nextLen);
                                    sendACKArray(acks);
                                    offset += nextLen;
                                    if (offset + nextLen > ackarray.length) {
                                        nextLen = ackarray.length - (msgCount - 1) * payloadIntSize;
                                    }
                                }
                            }
                        } else {
                            sendACKArray(ackarray);
                        }

                    } else if (receivedSeqChanged) {
                        receivedSeqChanged = false;
                        sendACKArray(new int[]{});
                    }

                    if (enableLoopLock) {
                        synchronized (ackLoopLock) {
                            ackLoopLock.notifyAll();
                        }
                    }
                    Thread.sleep(this.delayMS);
                }
            } catch (final InterruptedException ignored) {
                //okay
            }
        }, "DS-ACK");
        sendThread.start();
    }

    protected void waitAckLoop() throws InterruptedException {
        enableLoopLock = true;
        synchronized (ackLoopLock) {
            ackLoopLock.wait();
        }
    }

    private int[] toIntArray(final Set<Integer> integerSet) {
        final int[] array = new int[integerSet.size()];
        int i = 0;
        for (final Integer integer : integerSet) {
            array[i++] = integer;
        }
        return array;
    }

    protected void sendACKArray(final int[] ackarray) {
        //System.out.println("SEND ACK " + System.currentTimeMillis());
        final ACKMessage ackMessage = new ACKMessage(ackarray, receivedSeq);
        try {
            action.sendPacket(ackMessage.toBytes());
        } catch (IOException | InterruptedException ignored) {
        }
    }

    protected boolean addACK(final int seq) {
        synchronized (ackList) {
            return ackList.add(seq);
        }
    }

    protected void updateReceivedSeq(final int receivedSeq) {
        this.receivedSeq = receivedSeq;
        this.receivedSeqChanged = true;
    }

    protected void close() {
        running = false;
        sendThread.interrupt();
        ackList.clear();
        synchronized (ackLoopLock) {
            ackLoopLock.notifyAll();
        }
    }

}
