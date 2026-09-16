/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.a.AbstractValueEncoder;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.StringUtils;
import java.math.BigDecimal;

public class BooleanValueEncoder
extends AbstractValueEncoder {
    @Override
    public String getString(BindValue binding) {
        boolean b = (Boolean)binding.getValue();
        switch (binding.getMysqlType()) {
            case NULL: {
                return "null";
            }
            case CHAR: 
            case VARCHAR: 
            case TINYTEXT: 
            case TEXT: 
            case MEDIUMTEXT: 
            case LONGTEXT: {
                return String.valueOf(b);
            }
            case BIT: 
            case BOOLEAN: 
            case TINYINT: 
            case TINYINT_UNSIGNED: 
            case SMALLINT: 
            case SMALLINT_UNSIGNED: 
            case MEDIUMINT: 
            case MEDIUMINT_UNSIGNED: 
            case INT: 
            case INT_UNSIGNED: 
            case YEAR: {
                return String.valueOf(b ? 1 : 0);
            }
            case BIGINT: 
            case BIGINT_UNSIGNED: {
                return String.valueOf(b ? 1L : 0L);
            }
            case FLOAT: 
            case FLOAT_UNSIGNED: {
                return StringUtils.fixDecimalExponent(Float.toString(b ? 1.0f : 0.0f));
            }
            case DOUBLE: 
            case DOUBLE_UNSIGNED: {
                return StringUtils.fixDecimalExponent(Double.toString(b ? 1.0 : 0.0));
            }
            case DECIMAL: 
            case DECIMAL_UNSIGNED: {
                return new BigDecimal(b ? 1.0 : 0.0).toPlainString();
            }
        }
        throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}), this.exceptionInterceptor);
    }

    @Override
    public void encodeAsBinary(Message msg, BindValue binding) {
        boolean b = (Boolean)binding.getValue();
        NativePacketPayload intoPacket = (NativePacketPayload)msg;
        switch (binding.getMysqlType()) {
            case BIT: 
            case BOOLEAN: 
            case TINYINT: 
            case TINYINT_UNSIGNED: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, b ? 1L : 0L);
                return;
            }
            case CHAR: 
            case VARCHAR: 
            case TINYTEXT: 
            case TEXT: 
            case MEDIUMTEXT: 
            case LONGTEXT: {
                intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(String.valueOf(b), (String)this.charEncoding.getValue()));
                return;
            }
            case SMALLINT: 
            case SMALLINT_UNSIGNED: 
            case MEDIUMINT: 
            case MEDIUMINT_UNSIGNED: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, b ? 1L : 0L);
                return;
            }
            case INT: 
            case INT_UNSIGNED: 
            case YEAR: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (Long)binding.getValue());
                return;
            }
            case BIGINT: 
            case BIGINT_UNSIGNED: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, b ? 1L : 0L);
                return;
            }
            case FLOAT: 
            case FLOAT_UNSIGNED: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, Float.floatToIntBits(b ? 1.0f : 0.0f));
                return;
            }
            case DOUBLE: 
            case DOUBLE_UNSIGNED: {
                intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, Double.doubleToLongBits(b ? 1.0 : 0.0));
                return;
            }
            case DECIMAL: 
            case DECIMAL_UNSIGNED: {
                intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(new BigDecimal(b ? 1.0 : 0.0).toPlainString(), (String)this.charEncoding.getValue()));
                return;
            }
        }
        throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}), this.exceptionInterceptor);
    }

    @Override
    public void encodeAsQueryAttribute(Message msg, BindValue binding) {
        boolean b = (Boolean)binding.getValue();
        NativePacketPayload intoPacket = (NativePacketPayload)msg;
        intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, b ? 1L : 0L);
    }
}

