/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.libs.hikari.pool;

import com.ultimateduels.libs.hikari.pool.HikariProxyCallableStatement;
import com.ultimateduels.libs.hikari.pool.HikariProxyConnection;
import com.ultimateduels.libs.hikari.pool.HikariProxyDatabaseMetaData;
import com.ultimateduels.libs.hikari.pool.HikariProxyPreparedStatement;
import com.ultimateduels.libs.hikari.pool.HikariProxyResultSet;
import com.ultimateduels.libs.hikari.pool.HikariProxyStatement;
import com.ultimateduels.libs.hikari.pool.PoolEntry;
import com.ultimateduels.libs.hikari.pool.ProxyConnection;
import com.ultimateduels.libs.hikari.pool.ProxyLeakTask;
import com.ultimateduels.libs.hikari.pool.ProxyStatement;
import com.ultimateduels.libs.hikari.util.FastList;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class ProxyFactory {
    private ProxyFactory() {
    }

    static ProxyConnection getProxyConnection(PoolEntry poolEntry, Connection connection, FastList<Statement> fastList, ProxyLeakTask proxyLeakTask, boolean bl, boolean bl2) {
        return new HikariProxyConnection(poolEntry, connection, (FastList)fastList, proxyLeakTask, bl, bl2);
    }

    static Statement getProxyStatement(ProxyConnection proxyConnection, Statement statement) {
        return new HikariProxyStatement(proxyConnection, statement);
    }

    static CallableStatement getProxyCallableStatement(ProxyConnection proxyConnection, CallableStatement callableStatement) {
        return new HikariProxyCallableStatement(proxyConnection, callableStatement);
    }

    static PreparedStatement getProxyPreparedStatement(ProxyConnection proxyConnection, PreparedStatement preparedStatement) {
        return new HikariProxyPreparedStatement(proxyConnection, preparedStatement);
    }

    static ResultSet getProxyResultSet(ProxyConnection proxyConnection, ProxyStatement proxyStatement, ResultSet resultSet) {
        return new HikariProxyResultSet(proxyConnection, proxyStatement, resultSet);
    }

    static DatabaseMetaData getProxyDatabaseMetaData(ProxyConnection proxyConnection, DatabaseMetaData databaseMetaData) {
        return new HikariProxyDatabaseMetaData(proxyConnection, databaseMetaData);
    }
}

