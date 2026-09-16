/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.a.AbstractValueEncoder;
import com.mysql.cj.util.StringUtils;

public class NullValueEncoder
extends AbstractValueEncoder {
    @Override
    public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
        super.init(pset, serverSess, excInterceptor);
    }

    @Override
    public byte[] getBytes(BindValue binding) {
        return StringUtils.getBytes("null");
    }

    @Override
    public String getString(BindValue binding) {
        return "NULL";
    }

    @Override
    public void encodeAsBinary(Message msg, BindValue binding) {
    }
}

