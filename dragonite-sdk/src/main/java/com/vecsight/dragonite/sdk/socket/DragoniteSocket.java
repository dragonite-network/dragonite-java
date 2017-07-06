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

import com.vecsight.dragonite.sdk.exception.ConnectionNotAliveException;
import com.vecsight.dragonite.sdk.exception.IncorrectSizeException;
import com.vecsight.dragonite.sdk.exception.SenderClosedException;

import java.io.IOException;
import java.net.SocketAddress;

public abstract class DragoniteSocket {

    public abstract byte[] read() throws InterruptedException, ConnectionNotAliveException;

    public abstract void send(byte[] bytes) throws InterruptedException, IncorrectSizeException, IOException, SenderClosedException;

    public abstract boolean isAlive();

    protected abstract void updateLastReceiveTime();

    public abstract long getLastReceiveTime();

    public abstract SocketAddress getRemoteSocketAddress();

    public abstract void setSendSpeed(long sendSpeed);

    public abstract long getSendSpeed();

    public abstract DragoniteSocketStatistics getStatistics();

    public abstract String getDescription();

    public abstract void setDescription(String description);

    protected abstract void closeSender();

    public abstract void closeGracefully() throws InterruptedException, IOException, SenderClosedException;

    public abstract void destroy();

}
