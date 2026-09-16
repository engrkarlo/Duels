/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol;

import com.mysql.cj.CharsetSettings;
import com.mysql.cj.ServerVersion;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.ServerCapabilities;
import com.mysql.cj.protocol.ServerSessionStateController;
import java.util.Map;
import java.util.TimeZone;

public interface ServerSession {
    public static final int TRANSACTION_NOT_STARTED = 0;
    public static final int TRANSACTION_IN_PROGRESS = 1;
    public static final int TRANSACTION_STARTED = 2;
    public static final int TRANSACTION_COMPLETED = 3;

    public ServerCapabilities getCapabilities();

    public void setCapabilities(ServerCapabilities var1);

    public int getStatusFlags();

    public void setStatusFlags(int var1);

    public void setStatusFlags(int var1, boolean var2);

    public int getTransactionState();

    public boolean inTransactionOnServer();

    public boolean cursorExists();

    public boolean isAutocommit();

    public boolean hasMoreResults();

    public boolean isLastRowSent();

    public boolean noGoodIndexUsed();

    public boolean noIndexUsed();

    public boolean queryWasSlow();

    public long getClientParam();

    public void setClientParam(long var1);

    public boolean hasLongColumnInfo();

    public boolean useMultiResults();

    public boolean isSessionStateTrackingEnabled();

    public boolean isEOFDeprecated();

    public boolean supportsQueryAttributes();

    public Map<String, String> getServerVariables();

    public String getServerVariable(String var1);

    public int getServerVariable(String var1, int var2);

    public void setServerVariables(Map<String, String> var1);

    public ServerVersion getServerVersion();

    public boolean isVersion(ServerVersion var1);

    public boolean isLowerCaseTableNames();

    public boolean storesLowerCaseTableNames();

    public boolean isQueryCacheEnabled();

    public boolean isNoBackslashEscapesSet();

    public boolean useAnsiQuotedIdentifiers();

    public boolean isServerTruncatesFracSecs();

    public boolean isAutoCommit();

    public void setAutoCommit(boolean var1);

    public TimeZone getSessionTimeZone();

    public void setSessionTimeZone(TimeZone var1);

    public TimeZone getDefaultTimeZone();

    default public ServerSessionStateController getServerSessionStateController() {
        throw ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
    }

    public CharsetSettings getCharsetSettings();

    public void setCharsetSettings(CharsetSettings var1);
}

