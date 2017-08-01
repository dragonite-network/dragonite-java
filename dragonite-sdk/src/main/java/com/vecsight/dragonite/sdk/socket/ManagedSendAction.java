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

import com.github.bucket4j.Bandwidth;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.Bucket4j;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static com.vecsight.dragonite.utils.flow.Preconditions.checkArgument;

public class ManagedSendAction implements SendAction {

    private final SendAction sendAction;

    private volatile Bucket bucket;

    private volatile long speed;

    private final AtomicLong sendRawLength = new AtomicLong(0);

    public ManagedSendAction(final SendAction sendAction, final long speed) {
        this.sendAction = sendAction;
        setSpeed(speed);
    }

    public void setSpeed(final long speed) {
        checkArgument(speed > 0, "Speed must be greater than zero");
        bucket = Bucket4j.builder().addLimit(Bandwidth.simple(speed, Duration.ofSeconds(1))).build();
        this.speed = speed;
    }

    public long getSpeed() {
        return speed;
    }

    public void sendPacket(final byte[] bytes) throws InterruptedException, IOException {
        bucket.consume(bytes.length);
        sendAction.sendPacket(bytes);
        sendRawLength.addAndGet(bytes.length);
    }

    public long getSendRawLength() {
        return sendRawLength.get();
    }
}
