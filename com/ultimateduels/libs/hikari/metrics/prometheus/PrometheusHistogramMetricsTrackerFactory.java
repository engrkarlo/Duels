/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.prometheus.client.Collector
 *  io.prometheus.client.CollectorRegistry
 */
package com.ultimateduels.libs.hikari.metrics.prometheus;

import com.ultimateduels.libs.hikari.metrics.IMetricsTracker;
import com.ultimateduels.libs.hikari.metrics.MetricsTrackerFactory;
import com.ultimateduels.libs.hikari.metrics.PoolStats;
import com.ultimateduels.libs.hikari.metrics.prometheus.HikariCPCollector;
import com.ultimateduels.libs.hikari.metrics.prometheus.PrometheusHistogramMetricsTracker;
import com.ultimateduels.libs.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrometheusHistogramMetricsTrackerFactory
implements MetricsTrackerFactory {
    private static final Map<CollectorRegistry, PrometheusMetricsTrackerFactory.RegistrationStatus> registrationStatuses = new ConcurrentHashMap<CollectorRegistry, PrometheusMetricsTrackerFactory.RegistrationStatus>();
    private final HikariCPCollector collector = new HikariCPCollector();
    private final CollectorRegistry collectorRegistry;

    public PrometheusHistogramMetricsTrackerFactory() {
        this(CollectorRegistry.defaultRegistry);
    }

    public PrometheusHistogramMetricsTrackerFactory(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @Override
    public IMetricsTracker create(String poolName, PoolStats poolStats) {
        this.registerCollector(this.collector, this.collectorRegistry);
        this.collector.add(poolName, poolStats);
        return new PrometheusHistogramMetricsTracker(poolName, this.collectorRegistry, this.collector);
    }

    private void registerCollector(Collector collector, CollectorRegistry collectorRegistry) {
        if (registrationStatuses.putIfAbsent(collectorRegistry, PrometheusMetricsTrackerFactory.RegistrationStatus.REGISTERED) == null) {
            collector.register(collectorRegistry);
        }
    }
}

