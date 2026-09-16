/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

public class QueueStatistics {
    private final String kitName;
    private final AtomicInteger totalQueues;
    private final AtomicInteger totalMatches;
    private final AtomicInteger currentQueueSize;
    private final AtomicLong totalWaitTime;
    private final AtomicInteger waitTimeSamples;
    private volatile int peakQueueSize;
    private volatile long peakTime;

    public QueueStatistics(@Nonnull String kitName) {
        this.kitName = kitName;
        this.totalQueues = new AtomicInteger(0);
        this.totalMatches = new AtomicInteger(0);
        this.currentQueueSize = new AtomicInteger(0);
        this.totalWaitTime = new AtomicLong(0L);
        this.waitTimeSamples = new AtomicInteger(0);
        this.peakQueueSize = 0;
        this.peakTime = 0L;
    }

    public void incrementTotalQueues() {
        this.totalQueues.incrementAndGet();
    }

    public void recordMatch(long waitTimeMs) {
        this.totalMatches.incrementAndGet();
        this.totalWaitTime.addAndGet(waitTimeMs);
        this.waitTimeSamples.incrementAndGet();
    }

    public void updateCurrentQueueSize(int size) {
        this.currentQueueSize.set(size);
        if (size > this.peakQueueSize) {
            this.peakQueueSize = size;
            this.peakTime = System.currentTimeMillis();
        }
    }

    public void updatePeakSize(int currentSize) {
        if (currentSize > this.peakQueueSize) {
            this.peakQueueSize = currentSize;
            this.peakTime = System.currentTimeMillis();
        }
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    public int getTotalQueues() {
        return this.totalQueues.get();
    }

    public int getTotalMatches() {
        return this.totalMatches.get();
    }

    public int getCurrentQueueSize() {
        return this.currentQueueSize.get();
    }

    public long getAverageWaitTime() {
        int samples = this.waitTimeSamples.get();
        if (samples == 0) {
            return 0L;
        }
        return this.totalWaitTime.get() / (long)samples;
    }

    public double getAverageWaitTimeSeconds() {
        return (double)this.getAverageWaitTime() / 1000.0;
    }

    @Nonnull
    public String getFormattedAverageWaitTime() {
        long avgMs = this.getAverageWaitTime();
        long seconds = avgMs / 1000L;
        long minutes = seconds / 60L;
        if (minutes > 0L) {
            return String.format("%dm %ds", minutes, seconds % 60L);
        }
        return String.format("%ds", seconds);
    }

    public int getPeakQueueSize() {
        return this.peakQueueSize;
    }

    public long getPeakTime() {
        return this.peakTime;
    }

    public double getMatchRate() {
        int queues = this.totalQueues.get();
        if (queues == 0) {
            return 0.0;
        }
        return (double)this.totalMatches.get() / (double)queues;
    }

    public void reset() {
        this.totalQueues.set(0);
        this.totalMatches.set(0);
        this.currentQueueSize.set(0);
        this.totalWaitTime.set(0L);
        this.waitTimeSamples.set(0);
        this.peakQueueSize = 0;
        this.peakTime = 0L;
    }

    public String toString() {
        return "QueueStatistics{kit=" + this.kitName + ", queues=" + String.valueOf(this.totalQueues) + ", matches=" + String.valueOf(this.totalMatches) + ", current=" + String.valueOf(this.currentQueueSize) + ", avgWait=" + this.getFormattedAverageWaitTime() + ", peak=" + this.peakQueueSize + "}";
    }
}

