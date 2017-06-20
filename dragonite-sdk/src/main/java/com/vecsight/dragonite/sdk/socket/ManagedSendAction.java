package com.vecsight.dragonite.sdk.socket;

import com.github.bucket4j.Bandwidth;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.Bucket4j;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

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
