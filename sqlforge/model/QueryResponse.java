/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Standardized response wrapper for query results.
 */
public class QueryResponse {
    private boolean ok;
    private String message;
    private String timestamp;
    private List<Map<String, Object>> rows = new ArrayList<>();
    private int rowCount;
    private String sql; // for NL->SQL conversions

    public static QueryResponse ok() {
        QueryResponse r = new QueryResponse();
        r.ok = true;
        r.message = "OK";
        return r;
    }

    public static QueryResponse error(String msg) {
        QueryResponse r = new QueryResponse();
        r.ok = false;
        r.message = msg;
        return r;
    }

    public QueryResponse withSql(String sql) {
        this.sql = sql;
        return this;
    }

    // getters / setters

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
