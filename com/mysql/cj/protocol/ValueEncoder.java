/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;

public interface ValueEncoder {
    public void init(PropertySet var1, ServerSession var2, ExceptionInterceptor var3);

    public byte[] getBytes(BindValue var1);

    public String getString(BindValue var1);

    public long getTextLength(BindValue var1);

    public long getBinaryLength(BindValue var1);

    public void encodeAsText(Message var1, BindValue var2);

    public void encodeAsBinary(Message var1, BindValue var2);

    public void encodeAsQueryAttribute(Message var1, BindValue var2);
}

