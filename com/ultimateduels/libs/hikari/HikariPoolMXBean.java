/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari;

public interface HikariPoolMXBean {
    public int getIdleConnections();

    public int getActiveConnections();

    public int getTotalConnections();

    public int getThreadsAwaitingConnection();

    public void softEvictConnections();

    public void suspendPool();

    public void resumePool();
}

