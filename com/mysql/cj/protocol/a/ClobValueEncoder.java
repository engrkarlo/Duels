/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.a.ReaderValueEncoder;
import java.sql.Clob;

public class ClobValueEncoder
extends ReaderValueEncoder {
    @Override
    public byte[] getBytes(BindValue binding) {
        try {
            return this.readBytes(((Clob)binding.getValue()).getCharacterStream(), binding);
        }
        catch (Throwable t) {
            throw ExceptionFactory.createException(t.getMessage(), t, this.exceptionInterceptor);
        }
    }
}

