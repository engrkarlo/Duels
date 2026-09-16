/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.Query;
import com.mysql.cj.QueryBindings;
import com.mysql.cj.QueryInfo;
import com.mysql.cj.protocol.Message;

public interface PreparedQuery
extends Query {
    public QueryInfo getQueryInfo();

    public void setQueryInfo(QueryInfo var1);

    public void checkNullOrEmptyQuery(String var1);

    public String getOriginalSql();

    public void setOriginalSql(String var1);

    public int getParameterCount();

    public void setParameterCount(int var1);

    public QueryBindings getQueryBindings();

    public void setQueryBindings(QueryBindings var1);

    public int computeBatchSize(int var1);

    public int getBatchCommandIndex();

    public void setBatchCommandIndex(int var1);

    public String asSql();

    public <M extends Message> M fillSendPacket(QueryBindings var1);
}

