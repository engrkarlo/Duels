/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.CacheAdapter;
import com.mysql.cj.CacheAdapterFactory;
import com.mysql.cj.CoreSession;
import com.mysql.cj.Messages;
import com.mysql.cj.Query;
import com.mysql.cj.Session;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.ExceptionInterceptorChain;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.NetworkResources;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.SocketFactory;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.protocol.a.NativeServerSession;
import com.mysql.cj.protocol.a.NativeSocketConnection;
import com.mysql.cj.protocol.a.ResultsetFactory;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.LongValueFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class NativeSession
extends CoreSession
implements Serializable {
    private static final long serialVersionUID = 5323638898749073419L;
    private CacheAdapter<String, Map<String, String>> serverConfigCache;
    private long lastQueryFinishedTime = 0L;
    private boolean needsPing = false;
    private NativeMessageBuilder commandBuilder = null;
    private boolean isClosed = true;
    private Throwable forceClosedReason;
    private CopyOnWriteArrayList<WeakReference<Session.SessionEventListener>> listeners = new CopyOnWriteArrayList();
    private transient Timer cancelTimer;
    private static final String SERVER_VERSION_STRING_VAR_NAME = "server_version_string";

    public NativeSession(HostInfo hostInfo, PropertySet propSet) {
        super(hostInfo, propSet);
    }

    public void connect(HostInfo hi, String user, String password, String database, int loginTimeout, TransactionEventHandler transactionManager) throws IOException {
        this.hostInfo = hi;
        this.setSessionMaxRows(-1);
        NativeSocketConnection socketConnection = new NativeSocketConnection();
        socketConnection.connect(this.hostInfo.getHost(), this.hostInfo.getPort(), this.propertySet, this.getExceptionInterceptor(), this.log, loginTimeout);
        if (this.protocol == null) {
            this.protocol = NativeProtocol.getInstance(this, socketConnection, this.propertySet, this.log, transactionManager);
        } else {
            this.protocol.init(this, socketConnection, this.propertySet, transactionManager);
        }
        this.protocol.connect(user, password, database);
        this.isClosed = false;
        this.commandBuilder = new NativeMessageBuilder(this.getServerSession().supportsQueryAttributes());
    }

    public NativeProtocol getProtocol() {
        return (NativeProtocol)this.protocol;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void quit() {
        if (this.protocol != null) {
            try {
                ((NativeProtocol)this.protocol).quit();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        NativeSession nativeSession = this;
        synchronized (nativeSession) {
            if (this.cancelTimer != null) {
                this.cancelTimer.cancel();
                this.cancelTimer = null;
            }
        }
        this.isClosed = true;
        super.quit();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forceClose() {
        if (this.protocol != null) {
            try {
                this.protocol.getSocketConnection().forceClose();
                ((NativeProtocol)this.protocol).releaseResources();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        NativeSession nativeSession = this;
        synchronized (nativeSession) {
            if (this.cancelTimer != null) {
                this.cancelTimer.cancel();
                this.cancelTimer = null;
            }
        }
        this.isClosed = true;
        super.forceClose();
    }

    public void enableMultiQueries() {
        this.protocol.sendCommand(this.commandBuilder.buildComSetOption(((NativeProtocol)this.protocol).getSharedSendPacket(), 0), false, 0);
        ((NativeServerSession)this.getServerSession()).preserveOldTransactionState();
    }

    public void disableMultiQueries() {
        this.protocol.sendCommand(this.commandBuilder.buildComSetOption(((NativeProtocol)this.protocol).getSharedSendPacket(), 1), false, 0);
        ((NativeServerSession)this.getServerSession()).preserveOldTransactionState();
    }

    @Override
    public boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
        return ((NativeServerSession)this.protocol.getServerSession()).isSetNeededForAutoCommitMode(autoCommitFlag, false);
    }

    public int getSessionMaxRows() {
        return this.sessionMaxRows;
    }

    public void setSessionMaxRows(int sessionMaxRows) {
        this.sessionMaxRows = sessionMaxRows;
    }

    public void setQueryInterceptors(List<QueryInterceptor> queryInterceptors) {
        ((NativeProtocol)this.protocol).setQueryInterceptors(queryInterceptors);
    }

    public boolean isServerLocal(Session sess) {
        SocketFactory factory = this.protocol.getSocketConnection().getSocketFactory();
        return factory.isLocallyConnected(sess);
    }

    public void shutdownServer() {
        if (this.versionMeetsMinimum(5, 7, 9)) {
            this.protocol.sendCommand(this.commandBuilder.buildComQuery(this.getSharedSendPacket(), "SHUTDOWN"), false, 0);
        } else {
            this.protocol.sendCommand(this.commandBuilder.buildComShutdown(this.getSharedSendPacket()), false, 0);
        }
    }

    public void setSocketTimeout(int milliseconds) {
        this.getPropertySet().getProperty(PropertyKey.socketTimeout).setValue(milliseconds);
        ((NativeProtocol)this.protocol).setSocketTimeout(milliseconds);
    }

    public int getSocketTimeout() {
        RuntimeProperty sto = this.getPropertySet().getProperty(PropertyKey.socketTimeout);
        return (Integer)sto.getValue();
    }

    public NativePacketPayload getSharedSendPacket() {
        return ((NativeProtocol)this.protocol).getSharedSendPacket();
    }

    public void dumpPacketRingBuffer() {
        ((NativeProtocol)this.protocol).dumpPacketRingBuffer();
    }

    public <T extends Resultset> T invokeQueryInterceptorsPre(Supplier<String> sql, Query interceptedQuery, boolean forceExecute) {
        return ((NativeProtocol)this.protocol).invokeQueryInterceptorsPre(sql, interceptedQuery, forceExecute);
    }

    public <T extends Resultset> T invokeQueryInterceptorsPost(Supplier<String> sql, Query interceptedQuery, T originalResultSet, boolean forceExecute) {
        return ((NativeProtocol)this.protocol).invokeQueryInterceptorsPost(sql, interceptedQuery, originalResultSet, forceExecute);
    }

    public boolean shouldIntercept() {
        return ((NativeProtocol)this.protocol).getQueryInterceptors() != null;
    }

    public long getCurrentTimeNanosOrMillis() {
        return ((NativeProtocol)this.protocol).getCurrentTimeNanosOrMillis();
    }

    public long getSlowQueryThreshold() {
        return ((NativeProtocol)this.protocol).getSlowQueryThreshold();
    }

    public boolean hadWarnings() {
        return ((NativeProtocol)this.protocol).hadWarnings();
    }

    public void clearInputStream() {
        ((NativeProtocol)this.protocol).clearInputStream();
    }

    public NetworkResources getNetworkResources() {
        return this.protocol.getSocketConnection().getNetworkResources();
    }

    @Override
    public boolean isSSLEstablished() {
        return this.protocol.getSocketConnection().isSSLEstablished();
    }

    public int getCommandCount() {
        return ((NativeProtocol)this.protocol).getCommandCount();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        try {
            return this.protocol.getSocketConnection().getMysqlSocket().getRemoteSocketAddress();
        }
        catch (IOException e) {
            throw new CJCommunicationsException(e);
        }
    }

    public InputStream getLocalInfileInputStream() {
        return this.protocol.getLocalInfileInputStream();
    }

    public void setLocalInfileInputStream(InputStream stream) {
        this.protocol.setLocalInfileInputStream(stream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createConfigCacheIfNeeded(Object syncMutex) {
        Object object = syncMutex;
        synchronized (object) {
            if (this.serverConfigCache != null) {
                return;
            }
            String serverConfigCacheFactory = this.propertySet.getStringProperty(PropertyKey.serverConfigCacheFactory).getStringValue();
            CacheAdapterFactory cacheFactory = Util.getInstance(CacheAdapterFactory.class, serverConfigCacheFactory, null, null, this.getExceptionInterceptor());
            this.serverConfigCache = cacheFactory.getInstance(syncMutex, this.hostInfo.getDatabaseUrl(), Integer.MAX_VALUE, Integer.MAX_VALUE);
            ExceptionInterceptor evictOnCommsError = new ExceptionInterceptor(){

                @Override
                public ExceptionInterceptor init(Properties config, Log log1) {
                    return this;
                }

                @Override
                public void destroy() {
                }

                @Override
                public Exception interceptException(Exception sqlEx) {
                    if (sqlEx instanceof SQLException && ((SQLException)sqlEx).getSQLState() != null && ((SQLException)sqlEx).getSQLState().startsWith("08")) {
                        NativeSession.this.serverConfigCache.invalidate(NativeSession.this.hostInfo.getDatabaseUrl());
                    }
                    return null;
                }
            };
            if (this.exceptionInterceptor == null) {
                this.exceptionInterceptor = evictOnCommsError;
            } else {
                ((ExceptionInterceptorChain)this.exceptionInterceptor).addRingZero(evictOnCommsError);
            }
        }
    }

    public void loadServerVariables(Object syncMutex, String version) {
        if (((Boolean)this.cacheServerConfiguration.getValue()).booleanValue()) {
            this.createConfigCacheIfNeeded(syncMutex);
            Map<String, String> cachedVariableMap = this.serverConfigCache.get(this.hostInfo.getDatabaseUrl());
            if (cachedVariableMap != null) {
                String cachedServerVersion = cachedVariableMap.get(SERVER_VERSION_STRING_VAR_NAME);
                if (cachedServerVersion != null && this.getServerSession().getServerVersion() != null && cachedServerVersion.equals(this.getServerSession().getServerVersion().toString())) {
                    Map<String, String> localVariableMap = this.protocol.getServerSession().getServerVariables();
                    HashMap<String, String> newLocalVariableMap = new HashMap<String, String>();
                    newLocalVariableMap.putAll(cachedVariableMap);
                    newLocalVariableMap.putAll(localVariableMap);
                    this.protocol.getServerSession().setServerVariables(newLocalVariableMap);
                    return;
                }
                this.serverConfigCache.invalidate(this.hostInfo.getDatabaseUrl());
            }
        }
        try {
            if (version != null && version.indexOf(42) != -1) {
                StringBuilder buf = new StringBuilder(version.length() + 10);
                for (int i = 0; i < version.length(); ++i) {
                    char c = version.charAt(i);
                    buf.append(c == '*' ? "[star]" : Character.valueOf(c));
                }
                version = buf.toString();
            }
            String versionComment = this.propertySet.getBooleanProperty(PropertyKey.paranoid).getValue() != false || version == null ? "" : "/* " + version + " */";
            this.protocol.getServerSession().setServerVariables(new HashMap<String, String>());
            if (this.versionMeetsMinimum(5, 1, 0)) {
                StringBuilder queryBuf = new StringBuilder(versionComment).append("SELECT");
                queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
                queryBuf.append(", @@character_set_client AS character_set_client");
                queryBuf.append(", @@character_set_connection AS character_set_connection");
                queryBuf.append(", @@character_set_results AS character_set_results");
                queryBuf.append(", @@character_set_server AS character_set_server");
                queryBuf.append(", @@collation_server AS collation_server");
                queryBuf.append(", @@collation_connection AS collation_connection");
                queryBuf.append(", @@init_connect AS init_connect");
                queryBuf.append(", @@interactive_timeout AS interactive_timeout");
                if (!this.versionMeetsMinimum(5, 5, 0)) {
                    queryBuf.append(", @@language AS language");
                }
                queryBuf.append(", @@license AS license");
                queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
                queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
                queryBuf.append(", @@net_write_timeout AS net_write_timeout");
                queryBuf.append(", @@performance_schema AS performance_schema");
                if (!this.versionMeetsMinimum(8, 0, 3)) {
                    queryBuf.append(", @@query_cache_size AS query_cache_size");
                    queryBuf.append(", @@query_cache_type AS query_cache_type");
                }
                queryBuf.append(", @@sql_mode AS sql_mode");
                queryBuf.append(", @@system_time_zone AS system_time_zone");
                queryBuf.append(", @@time_zone AS time_zone");
                if (this.versionMeetsMinimum(8, 0, 3) || this.versionMeetsMinimum(5, 7, 20) && !this.versionMeetsMinimum(8, 0, 0)) {
                    queryBuf.append(", @@transaction_isolation AS transaction_isolation");
                } else {
                    queryBuf.append(", @@tx_isolation AS transaction_isolation");
                }
                queryBuf.append(", @@wait_timeout AS wait_timeout");
                NativePacketPayload resultPacket = (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, queryBuf.toString()), false, 0);
                Resultset rs = ((NativeProtocol)this.protocol).readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
                Field[] f = rs.getColumnDefinition().getFields();
                if (f.length > 0) {
                    StringValueFactory vf = new StringValueFactory(this.propertySet);
                    Row r = (Row)rs.getRows().next();
                    if (r != null) {
                        for (int i = 0; i < f.length; ++i) {
                            String value = r.getValue(i, vf);
                            this.protocol.getServerSession().getServerVariables().put(f[i].getColumnLabel(), value);
                        }
                    }
                }
            } else {
                Row r;
                NativePacketPayload resultPacket = (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, versionComment + "SHOW VARIABLES"), false, 0);
                Resultset rs = ((NativeProtocol)this.protocol).readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
                StringValueFactory vf = new StringValueFactory(this.propertySet);
                while ((r = (Row)rs.getRows().next()) != null) {
                    this.protocol.getServerSession().getServerVariables().put(r.getValue(0, vf), r.getValue(1, vf));
                }
            }
        }
        catch (IOException e) {
            throw ExceptionFactory.createException(e.getMessage(), e);
        }
        if (((Boolean)this.cacheServerConfiguration.getValue()).booleanValue()) {
            this.protocol.getServerSession().getServerVariables().put(SERVER_VERSION_STRING_VAR_NAME, this.getServerSession().getServerVersion().toString());
            HashMap<String, String> localVariableMap = new HashMap<String, String>();
            localVariableMap.putAll(this.protocol.getServerSession().getServerVariables());
            this.serverConfigCache.put(this.hostInfo.getDatabaseUrl(), Collections.unmodifiableMap(localVariableMap));
        }
    }

    public void setSessionVariables() {
        String sessionVariables = this.getPropertySet().getStringProperty(PropertyKey.sessionVariables).getValue();
        if (sessionVariables != null) {
            ArrayList<String> variablesToSet = new ArrayList<String>();
            for (String part : StringUtils.split(sessionVariables, ",", "\"'(", "\"')", "\"'", true)) {
                variablesToSet.addAll(StringUtils.split(part, ";", "\"'(", "\"')", "\"'", true));
            }
            if (!variablesToSet.isEmpty()) {
                StringBuilder query = new StringBuilder("SET ");
                String separator = "";
                for (String variableToSet : variablesToSet) {
                    if (variableToSet.length() <= 0) continue;
                    query.append(separator);
                    if (!variableToSet.startsWith("@")) {
                        query.append("SESSION ");
                    }
                    query.append(variableToSet);
                    separator = ",";
                }
                this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, query.toString()), false, 0);
            }
        }
    }

    @Override
    public String getProcessHost() {
        try {
            long threadId = this.getThreadId();
            String processHost = this.findProcessHost(threadId);
            if (processHost == null) {
                this.log.logWarn(String.format("Connection id %d not found in \"SHOW PROCESSLIST\", assuming 32-bit overflow, using SELECT CONNECTION_ID() instead", threadId));
                NativePacketPayload resultPacket = (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, "SELECT CONNECTION_ID()"), false, 0);
                Resultset rs = ((NativeProtocol)this.protocol).readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
                LongValueFactory lvf = new LongValueFactory(this.getPropertySet());
                Row r = (Row)rs.getRows().next();
                if (r != null) {
                    threadId = r.getValue(0, lvf);
                    processHost = this.findProcessHost(threadId);
                } else {
                    this.log.logError("No rows returned for statement \"SELECT CONNECTION_ID()\", local connection check will most likely be incorrect");
                }
            }
            if (processHost == null) {
                this.log.logWarn(String.format("Cannot find process listing for connection %d in SHOW PROCESSLIST output, unable to determine if locally connected", threadId));
            }
            return processHost;
        }
        catch (IOException e) {
            throw ExceptionFactory.createException(e.getMessage(), e);
        }
    }

    private String findProcessHost(long threadId) {
        try {
            Row r;
            String processHost = null;
            String ps = this.protocol.getServerSession().getServerVariable("performance_schema");
            NativePacketPayload resultPacket = this.versionMeetsMinimum(5, 6, 0) && ps != null && ("1".contentEquals(ps) || "ON".contentEquals(ps)) ? (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, "select PROCESSLIST_ID, PROCESSLIST_USER, PROCESSLIST_HOST from performance_schema.threads where PROCESSLIST_ID=" + threadId), false, 0) : (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, "SHOW PROCESSLIST"), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol).readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            LongValueFactory lvf = new LongValueFactory(this.getPropertySet());
            StringValueFactory svf = new StringValueFactory(this.propertySet);
            while ((r = (Row)rs.getRows().next()) != null) {
                long id = r.getValue(0, lvf);
                if (threadId != id) continue;
                processHost = r.getValue(2, svf);
                break;
            }
            return processHost;
        }
        catch (IOException e) {
            throw ExceptionFactory.createException(e.getMessage(), e);
        }
    }

    public String queryServerVariable(String varName) {
        try {
            String s;
            NativePacketPayload resultPacket = (NativePacketPayload)this.protocol.sendCommand(this.commandBuilder.buildComQuery(null, "SELECT " + varName), false, 0);
            Resultset rs = ((NativeProtocol)this.protocol).readAllResults(-1, false, resultPacket, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
            StringValueFactory svf = new StringValueFactory(this.propertySet);
            Row r = (Row)rs.getRows().next();
            if (r != null && (s = r.getValue(0, svf)) != null) {
                return s;
            }
            return null;
        }
        catch (IOException e) {
            throw ExceptionFactory.createException(e.getMessage(), e);
        }
    }

    public <T extends Resultset> T execSQL(Query callingQuery, String query, int maxRows, NativePacketPayload packet, boolean streamResults, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory, ColumnDefinition cachedMetadata, boolean isBatch) {
        long queryStartTime = (Boolean)this.gatherPerfMetrics.getValue() != false ? System.currentTimeMillis() : 0L;
        int endOfQueryPacketPosition = packet != null ? packet.getPosition() : 0;
        this.lastQueryFinishedTime = 0L;
        if (((Boolean)this.autoReconnect.getValue()).booleanValue() && (this.getServerSession().isAutoCommit() || ((Boolean)this.autoReconnectForPools.getValue()).booleanValue()) && this.needsPing && !isBatch) {
            try {
                this.ping(false, 0);
                this.needsPing = false;
            }
            catch (Exception Ex) {
                this.invokeReconnectListeners();
            }
        }
        try {
            T Ex = packet == null ? ((NativeProtocol)this.protocol).sendQueryString(callingQuery, query, (String)this.characterEncoding.getValue(), maxRows, streamResults, cachedMetadata, resultSetFactory) : ((NativeProtocol)this.protocol).sendQueryPacket(callingQuery, packet, maxRows, streamResults, cachedMetadata, resultSetFactory);
            return Ex;
        }
        catch (CJException sqlE) {
            if (this.getPropertySet().getBooleanProperty(PropertyKey.dumpQueriesOnException).getValue().booleanValue()) {
                String extractedSql = NativePacketPayload.extractSqlFromPacket(query, packet, endOfQueryPacketPosition, this.getPropertySet().getIntegerProperty(PropertyKey.maxQuerySizeToLog).getValue());
                StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                messageBuf.append(extractedSql);
                messageBuf.append("\n\n");
                sqlE.appendMessage(messageBuf.toString());
            }
            if (((Boolean)this.autoReconnect.getValue()).booleanValue()) {
                if (sqlE instanceof CJCommunicationsException) {
                    this.protocol.getSocketConnection().forceClose();
                }
                this.needsPing = true;
            } else if (sqlE instanceof CJCommunicationsException) {
                this.invokeCleanupListeners(sqlE);
            }
            throw sqlE;
        }
        catch (Throwable ex) {
            if (((Boolean)this.autoReconnect.getValue()).booleanValue()) {
                if (ex instanceof IOException) {
                    this.protocol.getSocketConnection().forceClose();
                } else if (ex instanceof IOException) {
                    this.invokeCleanupListeners(ex);
                }
                this.needsPing = true;
            }
            throw ExceptionFactory.createException(ex.getMessage(), ex, this.exceptionInterceptor);
        }
        finally {
            if (((Boolean)this.maintainTimeStats.getValue()).booleanValue()) {
                this.lastQueryFinishedTime = System.currentTimeMillis();
            }
            if (((Boolean)this.gatherPerfMetrics.getValue()).booleanValue()) {
                ((NativeProtocol)this.protocol).getMetricsHolder().registerQueryExecutionTime(System.currentTimeMillis() - queryStartTime);
            }
        }
    }

    public long getIdleFor() {
        return this.lastQueryFinishedTime == 0L ? 0L : System.currentTimeMillis() - this.lastQueryFinishedTime;
    }

    public boolean isNeedsPing() {
        return this.needsPing;
    }

    public void setNeedsPing(boolean needsPing) {
        this.needsPing = needsPing;
    }

    public void ping(boolean checkForClosedConnection, int timeoutMillis) {
        if (checkForClosedConnection) {
            this.checkClosed();
        }
        long pingMillisLifetime = this.getPropertySet().getIntegerProperty(PropertyKey.selfDestructOnPingSecondsLifetime).getValue().intValue();
        int pingMaxOperations = this.getPropertySet().getIntegerProperty(PropertyKey.selfDestructOnPingMaxOperations).getValue();
        if (pingMillisLifetime > 0L && System.currentTimeMillis() - this.connectionCreationTimeMillis > pingMillisLifetime || pingMaxOperations > 0 && pingMaxOperations <= this.getCommandCount()) {
            this.invokeNormalCloseListeners();
            throw ExceptionFactory.createException(Messages.getString("Connection.exceededConnectionLifetime"), "08S01", 0, false, null, this.exceptionInterceptor);
        }
        this.protocol.sendCommand(this.commandBuilder.buildComPing(null), false, timeoutMillis);
    }

    public long getConnectionCreationTimeMillis() {
        return this.connectionCreationTimeMillis;
    }

    public void setConnectionCreationTimeMillis(long connectionCreationTimeMillis) {
        this.connectionCreationTimeMillis = connectionCreationTimeMillis;
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    public void checkClosed() {
        if (this.isClosed) {
            if (this.forceClosedReason != null && this.forceClosedReason.getClass().equals(OperationCancelledException.class)) {
                throw (OperationCancelledException)this.forceClosedReason;
            }
            throw ExceptionFactory.createException(ConnectionIsClosedException.class, Messages.getString("Connection.2"), this.forceClosedReason, this.getExceptionInterceptor());
        }
    }

    public Throwable getForceClosedReason() {
        return this.forceClosedReason;
    }

    public void setForceClosedReason(Throwable forceClosedReason) {
        this.forceClosedReason = forceClosedReason;
    }

    @Override
    public void addListener(Session.SessionEventListener l) {
        this.listeners.addIfAbsent(new WeakReference<Session.SessionEventListener>(l));
    }

    @Override
    public void removeListener(Session.SessionEventListener listener) {
        for (WeakReference<Session.SessionEventListener> wr : this.listeners) {
            Session.SessionEventListener l = (Session.SessionEventListener)wr.get();
            if (l != listener) continue;
            this.listeners.remove(wr);
            break;
        }
    }

    protected void invokeNormalCloseListeners() {
        for (WeakReference<Session.SessionEventListener> wr : this.listeners) {
            Session.SessionEventListener l = (Session.SessionEventListener)wr.get();
            if (l != null) {
                l.handleNormalClose();
                continue;
            }
            this.listeners.remove(wr);
        }
    }

    protected void invokeReconnectListeners() {
        for (WeakReference<Session.SessionEventListener> wr : this.listeners) {
            Session.SessionEventListener l = (Session.SessionEventListener)wr.get();
            if (l != null) {
                l.handleReconnect();
                continue;
            }
            this.listeners.remove(wr);
        }
    }

    public void invokeCleanupListeners(Throwable whyCleanedUp) {
        for (WeakReference<Session.SessionEventListener> wr : this.listeners) {
            Session.SessionEventListener l = (Session.SessionEventListener)wr.get();
            if (l != null) {
                l.handleCleanup(whyCleanedUp);
                continue;
            }
            this.listeners.remove(wr);
        }
    }

    @Override
    public String getIdentifierQuoteString() {
        return this.protocol != null && this.protocol.getServerSession().useAnsiQuotedIdentifiers() ? "\"" : "`";
    }

    public synchronized Timer getCancelTimer() {
        if (this.cancelTimer == null) {
            this.cancelTimer = new Timer("MySQL Statement Cancellation Timer", Boolean.TRUE);
        }
        return this.cancelTimer;
    }

    public void resetSessionState() {
        this.checkClosed();
        NativePacketPayload message = this.commandBuilder.buildComResetConnection(((NativeProtocol)this.protocol).getSharedSendPacket());
        this.protocol.sendCommand(message, false, 0);
    }
}

