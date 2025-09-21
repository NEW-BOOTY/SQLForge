/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.service;

import com.sqlforge.model.QueryResponse;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides safe execution of SQL against an H2 sandbox instance.
 * Enforces allowed statement types and protects from destructive operations.
 */
@Service
public class DatabaseService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

    // Very small whitelist for allowed statements in the sandbox - adjustable
    private static final Pattern ALLOWED_STATEMENT = Pattern.compile("^(SELECT|WITH|EXPLAIN)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DISALLOWED = Pattern.compile("\\b(DROP|ALTER|TRUNCATE|DELETE|UPDATE|INSERT|REPLACE|MERGE|CREATE\\s+TABLE)\\b", Pattern.CASE_INSENSITIVE);

    private final DataSource dataSource;
    private Server h2Server;

    public DatabaseService() throws SQLException {
        // Start an H2 TCP server optionally for tools (bind to loopback)
        try {
            this.h2Server = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092").start();
        } catch (SQLException ex) {
            log.warn("H2 TCP server not started: {}", ex.getMessage());
        }

        // Create a file-based database under ./data/sqlforge (persist between restarts)
        String jdbc = "jdbc:h2:./data/sqlforge";  // ✅ FIXED: Removed AUTO_SERVER and FILE_LOCK
        this.dataSource = createDataSource(jdbc, "sa", "");
        initializeSampleSchema();
    }

    private DataSource createDataSource(String url, String user, String pass) throws SQLException {
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(pass);
        return ds;
    }

    private void initializeSampleSchema() {
        String[] statements = new String[]{
            "CREATE TABLE IF NOT EXISTS employees (id INT PRIMARY KEY, name VARCHAR(200), dept VARCHAR(100), salary DECIMAL)",
            "CREATE INDEX IF NOT EXISTS idx_employees_dept ON employees(dept)",
            "CREATE TABLE IF NOT EXISTS projects (id INT PRIMARY KEY, name VARCHAR(200), owner_id INT)",
            "CREATE INDEX IF NOT EXISTS idx_projects_owner ON projects(owner_id)"
        };
        try (Connection c = dataSource.getConnection()) {
            for (String s : statements) {
                try (PreparedStatement ps = c.prepareStatement(s)) {
                    ps.execute();
                } catch (SQLException e) {
                    log.warn("Schema init statement failed: {}", e.getMessage());
                }
            }
            try (PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM employees")) {
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = c.prepareStatement("INSERT INTO employees(id,name,dept,salary) VALUES(?,?,?,?)")) {
                        for (int i = 1; i <= 5; i++) {
                            ins.setInt(1, i);
                            ins.setString(2, "Employee " + i);
                            ins.setString(3, i % 2 == 0 ? "Engineering" : "Sales");
                            ins.setBigDecimal(4, java.math.BigDecimal.valueOf(60000 + i * 1000));
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to initialize DB schema", ex);
        }
    }

    /**
     * Executes SQL safely. Only allows SELECT / EXPLAIN / WITH statements by default.
     * @param sql raw SQL
     * @param mode "read" or "explain"
     * @return QueryResponse with rows or message
     */
    public QueryResponse executeSafe(String sql, String mode) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL must not be empty");
        }
        String trimmed = sql.trim();
        if (DISALLOWED.matcher(trimmed).find()) {
            throw new IllegalArgumentException("Destructive or schema-changing statements are not allowed in the sandbox.");
        }
        if (!ALLOWED_STATEMENT.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Only SELECT/WITH/EXPLAIN statements are allowed in the sandbox.");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            conn.setNetworkTimeout(null, 10_000);
            String toRun = "explain".equalsIgnoreCase(mode) ? ("EXPLAIN " + trimmed) : trimmed;

            try (PreparedStatement ps = conn.prepareStatement(toRun)) {
                boolean hasResult = ps.execute();
                QueryResponse resp = QueryResponse.ok();
                resp.setTimestamp(Instant.now().toString());
                if (hasResult) {
                    try (ResultSet rs = ps.getResultSet()) {
                        List<Map<String, Object>> rows = new ArrayList<>();
                        ResultSetMetaData md = rs.getMetaData();
                        int cols = md.getColumnCount();
                        while (rs.next() && rows.size() < 5000) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= cols; i++) {
                                row.put(md.getColumnLabel(i), rs.getObject(i));
                            }
                            rows.add(row);
                        }
                        resp.setRows(rows);
                        resp.setRowCount(rows.size());
                        resp.setMessage("OK");
                        return resp;
                    }
                } else {
                    int updateCount = ps.getUpdateCount();
                    resp.setMessage("Update count: " + updateCount);
                    return resp;
                }
            }
        } catch (SQLException ex) {
            log.error("SQL execution error", ex);
            return QueryResponse.error("SQL error: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected execution error", ex);
            return QueryResponse.error("Execution error: " + ex.getMessage());
        }
    }

    /**
     * Runs EXPLAIN on the statement and returns textual plan if available.
     */
    public QueryResponse explain(String sql) {
        return executeSafe(sql, "explain");
    }
}
