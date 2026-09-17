from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
DB = ROOT / 'com/ultimateduels/database/DatabaseManager.java'

text = DB.read_text(encoding='utf-8')

execute_insert = '''    public long executeInsert(@NotNull String sql, Object ... params) throws SQLException {
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, 1)) {
            this.setParameters(stmt, params);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1L;
        }
    }

'''

execute_query_single = '''    @NotNull
    public <T> Optional<T> executeQuerySingle(@NotNull String sql, @NotNull ResultSetMapper<T> mapper, Object ... params) throws SQLException {
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            this.setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapper.map(rs));
                }
            }
        }
        return Optional.empty();
    }

'''

pattern_insert = re.compile(
    r'    /\*\s*\n     \* Loose catch block\s*\n     \*/\s*\n'
    r'    public long executeInsert\(@NotNull String sql, Object \.\.\. params\) throws SQLException \{.*?\n    \}\n\n'
    r'    public int executeUpdate',
    re.S,
)

replacement_insert = execute_insert + '    public int executeUpdate'
text, n1 = pattern_insert.subn(replacement_insert, text, count=1)

pattern_query = re.compile(
    r'    /\*\s*\n     \* Loose catch block\s*\n     \*/\s*\n'
    r'    @NotNull\s*\n'
    r'    public <T> Optional<T> executeQuerySingle\(@NotNull String sql, @NotNull ResultSetMapper<T> mapper, Object \.\.\. params\) throws SQLException \{.*?\n    \}\n\n'
    r'    @NotNull\s*\n    public <T> List<T> executeQueryList',
    re.S,
)

replacement_query = execute_query_single + '    @NotNull\n    public <T> List<T> executeQueryList'
text, n2 = pattern_query.subn(replacement_query, text, count=1)

if n1 != 1 or n2 != 1:
    raise SystemExit(f'DatabaseManager repair did not match expected CFR output (insert={n1}, query={n2})')

DB.write_text(text, encoding='utf-8')
print('Repaired DatabaseManager CFR output')
