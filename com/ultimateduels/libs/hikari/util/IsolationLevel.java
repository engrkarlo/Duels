/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari.util;

public enum IsolationLevel {
    TRANSACTION_NONE(0),
    TRANSACTION_READ_UNCOMMITTED(1),
    TRANSACTION_READ_COMMITTED(2),
    TRANSACTION_CURSOR_STABILITY(3),
    TRANSACTION_REPEATABLE_READ(4),
    TRANSACTION_LAST_COMMITTED(5),
    TRANSACTION_SERIALIZABLE(8),
    TRANSACTION_SQL_SERVER_SNAPSHOT_ISOLATION_LEVEL(4096);

    private final int levelId;

    private IsolationLevel(int levelId) {
        this.levelId = levelId;
    }

    public int getLevelId() {
        return this.levelId;
    }
}

