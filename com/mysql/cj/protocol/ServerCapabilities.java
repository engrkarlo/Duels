/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol;

import com.mysql.cj.ServerVersion;

public interface ServerCapabilities {
    public int getCapabilityFlags();

    public void setCapabilityFlags(int var1);

    public ServerVersion getServerVersion();

    public long getThreadId();

    public void setThreadId(long var1);

    public boolean serverSupportsFracSecs();

    public int getServerDefaultCollationIndex();
}

