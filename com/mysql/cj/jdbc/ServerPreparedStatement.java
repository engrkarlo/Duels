/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.jdbc;

import com.mysql.cj.BindValue;
import com.mysql.cj.CancelQueryTask;
import com.mysql.cj.Messages;
import com.mysql.cj.NativeSession;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.QueryBindings;
import com.mysql.cj.QueryInfo;
import com.mysql.cj.ServerPreparedQuery;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.MysqlParameterMetadata;
import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.jdbc.result.ResultSetMetaData;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.Message;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerPreparedStatement
extends ClientPreparedStatement {
    private boolean hasOnDuplicateKeyUpdate = false;
    private boolean invalid = false;
    private CJException invalidationException;
    protected boolean isCacheable = false;
    protected boolean isCached = false;

    protected static ServerPreparedStatement getInstance(JdbcConnection conn, String sql, String db, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new ServerPreparedStatement(conn, sql, db, resultSetType, resultSetConcurrency);
    }

    protected ServerPreparedStatement(JdbcConnection conn, String sql, String db, int resultSetType, int resultSetConcurrency) throws SQLException {
        super(conn, db);
        this.checkNullOrEmptyQuery(sql);
        String statementComment = this.session.getProtocol().getQueryComment();
        PreparedQuery prepQuery = (PreparedQuery)this.query;
        prepQuery.setOriginalSql(statementComment == null ? sql : "/* " + statementComment + " */ " + sql);
        prepQuery.setQueryInfo(new QueryInfo(prepQuery.getOriginalSql(), this.session, this.charEncoding));
        this.hasOnDuplicateKeyUpdate = prepQuery.getQueryInfo().containsOnDuplicateKeyUpdate();
        try {
            this.serverPrepare(sql);
        }
        catch (CJException | SQLException sqlEx) {
            this.realClose(false, true);
            throw SQLExceptionsMapping.translateException(sqlEx, this.exceptionInterceptor);
        }
        this.setResultSetType(resultSetType);
        this.setResultSetConcurrency(resultSetConcurrency);
    }

    @Override
    protected void initQuery() {
        this.query = ServerPreparedQuery.getInstance(this.session);
    }

    @Override
    public String toString() {
        try {
            StringBuilder toStringBuf = new StringBuilder();
            toStringBuf.append(this.getClass().getName() + "[");
            toStringBuf.append(((ServerPreparedQuery)this.query).getServerStatementId());
            toStringBuf.append("]: ");
            toStringBuf.append(((PreparedQuery)this.query).asSql());
            return toStringBuf.toString();
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addBatch() throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.query.addBatch(((PreparedQuery)this.query).getQueryBindings().clone());
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    @Override
    protected JdbcConnection checkClosed() {
        if (this.invalid) {
            throw this.invalidationException;
        }
        return super.checkClosed();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearParameters() {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                ((ServerPreparedQuery)this.query).clearParameters(true);
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    protected void setClosed(boolean flag) {
        this.isClosed = flag;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws SQLException {
        try {
            JdbcConnection locallyScopedConn = this.connection;
            if (locallyScopedConn == null) {
                return;
            }
            Object object = locallyScopedConn.getConnectionMutex();
            synchronized (object) {
                if (this.isClosed) {
                    return;
                }
                if (this.isCacheable && this.isPoolable()) {
                    this.clearParameters();
                    this.clearAttributes();
                    this.isClosed = true;
                    this.connection.recachePreparedStatement(this);
                    this.isCached = true;
                    return;
                }
                this.isClosed = false;
                this.realClose(true, true);
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected long[] executeBatchSerially(long batchTimeout) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            long[] lArray;
            JdbcConnection locallyScopedConn = this.connection;
            if (locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", this.exceptionInterceptor);
            }
            this.clearWarnings();
            BindValue[] oldBindValues = ((ServerPreparedQuery)this.query).getQueryBindings().getBindValues();
            try {
                long[] updateCounts = null;
                if (this.query.getBatchedArgs() != null) {
                    int nbrCommands = this.query.getBatchedArgs().size();
                    updateCounts = new long[nbrCommands];
                    if (this.retrieveGeneratedKeys) {
                        this.batchedGeneratedKeys = new ArrayList(nbrCommands);
                    }
                    for (int i = 0; i < nbrCommands; ++i) {
                        updateCounts[i] = -3L;
                    }
                    SQLException sqlEx = null;
                    int commandIndex = 0;
                    BindValue[] previousBindValuesForBatch = null;
                    CancelQueryTask timeoutTask = null;
                    try {
                        timeoutTask = this.startQueryTimer(this, batchTimeout);
                        for (commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                            Object arg = this.query.getBatchedArgs().get(commandIndex);
                            try {
                                if (arg instanceof String) {
                                    updateCounts[commandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                                    this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0);
                                    continue;
                                }
                                ((PreparedQuery)this.query).setQueryBindings((QueryBindings)arg);
                                BindValue[] parameterBindings = ((QueryBindings)arg).getBindValues();
                                if (previousBindValuesForBatch != null) {
                                    for (int j = 0; j < parameterBindings.length; ++j) {
                                        if (parameterBindings[j].getMysqlType() == previousBindValuesForBatch[j].getMysqlType()) continue;
                                        ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
                                        break;
                                    }
                                }
                                try {
                                    updateCounts[commandIndex] = this.executeUpdateInternal(false, true);
                                }
                                finally {
                                    previousBindValuesForBatch = parameterBindings;
                                }
                                this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdate() ? 1 : 0);
                                continue;
                            }
                            catch (SQLException ex) {
                                updateCounts[commandIndex] = -3L;
                                if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                    sqlEx = ex;
                                    continue;
                                }
                                long[] newUpdateCounts = new long[commandIndex];
                                System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                                throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.exceptionInterceptor);
                            }
                        }
                    }
                    finally {
                        this.stopQueryTimer(timeoutTask, false, false);
                        this.resetCancelledState();
                    }
                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                    }
                }
                lArray = updateCounts != null ? updateCounts : new long[]{};
                ((ServerPreparedQuery)this.query).getQueryBindings().setBindValues(oldBindValues);
                ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
            }
            catch (Throwable throwable) {
                ((ServerPreparedQuery)this.query).getQueryBindings().setBindValues(oldBindValues);
                ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
                this.clearBatch();
                throw throwable;
            }
            this.clearBatch();
            return lArray;
        }
    }

    private static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor) {
        String sqlState = sqlEx.getSQLState();
        int vendorErrorCode = sqlEx.getErrorCode();
        SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(sqlEx.getMessage() + messageToAppend, sqlState, vendorErrorCode, interceptor);
        sqlExceptionWithNewMessage.setStackTrace(sqlEx.getStackTrace());
        return sqlExceptionWithNewMessage;
    }

    @Override
    protected <M extends Message> ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, M sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, ColumnDefinition metadata, boolean isBatch) throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                ((PreparedQuery)this.query).getQueryBindings().setNumberOfExecutions(((PreparedQuery)this.query).getQueryBindings().getNumberOfExecutions() + 1);
                try {
                    return this.serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata);
                }
                catch (SQLException sqlEx) {
                    if (this.session.getPropertySet().getBooleanProperty(PropertyKey.enablePacketDebug).getValue().booleanValue()) {
                        this.session.dumpPacketRingBuffer();
                    }
                    if (((Boolean)this.dumpQueriesOnException.getValue()).booleanValue()) {
                        String extractedSql = this.toString();
                        StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                        messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                        messageBuf.append(extractedSql);
                        messageBuf.append("\n\n");
                        sqlEx = ServerPreparedStatement.appendMessageToException(sqlEx, messageBuf.toString(), this.exceptionInterceptor);
                    }
                    throw sqlEx;
                }
                catch (Exception ex) {
                    if (this.session.getPropertySet().getBooleanProperty(PropertyKey.enablePacketDebug).getValue().booleanValue()) {
                        this.session.dumpPacketRingBuffer();
                    }
                    SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", ex, this.exceptionInterceptor);
                    if (((Boolean)this.dumpQueriesOnException.getValue()).booleanValue()) {
                        String extractedSql = this.toString();
                        StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                        messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                        messageBuf.append(extractedSql);
                        messageBuf.append("\n\n");
                        sqlEx = ServerPreparedStatement.appendMessageToException(sqlEx, messageBuf.toString(), this.exceptionInterceptor);
                    }
                    throw sqlEx;
                }
            }
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected BindValue getBinding(int parameterIndex, boolean forLongData) throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                int i = this.getCoreParameterIndex(parameterIndex);
                return ((ServerPreparedQuery)this.query).getQueryBindings().getBinding(i, forLongData);
            }
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public java.sql.ResultSetMetaData getMetaData() throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                ColumnDefinition resultFields = ((ServerPreparedQuery)this.query).getResultFields();
                return resultFields == null || resultFields.getFields() == null ? null : new ResultSetMetaData(this.session, resultFields.getFields(), this.session.getPropertySet().getBooleanProperty(PropertyKey.useOldAliasMetadataBehavior).getValue(), this.session.getPropertySet().getBooleanProperty(PropertyKey.yearIsDateType).getValue(), this.exceptionInterceptor);
            }
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                if (this.parameterMetaData == null) {
                    this.parameterMetaData = new MysqlParameterMetadata(this.session, ((ServerPreparedQuery)this.query).getParameterFields(), ((PreparedQuery)this.query).getParameterCount(), this.exceptionInterceptor);
                }
                return this.parameterMetaData;
            }
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    @Override
    public boolean isNull(int paramIndex) {
        throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        try {
            JdbcConnection locallyScopedConn = this.connection;
            if (locallyScopedConn == null) {
                return;
            }
            Object object = locallyScopedConn.getConnectionMutex();
            synchronized (object) {
                if (this.connection != null) {
                    CJException exceptionDuringClose = null;
                    if (this.isCached) {
                        locallyScopedConn.decachePreparedStatement(this);
                        this.isCached = false;
                    }
                    super.realClose(calledExplicitly, closeOpenResults);
                    ((ServerPreparedQuery)this.query).clearParameters(false);
                    if (calledExplicitly && !locallyScopedConn.isClosed()) {
                        Object object2 = locallyScopedConn.getConnectionMutex();
                        synchronized (object2) {
                            try {
                                ((NativeSession)locallyScopedConn.getSession()).getProtocol().sendCommand(this.commandBuilder.buildComStmtClose(null, ((ServerPreparedQuery)this.query).getServerStatementId()), true, 0);
                            }
                            catch (CJException sqlEx) {
                                exceptionDuringClose = sqlEx;
                            }
                        }
                    }
                    if (exceptionDuringClose != null) {
                        throw exceptionDuringClose;
                    }
                }
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void rePrepare() {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            this.invalidationException = null;
            try {
                this.serverPrepare(((PreparedQuery)this.query).getOriginalSql());
            }
            catch (Exception ex) {
                this.invalidationException = ExceptionFactory.createException(ex.getMessage(), ex);
            }
            if (this.invalidationException != null) {
                this.invalid = true;
                this.query.closeQuery();
                if (this.results != null) {
                    try {
                        this.results.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (this.generatedKeysResults != null) {
                    try {
                        this.generatedKeysResults.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                try {
                    this.closeAllOpenResults();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (this.connection != null && !((Boolean)this.dontTrackOpenResources.getValue()).booleanValue()) {
                    this.connection.unregisterStatement(this);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ResultSetInternalMethods serverExecute(int maxRowsToRetrieve, boolean createStreamingResultSet, ColumnDefinition metadata) throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                this.results = ((ServerPreparedQuery)this.query).serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata, this.resultSetFactory);
                return this.results;
            }
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void serverPrepare(String sql) throws SQLException {
        try {
            Object object = this.checkClosed().getConnectionMutex();
            synchronized (object) {
                block21: {
                    SQLException t = null;
                    try {
                        ServerPreparedQuery q = (ServerPreparedQuery)this.query;
                        q.serverPrepare(sql);
                    }
                    catch (IOException ioEx) {
                        t = SQLError.createCommunicationsException(this.connection, this.session.getProtocol().getPacketSentTimeHolder(), this.session.getProtocol().getPacketReceivedTimeHolder(), ioEx, this.exceptionInterceptor);
                    }
                    catch (CJException sqlEx) {
                        SQLException ex = SQLExceptionsMapping.translateException(sqlEx);
                        if (((Boolean)this.dumpQueriesOnException.getValue()).booleanValue()) {
                            StringBuilder messageBuf = new StringBuilder(((PreparedQuery)this.query).getOriginalSql().length() + 32);
                            messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                            messageBuf.append(((PreparedQuery)this.query).getOriginalSql());
                            ex = ServerPreparedStatement.appendMessageToException(ex, messageBuf.toString(), this.exceptionInterceptor);
                        }
                        t = ex;
                    }
                    finally {
                        block22: {
                            try {
                                this.session.clearInputStream();
                            }
                            catch (Exception e) {
                                if (t != null) break block22;
                                t = SQLError.createCommunicationsException(this.connection, this.session.getProtocol().getPacketSentTimeHolder(), this.session.getProtocol().getPacketReceivedTimeHolder(), e, this.exceptionInterceptor);
                            }
                        }
                        if (t == null) break block21;
                        throw t;
                    }
                }
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    @Override
    protected void checkBounds(int parameterIndex, int parameterIndexOffset) throws SQLException {
        int paramCount = ((PreparedQuery)this.query).getParameterCount();
        if (paramCount == 0) {
            throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("ServerPreparedStatement.8"), this.session.getExceptionInterceptor());
        }
        if (parameterIndex < 0 || parameterIndex > paramCount) {
            throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + paramCount, this.session.getExceptionInterceptor());
        }
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            this.checkClosed();
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        try {
            this.checkClosed();
            this.setString(parameterIndex, x.toString());
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }

    @Override
    public long getServerStatementId() {
        return ((ServerPreparedQuery)this.query).getServerStatementId();
    }

    @Override
    protected boolean containsOnDuplicateKeyUpdate() {
        return this.hasOnDuplicateKeyUpdate;
    }

    @Override
    protected ClientPreparedStatement prepareBatchedInsertSQL(JdbcConnection localConn, int numBatches) throws SQLException {
        Object object = this.checkClosed().getConnectionMutex();
        synchronized (object) {
            try {
                ClientPreparedStatement pstmt = localConn.prepareStatement(((PreparedQuery)this.query).getQueryInfo().getSqlForBatch(numBatches), this.resultSetConcurrency, this.query.getResultType().getIntValue()).unwrap(ClientPreparedStatement.class);
                pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
                this.getQueryAttributesBindings().runThroughAll(a -> pstmt.setAttribute(a.getName(), a.getValue()));
                return pstmt;
            }
            catch (CJException e) {
                SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.27"), "S1000", this.exceptionInterceptor);
                sqlEx.initCause(e);
                throw sqlEx;
            }
        }
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        try {
            super.setPoolable(poolable);
            if (!poolable && this.isCached) {
                this.connection.decachePreparedStatement(this);
                this.isCached = false;
                if (this.isClosed) {
                    this.isClosed = false;
                    this.realClose(true, true);
                }
            }
            return;
        }
        catch (CJException cJException) {
            throw SQLExceptionsMapping.translateException(cJException, this.getExceptionInterceptor());
        }
    }
}

