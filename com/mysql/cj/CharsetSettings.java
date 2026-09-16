/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.ServerVersion;

public interface CharsetSettings {
    public static final String CHARACTER_SET_CLIENT = "character_set_client";
    public static final String CHARACTER_SET_CONNECTION = "character_set_connection";
    public static final String CHARACTER_SET_RESULTS = "character_set_results";
    public static final String COLLATION_CONNECTION = "collation_connection";

    public int configurePreHandshake(boolean var1);

    public void configurePostHandshake(boolean var1);

    public boolean doesPlatformDbCharsetMatches();

    public String getPasswordCharacterEncoding();

    public String getErrorMessageEncoding();

    public String getMetadataEncoding();

    public int getMetadataCollationIndex();

    public boolean getRequiresEscapingEncoder();

    public String getJavaEncodingForCollationIndex(int var1);

    public int getMaxBytesPerChar(String var1);

    public int getMaxBytesPerChar(Integer var1, String var2);

    public Integer getCollationIndexForCollationName(String var1);

    public String getCollationNameForCollationIndex(Integer var1);

    public String getMysqlCharsetNameForCollationIndex(Integer var1);

    public int getCollationIndexForJavaEncoding(String var1, ServerVersion var2);

    public int getCollationIndexForMysqlCharsetName(String var1);

    public String getJavaEncodingForMysqlCharset(String var1);

    public String getMysqlCharsetForJavaEncoding(String var1, ServerVersion var2);

    public boolean isMultibyteCharset(String var1);
}

