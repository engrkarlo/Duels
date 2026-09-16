/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj;

import com.mysql.cj.Messages;
import com.mysql.cj.QueryReturnType;
import com.mysql.cj.Session;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.util.SearchMode;
import com.mysql.cj.util.StringInspector;
import com.mysql.cj.util.StringUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class QueryInfo {
    private static final String OPENING_MARKERS = "`'\"";
    private static final String CLOSING_MARKERS = "`'\"";
    private static final String OVERRIDING_MARKERS = "";
    private static final String INSERT_STATEMENT = "INSERT";
    private static final String REPLACE_STATEMENT = "REPLACE";
    private static final String VALUE_CLAUSE = "VALUE";
    private static final String AS_CLAUSE = "AS";
    private static final String[] ODKU_CLAUSE = new String[]{"ON", "DUPLICATE", "KEY", "UPDATE"};
    private static final String LAST_INSERT_ID_FUNC = "LAST_INSERT_ID";
    private QueryInfo baseQueryInfo = null;
    private String sql;
    private String encoding;
    private QueryReturnType queryReturnType = null;
    private int queryLength = 0;
    private int queryStartPos = 0;
    private char statementFirstChar = '\u0000';
    private int batchCount = 1;
    private int numberOfPlaceholders = 0;
    private int numberOfQueries = 0;
    private boolean containsOnDuplicateKeyUpdate = false;
    private boolean isRewritableWithMultiValuesClause = false;
    private int valuesClauseLength = -1;
    private ArrayList<Integer> valuesEndpoints = new ArrayList();
    private byte[][] staticSqlParts = null;

    public QueryInfo(String sql, Session session, String encoding) {
        boolean isReplace;
        boolean isInsert;
        if (sql == null) {
            throw ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("QueryInfo.NullSql"), session.getExceptionInterceptor());
        }
        this.baseQueryInfo = this;
        this.sql = sql;
        this.encoding = encoding;
        boolean noBackslashEscapes = session.getServerSession().isNoBackslashEscapesSet();
        boolean rewriteBatchedStatements = session.getPropertySet().getBooleanProperty(PropertyKey.rewriteBatchedStatements).getValue();
        boolean dontCheckOnDuplicateKeyUpdateInSQL = session.getPropertySet().getBooleanProperty(PropertyKey.dontCheckOnDuplicateKeyUpdateInSQL).getValue();
        this.queryReturnType = QueryInfo.getQueryReturnType(this.sql, noBackslashEscapes);
        this.queryLength = this.sql.length();
        StringInspector strInspector = new StringInspector(this.sql, "`'\"", "`'\"", OVERRIDING_MARKERS, noBackslashEscapes ? SearchMode.__MRK_COM_MYM_HNT_WS : SearchMode.__BSE_MRK_COM_MYM_HNT_WS);
        this.queryStartPos = strInspector.indexOfNextAlphanumericChar();
        if (this.queryStartPos == -1) {
            this.queryStartPos = this.queryLength;
        } else {
            this.numberOfQueries = 1;
            this.statementFirstChar = Character.toUpperCase(strInspector.getChar());
        }
        boolean bl = isInsert = strInspector.matchesIgnoreCase(INSERT_STATEMENT) != -1;
        if (isInsert) {
            strInspector.incrementPosition(INSERT_STATEMENT.length());
        }
        boolean bl2 = isReplace = !isInsert && strInspector.matchesIgnoreCase(REPLACE_STATEMENT) != -1;
        if (isReplace) {
            strInspector.incrementPosition(REPLACE_STATEMENT.length());
        }
        boolean rewritableAsMultiValues = (isInsert || isReplace) && rewriteBatchedStatements;
        boolean lookForOnDuplicateKeyUpdate = isInsert && (!dontCheckOnDuplicateKeyUpdateInSQL || rewriteBatchedStatements);
        int generalEndpointStart = 0;
        int valuesEndpointStart = 0;
        int valuesClauseBegin = -1;
        boolean valuesClauseBeginFound = false;
        int valuesClauseEnd = -1;
        boolean valuesClauseEndFound = false;
        boolean withinValuesClause = false;
        boolean valueStrMayBeTableName = true;
        int parensLevel = 0;
        int matchEnd = -1;
        int lastPos = -1;
        char lastChar = '\u0000';
        ArrayList<Integer> staticEndpoints = new ArrayList<Integer>();
        while (strInspector.indexOfNextChar() != -1) {
            int currPos = strInspector.getPosition();
            char currChar = strInspector.getChar();
            if (currChar == '?') {
                valueStrMayBeTableName = false;
                ++this.numberOfPlaceholders;
                int endpointEnd = strInspector.getPosition();
                staticEndpoints.add(generalEndpointStart);
                staticEndpoints.add(endpointEnd);
                strInspector.incrementPosition();
                generalEndpointStart = strInspector.getPosition();
                if (rewritableAsMultiValues) {
                    if (!valuesClauseBeginFound) {
                        rewritableAsMultiValues = false;
                    } else if (valuesClauseEndFound) {
                        rewritableAsMultiValues = false;
                    } else if (withinValuesClause) {
                        this.valuesEndpoints.add(valuesEndpointStart);
                        this.valuesEndpoints.add(endpointEnd);
                        valuesEndpointStart = generalEndpointStart;
                    }
                }
            } else if (currChar == ';') {
                valueStrMayBeTableName = false;
                strInspector.incrementPosition();
                if (strInspector.indexOfNextNonWsChar() != -1) {
                    ++this.numberOfQueries;
                    if (rewritableAsMultiValues) {
                        rewritableAsMultiValues = false;
                        valuesClauseBeginFound = false;
                        valuesClauseBegin = -1;
                        valuesClauseEndFound = false;
                        valuesClauseEnd = -1;
                        withinValuesClause = false;
                        parensLevel = 0;
                    }
                    if (dontCheckOnDuplicateKeyUpdateInSQL || this.containsOnDuplicateKeyUpdate) {
                        lookForOnDuplicateKeyUpdate = false;
                    } else {
                        boolean bl3 = isInsert = strInspector.matchesIgnoreCase(INSERT_STATEMENT) != -1;
                        if (isInsert) {
                            strInspector.incrementPosition(INSERT_STATEMENT.length() - 1);
                            currPos = strInspector.getPosition();
                            currChar = strInspector.getChar();
                            strInspector.incrementPosition();
                        }
                        lookForOnDuplicateKeyUpdate = isInsert;
                    }
                }
            } else {
                if (rewritableAsMultiValues) {
                    if ((!valuesClauseBeginFound || valueStrMayBeTableName) && strInspector.matchesIgnoreCase(VALUE_CLAUSE) != -1) {
                        boolean rightBound;
                        boolean leftBound = currPos > lastPos + 1 || lastChar == ')';
                        strInspector.incrementPosition(VALUE_CLAUSE.length() - 1);
                        currPos = strInspector.getPosition();
                        currChar = strInspector.getChar();
                        strInspector.incrementPosition();
                        boolean matchedValues = false;
                        if (strInspector.matchesIgnoreCase("S") != -1) {
                            currPos = strInspector.getPosition();
                            currChar = strInspector.getChar();
                            strInspector.incrementPosition();
                            matchedValues = true;
                        }
                        int endPos = strInspector.getPosition();
                        int nextPos = strInspector.indexOfNextChar();
                        boolean bl4 = rightBound = nextPos > endPos || strInspector.getChar() == '(';
                        if (leftBound && rightBound) {
                            if (matchedValues) {
                                valueStrMayBeTableName = false;
                            }
                            if (matchedValues && this.containsOnDuplicateKeyUpdate) {
                                rewritableAsMultiValues = false;
                            } else {
                                withinValuesClause = true;
                                valuesClauseBegin = strInspector.getPosition();
                                valuesClauseBeginFound = true;
                                valuesEndpointStart = valuesClauseBegin;
                            }
                        }
                    } else if (withinValuesClause && currChar == '(') {
                        ++parensLevel;
                        strInspector.incrementPosition();
                    } else if (withinValuesClause && currChar == ')') {
                        if (--parensLevel < 0) {
                            parensLevel = 0;
                        }
                        strInspector.incrementPosition();
                        valuesClauseEnd = strInspector.getPosition();
                    } else if (withinValuesClause && parensLevel == 0 && isInsert && strInspector.matchesIgnoreCase(AS_CLAUSE) != -1) {
                        valueStrMayBeTableName = false;
                        if (valuesClauseEnd == -1) {
                            valuesClauseEnd = strInspector.getPosition();
                        }
                        valuesClauseEndFound = true;
                        withinValuesClause = false;
                        strInspector.incrementPosition(AS_CLAUSE.length() - 1);
                        currPos = strInspector.getPosition();
                        currChar = strInspector.getChar();
                        strInspector.incrementPosition();
                        this.valuesEndpoints.add(valuesEndpointStart);
                        this.valuesEndpoints.add(valuesClauseEnd);
                    } else if (withinValuesClause && parensLevel == 0 && isInsert && (matchEnd = strInspector.matchesIgnoreCase(ODKU_CLAUSE)) != -1) {
                        valueStrMayBeTableName = false;
                        if (valuesClauseEnd == -1) {
                            valuesClauseEnd = strInspector.getPosition();
                        }
                        valuesClauseEndFound = true;
                        withinValuesClause = false;
                        strInspector.incrementPosition(matchEnd - strInspector.getPosition() - 1);
                        currPos = strInspector.getPosition();
                        currChar = strInspector.getChar();
                        strInspector.incrementPosition();
                        this.valuesEndpoints.add(valuesEndpointStart);
                        this.valuesEndpoints.add(valuesClauseEnd);
                        this.containsOnDuplicateKeyUpdate = true;
                        lookForOnDuplicateKeyUpdate = false;
                    } else if (strInspector.matchesIgnoreCase(LAST_INSERT_ID_FUNC) != -1) {
                        rewritableAsMultiValues = false;
                        strInspector.incrementPosition(LAST_INSERT_ID_FUNC.length() - 1);
                        currPos = strInspector.getPosition();
                        currChar = strInspector.getChar();
                        strInspector.incrementPosition();
                    }
                }
                if (lookForOnDuplicateKeyUpdate && currPos == strInspector.getPosition() && (matchEnd = strInspector.matchesIgnoreCase(ODKU_CLAUSE)) != -1) {
                    strInspector.incrementPosition(matchEnd - strInspector.getPosition() - 1);
                    currPos = strInspector.getPosition();
                    currChar = strInspector.getChar();
                    strInspector.incrementPosition();
                    this.containsOnDuplicateKeyUpdate = true;
                    lookForOnDuplicateKeyUpdate = false;
                }
                if (currPos == strInspector.getPosition()) {
                    strInspector.incrementPosition();
                }
            }
            lastPos = currPos;
            lastChar = currChar;
        }
        staticEndpoints.add(generalEndpointStart);
        staticEndpoints.add(this.queryLength);
        if (rewritableAsMultiValues) {
            if (!valuesClauseEndFound) {
                if (valuesClauseEnd == -1) {
                    valuesClauseEnd = this.queryLength;
                }
                valuesClauseEndFound = true;
                withinValuesClause = false;
                this.valuesEndpoints.add(valuesEndpointStart);
                this.valuesEndpoints.add(valuesClauseEnd);
            }
            if (valuesClauseBeginFound && valuesClauseEndFound) {
                this.valuesClauseLength = valuesClauseEnd - valuesClauseBegin;
            } else {
                rewritableAsMultiValues = false;
            }
        } else {
            this.valuesEndpoints.clear();
        }
        this.isRewritableWithMultiValuesClause = rewritableAsMultiValues;
        this.staticSqlParts = new byte[this.numberOfPlaceholders + 1][];
        int j = 0;
        for (int i = 0; i <= this.numberOfPlaceholders; ++i) {
            int begin = (Integer)staticEndpoints.get(j++);
            int end = (Integer)staticEndpoints.get(j++);
            int length = end - begin;
            this.staticSqlParts[i] = StringUtils.getBytes(this.sql, begin, length, this.encoding);
        }
    }

    private QueryInfo(QueryInfo baseQueryInfo, int batchCount) {
        this.baseQueryInfo = baseQueryInfo;
        this.sql = null;
        this.encoding = this.baseQueryInfo.encoding;
        this.queryReturnType = this.baseQueryInfo.queryReturnType;
        this.queryLength = 0;
        this.queryStartPos = this.baseQueryInfo.queryStartPos;
        this.statementFirstChar = this.baseQueryInfo.statementFirstChar;
        this.batchCount = batchCount;
        this.numberOfPlaceholders = this.baseQueryInfo.numberOfPlaceholders * this.batchCount;
        this.numberOfQueries = 1;
        this.containsOnDuplicateKeyUpdate = this.baseQueryInfo.containsOnDuplicateKeyUpdate;
        this.isRewritableWithMultiValuesClause = true;
        this.valuesClauseLength = -1;
        if (this.numberOfPlaceholders == 0) {
            this.staticSqlParts = new byte[1][];
            int begin = this.baseQueryInfo.valuesEndpoints.get(0);
            int end = this.baseQueryInfo.valuesEndpoints.get(1);
            int length = end - begin;
            byte[] valuesSegment = StringUtils.getBytes(this.baseQueryInfo.sql, begin, length, this.encoding);
            byte[] bindingSegment = StringUtils.getBytes(",", this.encoding);
            ByteBuffer queryByteBuffer = ByteBuffer.allocate(this.baseQueryInfo.queryLength + (length + bindingSegment.length) * (batchCount - 1));
            queryByteBuffer.put(StringUtils.getBytes(this.baseQueryInfo.sql, 0, (int)this.baseQueryInfo.valuesEndpoints.get(1), this.encoding));
            for (int i = 0; i < this.batchCount - 1; ++i) {
                queryByteBuffer.put(bindingSegment);
                queryByteBuffer.put(valuesSegment);
            }
            begin = this.baseQueryInfo.valuesEndpoints.get(1);
            end = this.baseQueryInfo.queryLength;
            length = end - begin;
            queryByteBuffer.put(StringUtils.getBytes(this.baseQueryInfo.sql, begin, length, this.encoding));
            this.staticSqlParts[0] = queryByteBuffer.array();
        } else {
            this.staticSqlParts = new byte[this.numberOfPlaceholders + 1][];
            int begin = this.baseQueryInfo.valuesEndpoints.get(this.baseQueryInfo.valuesEndpoints.size() - 2);
            int end = this.baseQueryInfo.valuesEndpoints.get(this.baseQueryInfo.valuesEndpoints.size() - 1);
            int length = end - begin;
            byte[] valuesEndSegment = StringUtils.getBytes(this.baseQueryInfo.sql, begin, length, this.encoding);
            byte[] delimiter = StringUtils.getBytes(",", this.encoding);
            begin = this.baseQueryInfo.valuesEndpoints.get(0);
            end = this.baseQueryInfo.valuesEndpoints.get(1);
            length = end - begin;
            byte[] valuesBeginSegment = StringUtils.getBytes(this.baseQueryInfo.sql, begin, length, this.encoding);
            ByteBuffer bindingSegmentByteBuffer = ByteBuffer.allocate(valuesEndSegment.length + delimiter.length + valuesBeginSegment.length);
            bindingSegmentByteBuffer.put(valuesEndSegment).put(delimiter).put(valuesBeginSegment);
            byte[] bindingSegment = bindingSegmentByteBuffer.array();
            this.staticSqlParts[0] = this.baseQueryInfo.staticSqlParts[0];
            int i = 0;
            int p = 1;
            while (i < this.batchCount) {
                int j = 1;
                while (j < this.baseQueryInfo.staticSqlParts.length - 1) {
                    this.staticSqlParts[p] = this.baseQueryInfo.staticSqlParts[j];
                    ++j;
                    ++p;
                }
                this.staticSqlParts[p] = bindingSegment;
                ++i;
                ++p;
            }
            this.staticSqlParts[this.staticSqlParts.length - 1] = this.baseQueryInfo.staticSqlParts[this.baseQueryInfo.staticSqlParts.length - 1];
        }
    }

    public int getNumberOfQueries() {
        return this.numberOfQueries;
    }

    public QueryReturnType getQueryReturnType() {
        return this.queryReturnType;
    }

    public char getFirstStmtChar() {
        return this.baseQueryInfo.statementFirstChar;
    }

    public int getValuesClauseLength() {
        return this.baseQueryInfo.valuesClauseLength;
    }

    public boolean containsOnDuplicateKeyUpdate() {
        return this.containsOnDuplicateKeyUpdate;
    }

    public byte[][] getStaticSqlParts() {
        return this.staticSqlParts;
    }

    public boolean isRewritableWithMultiValuesClause() {
        return this.isRewritableWithMultiValuesClause;
    }

    public QueryInfo getQueryInfoForBatch(int count) {
        if (count == 1) {
            return this.baseQueryInfo;
        }
        if (count == this.batchCount) {
            return this;
        }
        if (!this.isRewritableWithMultiValuesClause) {
            return null;
        }
        return new QueryInfo(this.baseQueryInfo, count);
    }

    public String getSqlForBatch() {
        if (this.batchCount == 1) {
            return this.baseQueryInfo.sql;
        }
        int size = this.baseQueryInfo.queryLength + (this.batchCount - 1) * this.baseQueryInfo.valuesClauseLength + this.batchCount - 1;
        StringBuilder buf = new StringBuilder(size);
        buf.append(StringUtils.toString(this.staticSqlParts[0], this.encoding));
        for (int i = 1; i < this.staticSqlParts.length; ++i) {
            buf.append("?").append(StringUtils.toString(this.staticSqlParts[i], this.encoding));
        }
        return buf.toString();
    }

    public String getSqlForBatch(int count) {
        QueryInfo batchInfo = this.getQueryInfoForBatch(count);
        return batchInfo.getSqlForBatch();
    }

    public static int indexOfStatementKeyword(String sql, boolean noBackslashEscapes) {
        return StringUtils.indexOfNextAlphanumericChar(0, sql, "`'\"", "`'\"", OVERRIDING_MARKERS, noBackslashEscapes ? SearchMode.__MRK_COM_MYM_HNT_WS : SearchMode.__BSE_MRK_COM_MYM_HNT_WS);
    }

    public static char firstCharOfStatementUc(String sql, boolean noBackslashEscapes) {
        int statementKeywordPos = QueryInfo.indexOfStatementKeyword(sql, noBackslashEscapes);
        if (statementKeywordPos == -1) {
            return '\u0000';
        }
        return Character.toUpperCase(sql.charAt(statementKeywordPos));
    }

    public static boolean isReadOnlySafeQuery(String sql, boolean noBackslashEscapes) {
        int statementKeywordPos = QueryInfo.indexOfStatementKeyword(sql, noBackslashEscapes);
        if (statementKeywordPos == -1) {
            return true;
        }
        char firstStatementChar = Character.toUpperCase(sql.charAt(statementKeywordPos));
        if (firstStatementChar == 'A' && StringUtils.startsWithIgnoreCaseAndWs(sql, "ALTER", statementKeywordPos)) {
            return false;
        }
        if (firstStatementChar == 'C' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "CHANGE", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE", statementKeywordPos))) {
            return false;
        }
        if (firstStatementChar == 'D' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "DELETE", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "DROP", statementKeywordPos))) {
            return false;
        }
        if (firstStatementChar == 'G' && StringUtils.startsWithIgnoreCaseAndWs(sql, "GRANT", statementKeywordPos)) {
            return false;
        }
        if (firstStatementChar == 'I' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "IMPORT", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, INSERT_STATEMENT, statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "INSTALL", statementKeywordPos))) {
            return false;
        }
        if (firstStatementChar == 'L' && StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD", statementKeywordPos)) {
            return false;
        }
        if (firstStatementChar == 'O' && StringUtils.startsWithIgnoreCaseAndWs(sql, "OPTIMIZE", statementKeywordPos)) {
            return false;
        }
        if (firstStatementChar == 'R' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "RENAME", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "REPAIR", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, REPLACE_STATEMENT, statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "RESET", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "REVOKE", statementKeywordPos))) {
            return false;
        }
        if (firstStatementChar == 'T' && StringUtils.startsWithIgnoreCaseAndWs(sql, "TRUNCATE", statementKeywordPos)) {
            return false;
        }
        if (firstStatementChar == 'U' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "UNINSTALL", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "UPDATE", statementKeywordPos))) {
            return false;
        }
        if (firstStatementChar == 'W' && StringUtils.startsWithIgnoreCaseAndWs(sql, "WITH", statementKeywordPos)) {
            String context = QueryInfo.getContextForWithStatement(sql, noBackslashEscapes);
            return context == null || !context.equalsIgnoreCase("DELETE") && !context.equalsIgnoreCase("UPDATE");
        }
        return true;
    }

    public static QueryReturnType getQueryReturnType(String sql, boolean noBackslashEscapes) {
        int statementKeywordPos = QueryInfo.indexOfStatementKeyword(sql, noBackslashEscapes);
        if (statementKeywordPos == -1) {
            return QueryReturnType.NONE;
        }
        char firstStatementChar = Character.toUpperCase(sql.charAt(statementKeywordPos));
        if (firstStatementChar == 'A' && StringUtils.startsWithIgnoreCaseAndWs(sql, "ANALYZE", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'C' && StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL", statementKeywordPos)) {
            return QueryReturnType.MAY_PRODUCE_RESULT_SET;
        }
        if (firstStatementChar == 'C' && StringUtils.startsWithIgnoreCaseAndWs(sql, "CHECK", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'D' && StringUtils.startsWithIgnoreCaseAndWs(sql, "DESC", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'E' && StringUtils.startsWithIgnoreCaseAndWs(sql, "EXPLAIN", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'E' && StringUtils.startsWithIgnoreCaseAndWs(sql, "EXECUTE", statementKeywordPos)) {
            return QueryReturnType.MAY_PRODUCE_RESULT_SET;
        }
        if (firstStatementChar == 'H' && StringUtils.startsWithIgnoreCaseAndWs(sql, "HELP", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'O' && StringUtils.startsWithIgnoreCaseAndWs(sql, "OPTIMIZE", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'R' && StringUtils.startsWithIgnoreCaseAndWs(sql, "REPAIR", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'S' && (StringUtils.startsWithIgnoreCaseAndWs(sql, "SELECT", statementKeywordPos) || StringUtils.startsWithIgnoreCaseAndWs(sql, "SHOW", statementKeywordPos))) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'T' && StringUtils.startsWithIgnoreCaseAndWs(sql, "TABLE", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'V' && StringUtils.startsWithIgnoreCaseAndWs(sql, "VALUES", statementKeywordPos)) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        if (firstStatementChar == 'W' && StringUtils.startsWithIgnoreCaseAndWs(sql, "WITH", statementKeywordPos)) {
            String context = QueryInfo.getContextForWithStatement(sql, noBackslashEscapes);
            if ("SELECT".equalsIgnoreCase(context) || "TABLE".equalsIgnoreCase(context) || "VALUES".equalsIgnoreCase(context)) {
                return QueryReturnType.PRODUCES_RESULT_SET;
            }
            if ("UPDATE".equalsIgnoreCase(context) || "DELETE".equalsIgnoreCase(context)) {
                return QueryReturnType.DOES_NOT_PRODUCE_RESULT_SET;
            }
            return QueryReturnType.MAY_PRODUCE_RESULT_SET;
        }
        if (firstStatementChar == 'X' && StringUtils.indexOfIgnoreCase(statementKeywordPos, sql, new String[]{"XA", "RECOVER"}, "`'\"", "`'\"", noBackslashEscapes ? SearchMode.__MRK_COM_MYM_HNT_WS : SearchMode.__FULL) == statementKeywordPos) {
            return QueryReturnType.PRODUCES_RESULT_SET;
        }
        return QueryReturnType.DOES_NOT_PRODUCE_RESULT_SET;
    }

    private static String getContextForWithStatement(String sql, boolean noBackslashEscapes) {
        String section;
        String commentsFreeSql = StringUtils.stripCommentsAndHints(sql, "`'\"", "`'\"", !noBackslashEscapes);
        StringInspector strInspector = new StringInspector(commentsFreeSql, "`'\"(", "`'\")", "`'\"", noBackslashEscapes ? SearchMode.__MRK_COM_MYM_HNT_WS : SearchMode.__BSE_MRK_COM_MYM_HNT_WS);
        boolean asFound = false;
        while (true) {
            int nws;
            if ((nws = strInspector.indexOfNextNonWsChar()) == -1) {
                return null;
            }
            int ws = strInspector.indexOfNextWsChar();
            if (ws == -1) {
                ws = commentsFreeSql.length();
            }
            section = commentsFreeSql.substring(nws, ws);
            if (!asFound && section.equalsIgnoreCase(AS_CLAUSE)) {
                asFound = true;
                continue;
            }
            if (!asFound) continue;
            if (section.charAt(0) != ',') break;
            asFound = false;
        }
        return section;
    }

    public static boolean containsOnDuplicateKeyUpdateClause(String sql, boolean noBackslashEscapes) {
        return StringUtils.indexOfIgnoreCase(0, sql, ODKU_CLAUSE, "`'\"", "`'\"", noBackslashEscapes ? SearchMode.__MRK_COM_MYM_HNT_WS : SearchMode.__BSE_MRK_COM_MYM_HNT_WS) != -1;
    }
}

