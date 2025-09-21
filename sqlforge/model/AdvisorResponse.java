/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.model;

import java.util.ArrayList;
import java.util.List;

public class AdvisorResponse {
    private boolean ok = true;
    private String message;
    private List<String> tips = new ArrayList<>();
    private int score;
    private String originalSql;
    private String timestamp;

    public static AdvisorResponse error(String msg) {
        AdvisorResponse r = new AdvisorResponse();
        r.ok = false;
        r.message = msg;
        return r;
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

    public List<String> getTips() {
        return tips;
    }

    public void setTips(List<String> tips) {
        this.tips = tips;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
