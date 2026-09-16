/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.PreparedQuery;
import com.mysql.cj.QueryBindings;
import com.mysql.cj.Session;
import com.mysql.cj.protocol.Message;
import java.util.List;

public interface MessageBuilder<M extends Message> {
    public M buildSqlStatement(String var1);

    public M buildSqlStatement(String var1, List<Object> var2);

    public M buildClose();

    public M buildComQuery(M var1, Session var2, PreparedQuery var3, QueryBindings var4, String var5);
}

