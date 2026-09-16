/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.CharsetMapping;
import com.mysql.cj.CharsetSettings;
import com.mysql.cj.Messages;
import com.mysql.cj.NativeSession;
import com.mysql.cj.ServerVersion;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerCapabilities;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.ResultsetFactory;
import com.mysql.cj.result.IntegerValueFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.util.StringUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NativeCharsetSettings
extends CharsetMapping
implements CharsetSettings {
    private NativeSession session;
    private ServerSession serverSession;
    public Map<Integer, String> collationIndexToCollationName = null;
    public Map<String, Integer> collationNameToCollationIndex = null;
    public Map<Integer, String> collationIndexToCharsetName = null;
    public Map<String, Integer> charsetNameToMblen = null;
    public Map<String, String> charsetNameToJavaEncoding = null;
    public Map<String, Integer> charsetNameToCollationIndex = null;
    public Map<String, String> javaEncodingUcToCharsetName = null;
    public Set<String> multibyteEncodings = null;
    private Integer sessionCollationIndex = null;
    private String metadataEncoding = null;
    private int metadataCollationIndex;
    private String errorMessageEncoding = "Cp1252";
    protected RuntimeProperty<String> characterEncoding;
    protected RuntimeProperty<String> passwordCharacterEncoding;
    protected RuntimeProperty<String> characterSetResults;
    protected RuntimeProperty<String> connectionCollation;
    protected RuntimeProperty<Boolean> cacheServerConfiguration;
    private boolean requiresEscapingEncoder;
    private NativeMessageBuilder commandBuilder = null;
    private static final Map<String, Map<Integer, String>> customCollationIndexToCollationNameByUrl = new HashMap<String, Map<Integer, String>>();
    private static final Map<String, Map<String, Integer>> customCollationNameToCollationIndexByUrl = new HashMap<String, Map<String, Integer>>();
    private static final Map<String, Map<Integer, String>> customCollationIndexToCharsetNameByUrl = new HashMap<String, Map<Integer, String>>();
    private static final Map<String, Map<String, Integer>> customCharsetNameToMblenByUrl = new HashMap<String, Map<String, Integer>>();
    private static final Map<String, Map<String, String>> customCharsetNameToJavaEncodingByUrl = new HashMap<String, Map<String, String>>();
    private static final Map<String, Map<String, Integer>> customCharsetNameToCollationIndexByUrl = new HashMap<String, Map<String, Integer>>();
    private static final Map<String, Map<String, String>> customJavaEncodingUcToCharsetNameByUrl = new HashMap<String, Map<String, String>>();
    private static final Map<String, Set<String>> customMultibyteEncodingsByUrl = new HashMap<String, Set<String>>();
    private boolean platformDbCharsetMatches = true;

    private NativeMessageBuilder getCommandBuilder() {
        if (this.commandBuilder == null) {
            this.commandBuilder = new NativeMessageBuilder(this.serverSession.supportsQueryAttributes());
        }
        return this.commandBuilder;
    }

    private void checkForCharsetMismatch() {
        String characterEncodingValue = this.characterEncoding.getValue();
        if (characterEncodingValue != null) {
            Charset characterEncodingCs = Charset.forName(characterEncodingValue);
            Charset encodingToCheck = Charset.defaultCharset();
            this.platformDbCharsetMatches = encodingToCheck.equals(characterEncodingCs);
        }
    }

    @Override
    public boolean doesPlatformDbCharsetMatches() {
        return this.platformDbCharsetMatches;
    }

    public NativeCharsetSettings(NativeSession sess) {
        this.session = sess;
        this.serverSession = this.session.getServerSession();
        this.characterEncoding = sess.getPropertySet().getStringProperty(PropertyKey.characterEncoding);
        this.characterSetResults = this.session.getPropertySet().getProperty(PropertyKey.characterSetResults);
        this.passwordCharacterEncoding = this.session.getPropertySet().getStringProperty(PropertyKey.passwordCharacterEncoding);
        this.connectionCollation = this.session.getPropertySet().getStringProperty(PropertyKey.connectionCollation);
        this.cacheServerConfiguration = sess.getPropertySet().getBooleanProperty(PropertyKey.cacheServerConfiguration);
        this.tryAndFixEncoding(this.characterEncoding, true);
        this.tryAndFixEncoding(this.passwordCharacterEncoding, true);
        if (!"null".equalsIgnoreCase(this.characterSetResults.getValue())) {
            this.tryAndFixEncoding(this.characterSetResults, false);
        }
    }

    private void tryAndFixEncoding(RuntimeProperty<String> encodingProperty, boolean replaceImpermissibleEncodings) {
        String oldEncoding = encodingProperty.getValue();
        if (oldEncoding != null) {
            if (replaceImpermissibleEncodings && ("UnicodeBig".equalsIgnoreCase(oldEncoding) || "UTF-16".equalsIgnoreCase(oldEncoding) || "UTF-16LE".equalsIgnoreCase(oldEncoding) || "UTF-32".equalsIgnoreCase(oldEncoding))) {
                encodingProperty.setValue("UTF-8");
            } else {
                try {
                    StringUtils.getBytes("abc", oldEncoding);
                }
                catch (WrongArgumentException waEx) {
                    String newEncoding = NativeCharsetSettings.getStaticJavaEncodingForMysqlCharset(oldEncoding);
                    if (newEncoding == null) {
                        throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{oldEncoding}), this.session.getExceptionInterceptor());
                    }
                    StringUtils.getBytes("abc", newEncoding);
                    encodingProperty.setValue(newEncoding);
                }
            }
        }
    }

    @Override
    public int configurePreHandshake(boolean reset) {
        String connectionColl;
        if (reset) {
            this.sessionCollationIndex = null;
        }
        if (this.sessionCollationIndex != null) {
            return this.sessionCollationIndex;
        }
        ServerCapabilities capabilities = this.serverSession.getCapabilities();
        String encoding = this.passwordCharacterEncoding.getStringValue();
        if (encoding == null && ((connectionColl = this.connectionCollation.getStringValue()) == null || (this.sessionCollationIndex = NativeCharsetSettings.getStaticCollationIndexForCollationName(connectionColl)) == null) && (encoding = this.characterEncoding.getValue()) == null) {
            this.sessionCollationIndex = 255;
        }
        if (this.sessionCollationIndex == null && (this.sessionCollationIndex = Integer.valueOf(NativeCharsetSettings.getStaticCollationIndexForJavaEncoding(encoding, capabilities.getServerVersion()))) == 0) {
            throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}));
        }
        if (this.sessionCollationIndex > 255 || NativeCharsetSettings.isStaticImpermissibleCollation(this.sessionCollationIndex)) {
            this.sessionCollationIndex = 255;
        }
        if (this.sessionCollationIndex == 255 && !capabilities.getServerVersion().meetsMinimum(new ServerVersion(8, 0, 1))) {
            this.sessionCollationIndex = 45;
        }
        this.errorMessageEncoding = NativeCharsetSettings.getStaticJavaEncodingForCollationIndex(this.sessionCollationIndex);
        String csName = NativeCharsetSettings.getStaticMysqlCharsetNameForCollationIndex(this.sessionCollationIndex);
        this.serverSession.getServerVariables().put("character_set_results", csName);
        this.serverSession.getServerVariables().put("character_set_client", csName);
        this.serverSession.getServerVariables().put("character_set_connection", csName);
        this.serverSession.getServerVariables().put("collation_connection", NativeCharsetSettings.getStaticCollationNameForCollationIndex(this.sessionCollationIndex));
        return this.sessionCollationIndex;
    }

    @Override
    public void configurePostHandshake(boolean dontCheckServerMatch) {
        block29: {
            this.buildCollationMapping();
            String requiredCollation = this.connectionCollation.getStringValue();
            String requiredEncoding = this.characterEncoding.getValue();
            String passwordEncoding = this.passwordCharacterEncoding.getValue();
            String sessionCharsetName = this.getServerDefaultCharset();
            String sessionCollationClause = "";
            try {
                Integer requiredCollationIndex;
                if (requiredCollation != null && (requiredCollationIndex = this.getCollationIndexForCollationName(requiredCollation)) != null) {
                    if (this.isImpermissibleCollation(requiredCollationIndex)) {
                        if (this.serverSession.getCapabilities().getServerVersion().meetsMinimum(new ServerVersion(8, 0, 1))) {
                            requiredCollationIndex = 255;
                            requiredCollation = "utf8mb4_0900_ai_ci";
                        } else {
                            requiredCollationIndex = 45;
                            requiredCollation = "utf8mb4_general_ci";
                        }
                    }
                    sessionCollationClause = " COLLATE " + requiredCollation;
                    sessionCharsetName = this.getMysqlCharsetNameForCollationIndex(requiredCollationIndex);
                    requiredEncoding = this.getJavaEncodingForCollationIndex(requiredCollationIndex, requiredEncoding);
                    this.sessionCollationIndex = requiredCollationIndex;
                }
                if (requiredEncoding != null) {
                    if (sessionCollationClause.length() == 0) {
                        sessionCharsetName = this.getMysqlCharsetForJavaEncoding(requiredEncoding.toUpperCase(Locale.ENGLISH), this.serverSession.getServerVersion());
                    }
                } else {
                    if (!StringUtils.isNullOrEmpty(passwordEncoding)) {
                        if (this.serverSession.getCapabilities().getServerVersion().meetsMinimum(new ServerVersion(8, 0, 1))) {
                            this.sessionCollationIndex = 255;
                            requiredCollation = "utf8mb4_0900_ai_ci";
                        } else {
                            this.sessionCollationIndex = 45;
                            requiredCollation = "utf8mb4_general_ci";
                        }
                        sessionCollationClause = " COLLATE " + this.getCollationNameForCollationIndex(this.sessionCollationIndex);
                    }
                    if ((requiredEncoding = this.getJavaEncodingForCollationIndex(this.sessionCollationIndex, requiredEncoding)) == null) {
                        throw ExceptionFactory.createException(Messages.getString("Connection.5", new Object[]{this.sessionCollationIndex.toString()}), this.session.getExceptionInterceptor());
                    }
                    sessionCharsetName = this.getMysqlCharsetNameForCollationIndex(this.sessionCollationIndex);
                }
            }
            catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                throw ExceptionFactory.createException(Messages.getString("Connection.6", new Object[]{this.sessionCollationIndex}), this.session.getExceptionInterceptor());
            }
            this.characterEncoding.setValue(requiredEncoding);
            if (sessionCharsetName != null) {
                boolean isCollationDifferent;
                boolean isCharsetDifferent = !this.characterSetNamesMatches(sessionCharsetName);
                boolean bl = isCollationDifferent = sessionCollationClause.length() > 0 && !requiredCollation.equalsIgnoreCase(this.serverSession.getServerVariable("collation_connection"));
                if (dontCheckServerMatch || isCharsetDifferent || isCollationDifferent) {
                    this.session.getProtocol().sendCommand(this.getCommandBuilder().buildComQuery(null, "SET NAMES " + sessionCharsetName + sessionCollationClause), false, 0);
                    this.serverSession.getServerVariables().put("character_set_client", sessionCharsetName);
                    this.serverSession.getServerVariables().put("character_set_connection", sessionCharsetName);
                    if (sessionCollationClause.length() > 0) {
                        this.serverSession.getServerVariables().put("collation_connection", requiredCollation);
                    } else {
                        int idx = this.getCollationIndexForMysqlCharsetName(sessionCharsetName);
                        if (idx == 255 && !this.serverSession.getCapabilities().getServerVersion().meetsMinimum(new ServerVersion(8, 0, 1))) {
                            idx = 45;
                        }
                        this.serverSession.getServerVariables().put("collation_connection", this.getCollationNameForCollationIndex(idx));
                    }
                }
            }
            String sessionResultsCharset = this.serverSession.getServerVariable("character_set_results");
            String characterSetResultsValue = this.characterSetResults.getValue();
            if (StringUtils.isNullOrEmpty(characterSetResultsValue) || "null".equalsIgnoreCase(characterSetResultsValue)) {
                String defaultMetadataCharsetMysql;
                if (!StringUtils.isNullOrEmpty(sessionResultsCharset) && !"NULL".equalsIgnoreCase(sessionResultsCharset)) {
                    this.session.getProtocol().sendCommand(this.getCommandBuilder().buildComQuery(null, "SET character_set_results = NULL"), false, 0);
                    this.serverSession.getServerVariables().put("character_set_results", null);
                }
                this.metadataEncoding = (defaultMetadataCharsetMysql = this.serverSession.getServerVariable("character_set_system")) != null ? this.getJavaEncodingForMysqlCharset(defaultMetadataCharsetMysql) : "UTF-8";
                this.errorMessageEncoding = "UTF-8";
            } else {
                String resultsCharsetName = this.getMysqlCharsetForJavaEncoding(characterSetResultsValue.toUpperCase(Locale.ENGLISH), this.serverSession.getServerVersion());
                if (resultsCharsetName == null) {
                    throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("Connection.7", new Object[]{characterSetResultsValue}), this.session.getExceptionInterceptor());
                }
                if (!resultsCharsetName.equalsIgnoreCase(sessionResultsCharset)) {
                    this.session.getProtocol().sendCommand(this.getCommandBuilder().buildComQuery(null, "SET character_set_results = " + resultsCharsetName), false, 0);
                    this.serverSession.getServerVariables().put("character_set_results", resultsCharsetName);
                }
                this.metadataEncoding = characterSetResultsValue;
                this.errorMessageEncoding = characterSetResultsValue;
            }
            this.metadataCollationIndex = this.getCollationIndexForJavaEncoding(this.metadataEncoding, this.serverSession.getServerVersion());
            this.checkForCharsetMismatch();
            try {
                CharsetEncoder enc = Charset.forName(this.characterEncoding.getValue()).newEncoder();
                CharBuffer cbuf = CharBuffer.allocate(1);
                ByteBuffer bbuf = ByteBuffer.allocate(1);
                cbuf.put("\u00a5");
                cbuf.position(0);
                enc.encode(cbuf, bbuf, true);
                if (bbuf.get(0) == 92) {
                    this.requiresEscapingEncoder = true;
                } else {
                    cbuf.clear();
                    bbuf.clear();
                    cbuf.put("\u20a9");
                    cbuf.position(0);
                    enc.encode(cbuf, bbuf, true);
                    if (bbuf.get(0) == 92) {
                        this.requiresEscapingEncoder = true;
                    }
                }
            }
            catch (UnsupportedCharsetException ucex) {
                byte[] bbuf = StringUtils.getBytes("\u00a5", this.characterEncoding.getValue());
                if (bbuf[0] == 92) {
                    this.requiresEscapingEncoder = true;
                }
                bbuf = StringUtils.getBytes("\u20a9", this.characterEncoding.getValue());
                if (bbuf[0] != 92) break block29;
                this.requiresEscapingEncoder = true;
            }
        }
    }

    private boolean characterSetNamesMatches(String mysqlEncodingName) {
        boolean res;
        block2: {
            List<String> aliases;
            res = false;
            if (mysqlEncodingName == null) break block2;
            boolean bl = res = mysqlEncodingName.equalsIgnoreCase(this.serverSession.getServerVariable("character_set_client")) && mysqlEncodingName.equalsIgnoreCase(this.serverSession.getServerVariable("character_set_connection"));
            if (!res && (aliases = CharsetMapping.getStaticMysqlCharsetAliasesByName(mysqlEncodingName)) != null) {
                String alias;
                Iterator<String> iterator = aliases.iterator();
                while (iterator.hasNext() && !(res = (alias = iterator.next()).equalsIgnoreCase(this.serverSession.getServerVariable("character_set_client")) && alias.equalsIgnoreCase(this.serverSession.getServerVariable("character_set_connection")))) {
                }
            }
        }
        return res;
    }

    public String getServerDefaultCharset() {
        String charset = NativeCharsetSettings.getStaticMysqlCharsetNameForCollationIndex(this.sessionCollationIndex);
        return charset != null ? charset : this.serverSession.getServerVariable("character_set_server");
    }

    @Override
    public String getErrorMessageEncoding() {
        return this.errorMessageEncoding;
    }

    @Override
    public String getMetadataEncoding() {
        return this.metadataEncoding;
    }

    @Override
    public int getMetadataCollationIndex() {
        return this.metadataCollationIndex;
    }

    @Override
    public boolean getRequiresEscapingEncoder() {
        return this.requiresEscapingEncoder;
    }

    @Override
    public String getPasswordCharacterEncoding() {
        return NativeCharsetSettings.getStaticJavaEncodingForCollationIndex(this.sessionCollationIndex);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void buildCollationMapping() {
        Map<Integer, String> customCollationIndexToCollationName = null;
        Map<String, Integer> customCollationNameToCollationIndex = null;
        Map<Integer, String> customCollationIndexToCharsetName = null;
        Map<String, Integer> customCharsetNameToMblen = null;
        Map<String, String> customCharsetNameToJavaEncoding = new HashMap<String, String>();
        Map<String, String> customJavaEncodingUcToCharsetName = new HashMap<String, String>();
        Map<String, Integer> customCharsetNameToCollationIndex = new HashMap<String, Integer>();
        Set<String> customMultibyteEncodings = new HashSet<String>();
        String databaseURL = this.session.getHostInfo().getDatabaseUrl();
        if (this.cacheServerConfiguration.getValue().booleanValue()) {
            Map<String, Map<Integer, String>> map = customCollationIndexToCharsetNameByUrl;
            synchronized (map) {
                customCollationIndexToCollationName = customCollationIndexToCollationNameByUrl.get(databaseURL);
                customCollationNameToCollationIndex = customCollationNameToCollationIndexByUrl.get(databaseURL);
                customCollationIndexToCharsetName = customCollationIndexToCharsetNameByUrl.get(databaseURL);
                customCharsetNameToMblen = customCharsetNameToMblenByUrl.get(databaseURL);
                customCharsetNameToJavaEncoding = customCharsetNameToJavaEncodingByUrl.get(databaseURL);
                customJavaEncodingUcToCharsetName = customJavaEncodingUcToCharsetNameByUrl.get(databaseURL);
                customCharsetNameToCollationIndex = customCharsetNameToCollationIndexByUrl.get(databaseURL);
                customMultibyteEncodings = customMultibyteEncodingsByUrl.get(databaseURL);
            }
        }
        if (customCollationIndexToCharsetName == null && this.session.getPropertySet().getBooleanProperty(PropertyKey.detectCustomCollations).getValue().booleanValue()) {
            customCollationIndexToCollationName = new HashMap<Integer, String>();
            customCollationNameToCollationIndex = new HashMap<String, Integer>();
            customCollationIndexToCharsetName = new HashMap<Integer, String>();
            customCharsetNameToMblen = new HashMap<String, Integer>();
            customCharsetNameToJavaEncoding = new HashMap();
            customJavaEncodingUcToCharsetName = new HashMap();
            customCharsetNameToCollationIndex = new HashMap();
            customMultibyteEncodings = new HashSet();
            String customCharsetMapping = this.session.getPropertySet().getStringProperty(PropertyKey.customCharsetMapping).getValue();
            if (customCharsetMapping != null) {
                String[] pairs;
                for (String pair : pairs = customCharsetMapping.split(",")) {
                    int keyEnd = pair.indexOf(":");
                    if (keyEnd <= 0 || keyEnd + 1 >= pair.length()) continue;
                    String charset = pair.substring(0, keyEnd);
                    String encoding = pair.substring(keyEnd + 1);
                    customCharsetNameToJavaEncoding.put(charset, encoding);
                    customJavaEncodingUcToCharsetName.put(encoding.toUpperCase(Locale.ENGLISH), charset);
                }
            }
            IntegerValueFactory ivf = new IntegerValueFactory(this.session.getPropertySet());
            try {
                Row r;
                NativePacketPayload nativePacketPayload = this.session.getProtocol().sendCommand(this.getCommandBuilder().buildComQuery(null, "SELECT c.COLLATION_NAME, c.CHARACTER_SET_NAME, c.ID, cs.MAXLEN, c.IS_DEFAULT='Yes' from INFORMATION_SCHEMA.COLLATIONS AS c LEFT JOIN INFORMATION_SCHEMA.CHARACTER_SETS AS cs ON cs.CHARACTER_SET_NAME=c.CHARACTER_SET_NAME"), false, 0);
                Resultset rs = this.session.getProtocol().readAllResults(-1, false, nativePacketPayload, false, null, new ResultsetFactory(Resultset.Type.FORWARD_ONLY, null));
                StringValueFactory svf = new StringValueFactory(this.session.getPropertySet());
                while ((r = (Row)rs.getRows().next()) != null) {
                    String enc;
                    boolean isDefault;
                    String collationName = r.getValue(0, svf);
                    String charsetName = r.getValue(1, svf);
                    int collationIndex = ((Number)r.getValue(2, ivf)).intValue();
                    int maxlen = ((Number)r.getValue(3, ivf)).intValue();
                    boolean bl = isDefault = ((Number)r.getValue(4, ivf)).intValue() > 0;
                    if (collationIndex >= 1024 || collationIndex != NativeCharsetSettings.getStaticCollationIndexForCollationName(collationName) || !charsetName.equals(NativeCharsetSettings.getStaticMysqlCharsetNameForCollationIndex(collationIndex))) {
                        customCollationIndexToCollationName.put(collationIndex, collationName);
                        customCollationNameToCollationIndex.put(collationName, collationIndex);
                        customCollationIndexToCharsetName.put(collationIndex, charsetName);
                        if (isDefault || !customCharsetNameToCollationIndex.containsKey(charsetName) && CharsetMapping.getStaticCollationIndexForMysqlCharsetName(charsetName) == 0) {
                            customCharsetNameToCollationIndex.put(charsetName, collationIndex);
                        }
                    }
                    if (NativeCharsetSettings.getStaticMysqlCharsetByName(charsetName) != null) continue;
                    customCharsetNameToMblen.put(charsetName, maxlen);
                    if (maxlen <= 1 || (enc = customCharsetNameToJavaEncoding.get(charsetName)) == null) continue;
                    customMultibyteEncodings.add(enc.toUpperCase(Locale.ENGLISH));
                }
            }
            catch (IOException iOException) {
                throw ExceptionFactory.createException(iOException.getMessage(), iOException, this.session.getExceptionInterceptor());
            }
            if (this.cacheServerConfiguration.getValue().booleanValue()) {
                Map<String, Map<Integer, String>> map = customCollationIndexToCharsetNameByUrl;
                synchronized (map) {
                    customCollationIndexToCollationNameByUrl.put(databaseURL, Collections.unmodifiableMap(customCollationIndexToCollationName));
                    customCollationNameToCollationIndexByUrl.put(databaseURL, Collections.unmodifiableMap(customCollationNameToCollationIndex));
                    customCollationIndexToCharsetNameByUrl.put(databaseURL, Collections.unmodifiableMap(customCollationIndexToCharsetName));
                    customCharsetNameToMblenByUrl.put(databaseURL, Collections.unmodifiableMap(customCharsetNameToMblen));
                    customCharsetNameToJavaEncodingByUrl.put(databaseURL, Collections.unmodifiableMap(customCharsetNameToJavaEncoding));
                    customJavaEncodingUcToCharsetNameByUrl.put(databaseURL, Collections.unmodifiableMap(customJavaEncodingUcToCharsetName));
                    customCharsetNameToCollationIndexByUrl.put(databaseURL, Collections.unmodifiableMap(customCharsetNameToCollationIndex));
                    customMultibyteEncodingsByUrl.put(databaseURL, Collections.unmodifiableSet(customMultibyteEncodings));
                }
            }
        }
        if (customCollationIndexToCharsetName != null) {
            this.collationIndexToCollationName = customCollationIndexToCollationName;
            this.collationNameToCollationIndex = customCollationNameToCollationIndex;
            this.collationIndexToCharsetName = customCollationIndexToCharsetName;
            this.charsetNameToMblen = customCharsetNameToMblen;
            this.charsetNameToJavaEncoding = customCharsetNameToJavaEncoding;
            this.javaEncodingUcToCharsetName = customJavaEncodingUcToCharsetName;
            this.charsetNameToCollationIndex = customCharsetNameToCollationIndex;
            this.multibyteEncodings = customMultibyteEncodings;
        }
    }

    @Override
    public Integer getCollationIndexForCollationName(String collationName) {
        Integer collationIndex = null;
        if (this.collationNameToCollationIndex == null || (collationIndex = this.collationNameToCollationIndex.get(collationName)) == null) {
            collationIndex = NativeCharsetSettings.getStaticCollationIndexForCollationName(collationName);
        }
        return collationIndex;
    }

    @Override
    public String getCollationNameForCollationIndex(Integer collationIndex) {
        String collationName = null;
        if (collationIndex != null && (this.collationIndexToCollationName == null || (collationName = this.collationIndexToCollationName.get(collationIndex)) == null)) {
            collationName = NativeCharsetSettings.getStaticCollationNameForCollationIndex(collationIndex);
        }
        return collationName;
    }

    @Override
    public String getMysqlCharsetNameForCollationIndex(Integer collationIndex) {
        String charset = null;
        if (this.collationIndexToCharsetName == null || (charset = this.collationIndexToCharsetName.get(collationIndex)) == null) {
            charset = NativeCharsetSettings.getStaticMysqlCharsetNameForCollationIndex(collationIndex);
        }
        return charset;
    }

    @Override
    public String getJavaEncodingForCollationIndex(int collationIndex) {
        return this.getJavaEncodingForCollationIndex(collationIndex, this.characterEncoding.getValue());
    }

    public String getJavaEncodingForCollationIndex(Integer collationIndex, String fallBackJavaEncoding) {
        String encoding = null;
        String charset = null;
        if (collationIndex != -1) {
            if (this.collationIndexToCharsetName != null && (charset = this.collationIndexToCharsetName.get(collationIndex)) != null) {
                encoding = this.getJavaEncodingForMysqlCharset(charset, fallBackJavaEncoding);
            }
            if (encoding == null) {
                encoding = NativeCharsetSettings.getStaticJavaEncodingForCollationIndex(collationIndex, fallBackJavaEncoding);
            }
        }
        return encoding != null ? encoding : fallBackJavaEncoding;
    }

    @Override
    public int getCollationIndexForJavaEncoding(String javaEncoding, ServerVersion version) {
        return this.getCollationIndexForMysqlCharsetName(this.getMysqlCharsetForJavaEncoding(javaEncoding, version));
    }

    @Override
    public int getCollationIndexForMysqlCharsetName(String charsetName) {
        Integer index = null;
        if (this.charsetNameToCollationIndex == null || (index = this.charsetNameToCollationIndex.get(charsetName)) == null) {
            index = NativeCharsetSettings.getStaticCollationIndexForMysqlCharsetName(charsetName);
        }
        return index;
    }

    @Override
    public String getJavaEncodingForMysqlCharset(String mysqlCharsetName) {
        String encoding = null;
        if (this.charsetNameToJavaEncoding == null || (encoding = this.charsetNameToJavaEncoding.get(mysqlCharsetName)) == null) {
            encoding = NativeCharsetSettings.getStaticJavaEncodingForMysqlCharset(mysqlCharsetName);
        }
        return encoding;
    }

    public String getJavaEncodingForMysqlCharset(String mysqlCharsetName, String javaEncoding) {
        String encoding = null;
        if (this.charsetNameToJavaEncoding == null || (encoding = this.charsetNameToJavaEncoding.get(mysqlCharsetName)) == null) {
            encoding = NativeCharsetSettings.getStaticJavaEncodingForMysqlCharset(mysqlCharsetName, javaEncoding);
        }
        return encoding;
    }

    @Override
    public String getMysqlCharsetForJavaEncoding(String javaEncoding, ServerVersion version) {
        String charset = null;
        if (this.javaEncodingUcToCharsetName == null || (charset = this.javaEncodingUcToCharsetName.get(javaEncoding.toUpperCase(Locale.ENGLISH))) == null) {
            charset = NativeCharsetSettings.getStaticMysqlCharsetForJavaEncoding(javaEncoding, version);
        }
        return charset;
    }

    public boolean isImpermissibleCollation(int collationIndex) {
        String charsetName = null;
        if (this.collationIndexToCharsetName != null && (charsetName = this.collationIndexToCharsetName.get(collationIndex)) != null && (charsetName.equals("ucs2") || charsetName.equals("utf16") || charsetName.equals("utf16le") || charsetName.equals("utf32"))) {
            return true;
        }
        return NativeCharsetSettings.isStaticImpermissibleCollation(collationIndex);
    }

    @Override
    public boolean isMultibyteCharset(String javaEncodingName) {
        if (this.multibyteEncodings != null && this.multibyteEncodings.contains(javaEncodingName.toUpperCase(Locale.ENGLISH))) {
            return true;
        }
        return NativeCharsetSettings.isStaticMultibyteCharset(javaEncodingName);
    }

    @Override
    public int getMaxBytesPerChar(String javaCharsetName) {
        return this.getMaxBytesPerChar(null, javaCharsetName);
    }

    @Override
    public int getMaxBytesPerChar(Integer charsetIndex, String javaCharsetName) {
        String charset = null;
        charset = this.getMysqlCharsetNameForCollationIndex(charsetIndex);
        if (charset == null) {
            charset = NativeCharsetSettings.getStaticMysqlCharsetForJavaEncoding(javaCharsetName, this.serverSession.getServerVersion());
        }
        Integer mblen = null;
        if (this.charsetNameToMblen == null || (mblen = this.charsetNameToMblen.get(charset)) == null) {
            mblen = NativeCharsetSettings.getStaticMblen(charset);
        }
        return mblen != null ? mblen : 1;
    }
}

