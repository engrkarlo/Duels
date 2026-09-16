/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.a.InputStreamValueEncoder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import java.sql.Blob;

public class BlobValueEncoder
extends InputStreamValueEncoder {
    @Override
    public byte[] getBytes(BindValue binding) {
        try {
            return this.streamToBytes(((Blob)binding.getValue()).getBinaryStream(), binding.getScaleOrLength(), null);
        }
        catch (Throwable t) {
            throw ExceptionFactory.createException(t.getMessage(), t, this.exceptionInterceptor);
        }
    }

    @Override
    public void encodeAsText(Message msg, BindValue binding) {
        try {
            this.streamToBytes(((Blob)binding.getValue()).getBinaryStream(), binding.getScaleOrLength(), (NativePacketPayload)msg);
        }
        catch (Throwable t) {
            throw ExceptionFactory.createException(t.getMessage(), t, this.exceptionInterceptor);
        }
    }
}

