/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari.pool;

import com.ultimateduels.libs.hikari.pool.ProxyConnection;
import com.ultimateduels.libs.hikari.pool.ProxyPreparedStatement;
import java.sql.CallableStatement;

public abstract class ProxyCallableStatement
extends ProxyPreparedStatement
implements CallableStatement {
    protected ProxyCallableStatement(ProxyConnection connection, CallableStatement statement) {
        super(connection, statement);
    }
}

