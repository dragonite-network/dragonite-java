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

package com.vecsight.dragonite.mux.conn;

import com.vecsight.dragonite.mux.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.mux.exception.SenderClosedException;
import com.vecsight.dragonite.mux.frame.Frame;
import com.vecsight.dragonite.mux.frame.types.CloseConnectionFrame;
import com.vecsight.dragonite.mux.frame.types.ContinueConnectionFrame;
import com.vecsight.dragonite.mux.frame.types.DataFrame;
import com.vecsight.dragonite.mux.frame.types.PauseConnectionFrame;
import com.vecsight.dragonite.mux.misc.MuxGlobalConstants;

import java.util.LinkedList;
import java.util.Queue;

public class MultiplexedConnection {

    private final Multiplexer multiplexer;

    private final short connectionID;

    private final SendAction sendAction;

    private final Queue<Frame> frameQueue = new LinkedList<>();

    private volatile boolean alive = true;

    private volatile boolean stopSend = false;

    private final Object closeLock = new Object();

    private final Object frameLock = new Object();

    private volatile int bufSize = 0;

    private volatile boolean remotePauseSend = false; //Told remote to stop send

    private volatile boolean localPauseSend = false; //Remote told to stop send

    private final Object sendLock = new Object();

    public MultiplexedConnection(final Multiplexer multiplexer, final short connectionID, final SendAction sendAction) {
        this.multiplexer = multiplexer;
        this.connectionID = connectionID;
        this.sendAction = sendAction;
    }

    protected void addFrame(final Frame frame) {
        if (alive) {
            synchronized (frameLock) {
                frameQueue.add(frame);

                if (frame instanceof DataFrame) {
                    bufSize += ((DataFrame) frame).getData().length;
                    if (bufSize > MuxGlobalConstants.CONNECTION_MAX_DATA_BUFFER_SIZE && !remotePauseSend) {
                        remotePauseSend = true;
                        sendAction.sendPacket(new PauseConnectionFrame(connectionID).toBytes());
                    }
                }

                frameLock.notifyAll();
            }
        }
    }

    private byte[] readRaw() throws InterruptedException, ConnectionNotAliveException {
        if (alive) {
            Frame frame;
            synchronized (frameLock) {
                while ((frame = frameQueue.poll()) == null && alive) {
                    frameLock.wait();
                }
            }

            if (frame == null) {
                throw new ConnectionNotAliveException();
            }

            if (frame instanceof DataFrame) {
                final byte[] data = ((DataFrame) frame).getData();

                synchronized (frameLock) {
                    bufSize -= data.length;
                    if (bufSize < MuxGlobalConstants.CONNECTION_MAX_DATA_BUFFER_SIZE && remotePauseSend) {
                        remotePauseSend = false;
                        sendAction.sendPacket(new ContinueConnectionFrame(connectionID).toBytes());
                    }
                }

                return data;
            } else if (frame instanceof CloseConnectionFrame) {
                close();
                return null;
            } else {
                return null;
            }
        } else {
            throw new ConnectionNotAliveException();
        }
    }

    public byte[] read() throws ConnectionNotAliveException, InterruptedException {
        byte[] tmp = null;
        while (tmp == null) {
            tmp = readRaw();
        }
        return tmp;
    }

    private boolean canSend() {
        return alive && !stopSend;
    }

    public void send(final byte[] bytes) throws SenderClosedException, InterruptedException {
        if (canSend()) {
            synchronized (sendLock) {
                while (localPauseSend && canSend()) {
                    sendLock.wait();
                }
            }
            sendAction.sendPacket(new DataFrame(connectionID, bytes).toBytes());
        } else {
            throw new SenderClosedException();
        }
    }

    protected void pauseSend() {
        localPauseSend = true;
    }

    protected void continueSend() {
        localPauseSend = false;
        synchronized (sendLock) {
            sendLock.notifyAll();
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public short getID() {
        return connectionID;
    }

    protected void closeSender() {
        stopSend = true;
    }

    public void close() {
        close_impl(true);
    }

    protected void closeNoRemove() {
        close_impl(false);
    }

    private void close_impl(final boolean removeFromMap) {
        synchronized (closeLock) {
            if (alive) {

                sendAction.sendPacket(new CloseConnectionFrame(connectionID).toBytes());

                alive = false;

                continueSend();

                if (removeFromMap) multiplexer.removeConnectionFromMap(connectionID);

                synchronized (frameLock) {
                    frameQueue.clear();
                    frameLock.notifyAll();
                }

            }
        }
    }
}
