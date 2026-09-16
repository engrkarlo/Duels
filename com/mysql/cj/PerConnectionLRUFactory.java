/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.CacheAdapter;
import com.mysql.cj.CacheAdapterFactory;
import com.mysql.cj.QueryInfo;
import com.mysql.cj.util.LRUCache;
import java.util.Set;

public class PerConnectionLRUFactory
implements CacheAdapterFactory<String, QueryInfo> {
    @Override
    public CacheAdapter<String, QueryInfo> getInstance(Object syncMutex, String url, int cacheMaxSize, int maxKeySize) {
        return new PerConnectionLRU(syncMutex, cacheMaxSize, maxKeySize);
    }

    class PerConnectionLRU
    implements CacheAdapter<String, QueryInfo> {
        private final int cacheSqlLimit;
        private final LRUCache<String, QueryInfo> cache;
        private final Object syncMutex;

        protected PerConnectionLRU(Object syncMutex, int cacheMaxSize, int maxKeySize) {
            int cacheSize = cacheMaxSize;
            this.cacheSqlLimit = maxKeySize;
            this.cache = new LRUCache(cacheSize);
            this.syncMutex = syncMutex;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public QueryInfo get(String key) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return null;
            }
            Object object = this.syncMutex;
            synchronized (object) {
                return (QueryInfo)this.cache.get(key);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void put(String key, QueryInfo value) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return;
            }
            Object object = this.syncMutex;
            synchronized (object) {
                this.cache.put(key, value);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidate(String key) {
            Object object = this.syncMutex;
            synchronized (object) {
                this.cache.remove(key);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidateAll(Set<String> keys) {
            Object object = this.syncMutex;
            synchronized (object) {
                for (String key : keys) {
                    this.cache.remove(key);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidateAll() {
            Object object = this.syncMutex;
            synchronized (object) {
                this.cache.clear();
            }
        }
    }
}

