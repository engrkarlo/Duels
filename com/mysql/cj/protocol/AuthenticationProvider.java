/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.Protocol;

public interface AuthenticationProvider<M extends Message> {
    public void init(Protocol<M> var1, PropertySet var2, ExceptionInterceptor var3);

    public void connect(String var1, String var2, String var3);

    public void changeUser(String var1, String var2, String var3);
}

