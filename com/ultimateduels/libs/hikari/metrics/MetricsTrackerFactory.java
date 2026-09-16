/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari.metrics;

import com.ultimateduels.libs.hikari.metrics.IMetricsTracker;
import com.ultimateduels.libs.hikari.metrics.PoolStats;

public interface MetricsTrackerFactory {
    public IMetricsTracker create(String var1, PoolStats var2);
}

