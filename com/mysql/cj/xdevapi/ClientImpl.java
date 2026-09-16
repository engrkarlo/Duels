/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.conf.BooleanPropertyDefinition;
import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.conf.DefaultPropertySet;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.IntegerPropertyDefinition;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.x.XProtocol;
import com.mysql.cj.protocol.x.XProtocolError;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.DbDoc;
import com.mysql.cj.xdevapi.JsonLiteral;
import com.mysql.cj.xdevapi.JsonNumber;
import com.mysql.cj.xdevapi.JsonParser;
import com.mysql.cj.xdevapi.JsonString;
import com.mysql.cj.xdevapi.JsonValue;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;
import com.mysql.cj.xdevapi.SessionImpl;
import com.mysql.cj.xdevapi.XDevAPIError;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientImpl
implements Client,
Protocol.ProtocolEventListener {
    private final PooledXProtocol poisonProtocolMarker = new PooledXProtocol();
    private boolean isClosed = false;
    private ConnectionUrl connUrl = null;
    private boolean poolingEnabled = true;
    private int maxSize = 25;
    private int maxIdleTime = 0;
    private int queueTimeout = 0;
    private Set<WeakReference<Session>> nonPooledSessions = null;
    private int demotedTimeout = 120000;
    private ConcurrentMap<HostInfo, Long> demotedHosts = null;
    private Set<WeakReference<PooledXProtocol>> activeProtocols = null;
    private BlockingQueue<PooledXProtocol> idleProtocols = null;
    private Semaphore availableProtocols;
    private ReadWriteLock clientShutdownLock;
    private SessionFactory sessionFactory = new SessionFactory();

    public ClientImpl(String url, String clientPropsJson) {
        Properties clientProps = StringUtils.isNullOrEmpty(clientPropsJson) ? new Properties() : this.clientPropsFromJson(clientPropsJson);
        this.init(url, clientProps);
    }

    public ClientImpl(String url, Properties clientProps) {
        this.init(url, clientProps != null ? clientProps : new Properties());
    }

    private Properties clientPropsFromJson(String clientPropsJson) {
        Properties props = new Properties();
        DbDoc clientPropsDoc = JsonParser.parseDoc(clientPropsJson);
        JsonValue pooling = (JsonValue)clientPropsDoc.remove("pooling");
        if (pooling != null) {
            if (!DbDoc.class.isAssignableFrom(pooling.getClass())) {
                throw new XDevAPIError(String.format("Client option 'pooling' does not support value '%s'.", pooling.toFormattedString()));
            }
            DbDoc poolingDoc = (DbDoc)pooling;
            JsonValue jsonVal = (JsonValue)poolingDoc.remove("enabled");
            if (jsonVal != null) {
                if (JsonLiteral.class.isAssignableFrom(jsonVal.getClass())) {
                    JsonLiteral pe = (JsonLiteral)jsonVal;
                    if (pe != JsonLiteral.FALSE && pe != JsonLiteral.TRUE) {
                        throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_ENABLED.getKeyName(), jsonVal.toFormattedString()));
                    }
                    props.setProperty(Client.ClientProperty.POOLING_ENABLED.getKeyName(), pe.value);
                } else {
                    if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                        throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_ENABLED.getKeyName(), ((JsonString)jsonVal).getString()));
                    }
                    throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_ENABLED.getKeyName(), jsonVal.toFormattedString()));
                }
            }
            if ((jsonVal = (JsonValue)poolingDoc.remove("maxSize")) != null) {
                if (JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
                    props.setProperty(Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(), ((JsonNumber)jsonVal).toString());
                } else {
                    if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                        throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(), ((JsonString)jsonVal).getString()));
                    }
                    throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(), jsonVal.toFormattedString()));
                }
            }
            if ((jsonVal = (JsonValue)poolingDoc.remove("maxIdleTime")) != null) {
                if (JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
                    props.setProperty(Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(), ((JsonNumber)jsonVal).toString());
                } else {
                    if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                        throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(), ((JsonString)jsonVal).getString()));
                    }
                    throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(), jsonVal.toFormattedString()));
                }
            }
            if ((jsonVal = (JsonValue)poolingDoc.remove("queueTimeout")) != null) {
                if (JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
                    props.setProperty(Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(), ((JsonNumber)jsonVal).toString());
                } else {
                    if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                        throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(), ((JsonString)jsonVal).getString()));
                    }
                    throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(), jsonVal.toFormattedString()));
                }
            }
            if (poolingDoc.size() > 0) {
                String key = (String)poolingDoc.keySet().stream().findFirst().get();
                throw new XDevAPIError(String.format("Client option 'pooling.%s' is not recognized as valid.", key));
            }
        }
        if (!clientPropsDoc.isEmpty()) {
            String key = (String)clientPropsDoc.keySet().stream().findFirst().get();
            throw new XDevAPIError(String.format("Client option '%s' is not recognized as valid.", key));
        }
        return props;
    }

    private void validateAndInitializeClientProps(Properties clientProps) {
        String propKey = "";
        String propValue = "";
        propKey = Client.ClientProperty.POOLING_ENABLED.getKeyName();
        if (clientProps.containsKey(propKey)) {
            propValue = clientProps.getProperty(propKey);
            try {
                this.poolingEnabled = BooleanPropertyDefinition.booleanFrom(propKey, propValue, null);
            }
            catch (CJException e) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), e);
            }
        }
        if (clientProps.containsKey(propKey = Client.ClientProperty.POOLING_MAX_SIZE.getKeyName())) {
            propValue = clientProps.getProperty(propKey);
            try {
                this.maxSize = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
            }
            catch (WrongArgumentException e) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), e);
            }
            if (this.maxSize <= 0) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
            }
        }
        if (clientProps.containsKey(propKey = Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName())) {
            propValue = clientProps.getProperty(propKey);
            try {
                this.maxIdleTime = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
            }
            catch (WrongArgumentException e) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), e);
            }
            if (this.maxIdleTime < 0) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
            }
        }
        if (clientProps.containsKey(propKey = Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName())) {
            propValue = clientProps.getProperty(propKey);
            try {
                this.queueTimeout = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
            }
            catch (WrongArgumentException e) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), e);
            }
            if (this.queueTimeout < 0) {
                throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
            }
        }
        List clientPropsAsString = Stream.of(Client.ClientProperty.values()).map(Client.ClientProperty::getKeyName).collect(Collectors.toList());
        propKey = clientProps.keySet().stream().filter(k -> !clientPropsAsString.contains(k)).findFirst().orElse(null);
        if (propKey != null) {
            throw new XDevAPIError(String.format("Client option '%s' is not recognized as valid.", propKey));
        }
    }

    private void init(String url, Properties clientProps) {
        this.connUrl = this.sessionFactory.parseUrl(url);
        this.validateAndInitializeClientProps(clientProps);
        if (this.poolingEnabled) {
            this.demotedHosts = new ConcurrentHashMap<HostInfo, Long>();
            this.activeProtocols = new CopyOnWriteArraySet<WeakReference<PooledXProtocol>>();
            this.idleProtocols = new LinkedBlockingQueue<PooledXProtocol>(this.maxSize);
            this.availableProtocols = new Semaphore(this.maxSize, true);
        } else {
            this.nonPooledSessions = new CopyOnWriteArraySet<WeakReference<Session>>();
        }
        this.clientShutdownLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public Session getSession() {
        return this.poolingEnabled ? this.getPooledSession() : this.getNonPooledSession();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Session getNonPooledSession() {
        if (this.isClosed) {
            throw new XDevAPIError("Client is closed.");
        }
        this.clientShutdownLock.readLock().lock();
        try {
            for (WeakReference<Session> ws : this.nonPooledSessions) {
                Session s = (Session)ws.get();
                if (s != null && s.isOpen()) continue;
                this.nonPooledSessions.remove(ws);
            }
            Session sess = this.sessionFactory.getSession(this.connUrl);
            this.nonPooledSessions.add(new WeakReference<Session>(sess));
            Session session = sess;
            return session;
        }
        finally {
            this.clientShutdownLock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Session getPooledSession() {
        PooledXProtocol protocol = null;
        List<HostInfo> hostsList = this.connUrl.getHostsList();
        long startTime = System.currentTimeMillis();
        while (protocol == null) {
            if (this.isClosed) {
                throw new XDevAPIError("Client is closed.");
            }
            if (this.queueTimeout != 0 && System.currentTimeMillis() > startTime + (long)this.queueTimeout) {
                if (this.availableProtocols.tryAcquire()) {
                    protocol = this.newPooledXProtocol(hostsList);
                }
                if (protocol == null) {
                    throw new XDevAPIError("Session can not be obtained within " + this.queueTimeout + " milliseconds.");
                }
            }
            if ((protocol = (PooledXProtocol)this.idleProtocols.poll()) != null) {
                protocol = this.validateAndResetPooledXProtocol(protocol, hostsList);
                continue;
            }
            if (this.availableProtocols.tryAcquire()) {
                protocol = this.newPooledXProtocol(hostsList);
                continue;
            }
            if (this.queueTimeout > 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = (long)this.queueTimeout - elapsedTime;
                try {
                    if (remainingTime <= 0L) continue;
                    protocol = this.idleProtocols.poll(remainingTime, TimeUnit.MILLISECONDS);
                    protocol = this.validateAndResetPooledXProtocol(protocol, hostsList);
                    continue;
                }
                catch (InterruptedException e) {
                    throw new XDevAPIError("Session can not be obtained within " + this.queueTimeout + " milliseconds.", e);
                }
            }
            try {
                protocol = this.idleProtocols.take();
                this.validateAndResetPooledXProtocol(protocol, hostsList);
            }
            catch (InterruptedException e) {
                throw new XDevAPIError("Session can not be obtained.", e);
            }
        }
        this.clientShutdownLock.readLock().lock();
        try {
            if (this.isClosed) {
                throw new XDevAPIError("Client is closed.");
            }
            this.activeProtocols.add(new WeakReference<PooledXProtocol>(protocol));
        }
        finally {
            this.clientShutdownLock.readLock().unlock();
        }
        SessionImpl sess = new SessionImpl(protocol);
        return sess;
    }

    private PooledXProtocol newPooledXProtocol(List<HostInfo> hostsList) {
        PooledXProtocol protocol = null;
        CJCommunicationsException latestException = null;
        ArrayList<HostInfo> hostsToRetryAfterwards = new ArrayList<HostInfo>();
        for (HostInfo hi : hostsList) {
            if (this.demotedHosts.containsKey(hi)) {
                if (System.currentTimeMillis() - (Long)this.demotedHosts.get(hi) > (long)this.demotedTimeout) {
                    this.demotedHosts.remove(hi);
                } else {
                    hostsToRetryAfterwards.add(hi);
                    continue;
                }
            }
            try {
                protocol = this.newPooledXProtocol(hi);
                break;
            }
            catch (CJCommunicationsException e) {
                if (e.getCause() == null) {
                    this.availableProtocols.release();
                    throw e;
                }
                latestException = e;
                this.demotedHosts.put(hi, System.currentTimeMillis());
            }
        }
        if (protocol == null) {
            for (HostInfo hi : hostsToRetryAfterwards) {
                try {
                    protocol = this.newPooledXProtocol(hi);
                    this.demotedHosts.remove(hi);
                    break;
                }
                catch (CJCommunicationsException e) {
                    if (e.getCause() == null) {
                        this.availableProtocols.release();
                        throw e;
                    }
                    latestException = e;
                    this.demotedHosts.put(hi, System.currentTimeMillis());
                }
            }
        }
        if (protocol == null) {
            this.availableProtocols.release();
            if (latestException != null) {
                throw ExceptionFactory.createException(CJCommunicationsException.class, Messages.getString("Session.Create.Failover.0"), latestException);
            }
        }
        return protocol;
    }

    private PooledXProtocol newPooledXProtocol(HostInfo hi) {
        DefaultPropertySet pset = new DefaultPropertySet();
        pset.initializeProperties(hi.exposeAsProperties());
        PooledXProtocol protocol = new PooledXProtocol(hi, pset, this.maxIdleTime);
        protocol.addListener(this);
        protocol.connect(hi.getUser(), hi.getPassword(), hi.getDatabase());
        return protocol;
    }

    private PooledXProtocol validateAndResetPooledXProtocol(PooledXProtocol protocol, List<HostInfo> hostsList) {
        if (protocol == null) {
            return null;
        }
        if (protocol == this.poisonProtocolMarker) {
            this.idleProtocols.add(this.poisonProtocolMarker);
            throw new XDevAPIError("Session can not be obtained. Client instance is closing.");
        }
        if (!protocol.isOpen()) {
            this.availableProtocols.release();
            return null;
        }
        if (!protocol.isHostInfoValid(hostsList)) {
            this.availableProtocols.release();
            this.demotedHosts.remove(protocol.getHostInfo());
            protocol.realClose();
            return null;
        }
        if (protocol.isIdleTimeoutReached()) {
            this.availableProtocols.release();
            protocol.realClose();
            return null;
        }
        try {
            protocol.reset();
        }
        catch (CJCommunicationsException | XProtocolError e) {
            this.availableProtocols.release();
            return null;
        }
        return protocol;
    }

    @Override
    public void close() {
        this.clientShutdownLock.writeLock().lock();
        try {
            if (!this.isClosed) {
                this.isClosed = true;
                if (this.poolingEnabled) {
                    this.availableProtocols.drainPermits();
                    this.idleProtocols.forEach(PooledXProtocol::realClose);
                    this.idleProtocols.clear();
                    this.idleProtocols.add(this.poisonProtocolMarker);
                    this.activeProtocols.stream().map(Reference::get).filter(Objects::nonNull).forEach(PooledXProtocol::realClose);
                    this.activeProtocols.clear();
                } else {
                    this.nonPooledSessions.stream().map(Reference::get).filter(Objects::nonNull).filter(Session::isOpen).forEach(Session::close);
                }
            }
        }
        finally {
            this.clientShutdownLock.writeLock().unlock();
        }
    }

    void idleProtocol(PooledXProtocol protocol) {
        if (!this.isClosed) {
            for (WeakReference<PooledXProtocol> protocolReference : this.activeProtocols) {
                PooledXProtocol referencedProtocol = (PooledXProtocol)protocolReference.get();
                if (referencedProtocol == null) {
                    if (!this.activeProtocols.remove(protocolReference)) continue;
                    this.availableProtocols.release();
                    continue;
                }
                if (referencedProtocol != protocol) continue;
                this.activeProtocols.remove(protocolReference);
                this.idleProtocols.add(referencedProtocol);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleEvent(Protocol.ProtocolEventListener.EventType type, Object info, Throwable reason) {
        switch (type) {
            case SERVER_SHUTDOWN: {
                HostInfo hi = ((PooledXProtocol)info).getHostInfo();
                this.clientShutdownLock.writeLock().lock();
                try {
                    List toCloseAndRemove = this.idleProtocols.stream().filter(p -> p.getHostInfo().equalHostPortPair(hi)).collect(Collectors.toList());
                    ((Stream)toCloseAndRemove.stream().peek(PooledXProtocol::realClose).peek(this.idleProtocols::remove).map(PooledXProtocol::getHostInfo).sequential()).forEach(this.demotedHosts::remove);
                    this.removeActivePooledXProtocol((PooledXProtocol)info);
                    break;
                }
                finally {
                    this.clientShutdownLock.writeLock().unlock();
                }
            }
            case SERVER_CLOSED_SESSION: {
                this.clientShutdownLock.writeLock().lock();
                try {
                    this.removeActivePooledXProtocol((PooledXProtocol)info);
                    break;
                }
                finally {
                    this.clientShutdownLock.writeLock().unlock();
                }
            }
        }
    }

    private void removeActivePooledXProtocol(PooledXProtocol protocol) {
        for (WeakReference<PooledXProtocol> protocolReference : this.activeProtocols) {
            PooledXProtocol referencedProtocol = (PooledXProtocol)protocolReference.get();
            if (referencedProtocol != protocol) continue;
            if (this.activeProtocols.remove(protocolReference)) {
                this.availableProtocols.release();
            }
            protocol.realClose();
            return;
        }
    }

    public class PooledXProtocol
    extends XProtocol {
        private int maxIdleTime;
        private long idleSince;
        private HostInfo hostInfo;

        PooledXProtocol() {
            super(null, null);
            this.maxIdleTime = -1;
            this.idleSince = -1L;
            this.hostInfo = null;
        }

        PooledXProtocol(HostInfo hostInfo, PropertySet propertySet, int maxIdleTime) {
            super(hostInfo, propertySet);
            this.maxIdleTime = -1;
            this.idleSince = -1L;
            this.hostInfo = null;
            this.hostInfo = hostInfo;
            this.maxIdleTime = maxIdleTime;
        }

        @Override
        public void close() {
            this.reset();
            this.idleSince = System.currentTimeMillis();
            ClientImpl.this.idleProtocol(this);
        }

        HostInfo getHostInfo() {
            return this.hostInfo;
        }

        boolean isIdleTimeoutReached() {
            return this.maxIdleTime > 0 && this.idleSince > 0L && System.currentTimeMillis() > this.idleSince + (long)this.maxIdleTime;
        }

        boolean isHostInfoValid(List<HostInfo> hostsList) {
            return hostsList.stream().filter(h -> h.equalHostPortPair(this.hostInfo)).findFirst().isPresent();
        }

        void realClose() {
            try {
                super.close();
            }
            catch (IOException e) {
                throw new CJCommunicationsException(e);
            }
        }
    }
}

