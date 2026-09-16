/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari.util;

import com.ultimateduels.libs.hikari.util.IsolationLevel;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class UtilityElf {
    private UtilityElf() {
    }

    public static String getNullIfEmpty(String text) {
        return text == null ? null : (text.trim().isEmpty() ? null : text.trim());
    }

    public static void quietlySleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean safeIsAssignableFrom(Object obj, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.isAssignableFrom(obj.getClass());
        }
        catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static <T> T createInstance(String className, Class<T> clazz, Object ... args) {
        if (className == null) {
            return null;
        }
        try {
            Class<?> loaded = UtilityElf.class.getClassLoader().loadClass(className);
            if (args.length == 0) {
                return clazz.cast(loaded.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            }
            Class[] argClasses = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                argClasses[i] = args[i].getClass();
            }
            Constructor<?> constructor = loaded.getConstructor(argClasses);
            return clazz.cast(constructor.newInstance(args));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ThreadPoolExecutor createThreadPoolExecutor(int queueSize, String threadName, ThreadFactory threadFactory, RejectedExecutionHandler policy) {
        return UtilityElf.createThreadPoolExecutor(new LinkedBlockingQueue<Runnable>(queueSize), threadName, threadFactory, policy);
    }

    public static ThreadPoolExecutor createThreadPoolExecutor(BlockingQueue<Runnable> queue, String threadName, ThreadFactory threadFactory, RejectedExecutionHandler policy) {
        if (threadFactory == null) {
            threadFactory = new DefaultThreadFactory(threadName);
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 5L, TimeUnit.SECONDS, queue, threadFactory, policy);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static int getTransactionIsolation(String transactionIsolationName) {
        if (transactionIsolationName != null) {
            try {
                String upperCaseIsolationLevelName = transactionIsolationName.toUpperCase(Locale.ENGLISH);
                return IsolationLevel.valueOf(upperCaseIsolationLevelName).getLevelId();
            }
            catch (IllegalArgumentException e) {
                try {
                    int level = Integer.parseInt(transactionIsolationName);
                    for (IsolationLevel iso : IsolationLevel.values()) {
                        if (iso.getLevelId() != level) continue;
                        return iso.getLevelId();
                    }
                    throw new IllegalArgumentException("Invalid transaction isolation value: " + transactionIsolationName);
                }
                catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Invalid transaction isolation value: " + transactionIsolationName, nfe);
                }
            }
        }
        return -1;
    }

    public static final class DefaultThreadFactory
    implements ThreadFactory {
        private final String threadName;
        private final boolean daemon;

        public DefaultThreadFactory(String threadName) {
            this.threadName = threadName;
            this.daemon = true;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, this.threadName);
            thread.setDaemon(this.daemon);
            return thread;
        }
    }

    public static class CustomDiscardPolicy
    implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        }
    }
}

