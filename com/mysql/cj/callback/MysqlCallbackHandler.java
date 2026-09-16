/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.callback;

import com.mysql.cj.callback.MysqlCallback;

@FunctionalInterface
public interface MysqlCallbackHandler {
    public void handle(MysqlCallback var1);
}

