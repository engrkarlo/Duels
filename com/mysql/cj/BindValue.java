/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.MysqlType;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.result.Field;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public interface BindValue {
    public BindValue clone();

    public void reset();

    public boolean isNull();

    public void setNull(boolean var1);

    public boolean isStream();

    public MysqlType getMysqlType();

    public void setMysqlType(MysqlType var1);

    public byte[] getByteValue();

    public boolean isSet();

    public void setBinding(Object var1, MysqlType var2, int var3, AtomicBoolean var4);

    public Calendar getCalendar();

    public void setCalendar(Calendar var1);

    public boolean escapeBytesIfNeeded();

    public void setEscapeBytesIfNeeded(boolean var1);

    public Object getValue();

    public boolean isNational();

    public void setIsNational(boolean var1);

    public int getFieldType();

    public long getTextLength();

    public long getBinaryLength();

    public long getBoundBeforeExecutionNum();

    public String getString();

    public Field getField();

    public void setField(Field var1);

    public boolean keepOrigNanos();

    public void setKeepOrigNanos(boolean var1);

    public void setScaleOrLength(long var1);

    public long getScaleOrLength();

    public String getName();

    public void setName(String var1);

    public void writeAsText(Message var1);

    public void writeAsBinary(Message var1);

    public void writeAsQueryAttribute(Message var1);
}

