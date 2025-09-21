/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request wrapper for queries.
 */
public class QueryRequest {
    private String userId;

    @NotBlank(message = "SQL or text must be provided")
    private String sql;

    // mode: "read" (execute), "explain"
    private String mode = "read";

    public QueryRequest() {}

    public String getUserId() {
        return userId;
    }

    public QueryRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public QueryRequest setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public QueryRequest setMode(String mode) {
        this.mode = mode;
        return this;
    }
}
