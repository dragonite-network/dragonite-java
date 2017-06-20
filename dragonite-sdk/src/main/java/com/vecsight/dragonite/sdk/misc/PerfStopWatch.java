package com.vecsight.dragonite.sdk.misc;

public class PerfStopWatch {

    private final boolean warn;

    private final long warnDelta;

    private volatile long lastTick;

    public PerfStopWatch(final boolean warn, final long warnDelta) {
        this.warn = warn;
        this.warnDelta = warnDelta;
        start();
    }

    private void start() {
        lastTick = System.currentTimeMillis();
    }

    public long tick(final String msg) {
        final long time = System.currentTimeMillis() - lastTick;
        if (warn && time >= warnDelta) {
            System.out.println("[PerfStopWatch] " + msg + " " + time + "ms");
        }
        start();
        return time;
    }

    public long tick() {
        return tick("DEFAULT");
    }
}
