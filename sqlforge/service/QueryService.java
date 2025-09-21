/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Query utilities: lightweight NL->SQL mapping, history store.
 * This is intentionally rule-based to avoid external dependencies.
 */
@Service
public class QueryService {
    private static final Logger log = LoggerFactory.getLogger(QueryService.class);

    // Simple in-memory per-user history. For production, replace with persistent store.
    private final Map<String, Deque<String>> history = new ConcurrentHashMap<>();
    private final int HISTORY_LIMIT = 100;

    public void recordHistory(String userId, String sql) {
        String uid = (userId == null || userId.isBlank()) ? "anonymous" : userId;
        history.putIfAbsent(uid, new ArrayDeque<>());
        Deque<String> q = history.get(uid);
        synchronized (q) {
            q.addFirst(sql);
            while (q.size() > HISTORY_LIMIT) q.removeLast();
        }
    }

    public List<String> getHistory(String userId) {
        String uid = (userId == null || userId.isBlank()) ? "anonymous" : userId;
        Deque<String> q = history.getOrDefault(uid, new ArrayDeque<>());
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }

    /**
     * Very small local NL->SQL translator using heuristics.
     * Examples:
     *  - "list employees in engineering" -> SELECT * FROM employees WHERE dept = 'Engineering'
     *  - "top salaries" -> SELECT * FROM employees ORDER BY salary DESC LIMIT 10
     */
    public String nlToSql(String nl) {
        if (nl == null || nl.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }
        String t = nl.trim().toLowerCase(Locale.ROOT);
        log.info("Converting NL to SQL: {}", t);

        // heuristics
        if (t.matches(".*top .*salary.*") || t.matches(".*highest .*salary.*")) {
            return "SELECT * FROM employees ORDER BY salary DESC LIMIT 10";
        }
        if (t.matches(".*list .*employees.*in .*")) {
            String dept = extractAfter(t, "in ");
            if (dept != null) {
                dept = capitalizeWords(dept);
                return "SELECT * FROM employees WHERE dept = '" + escapeLiteral(dept) + "' LIMIT 100";
            }
        }
        if (t.matches(".*count .*employees.*")) {
            return "SELECT COUNT(*) AS total_employees FROM employees";
        }
        if (t.matches(".*projects owned by .*")) {
            String who = extractAfter(t, "owned by ");
            if (who != null) {
                who = escapeLiteral(capitalizeWords(who));
                return "SELECT p.* FROM projects p JOIN employees e ON p.owner_id = e.id WHERE e.name LIKE '%" + who + "%'";
            }
        }
        // fallback -> search across tables
        return "SELECT * FROM employees LIMIT 50";
    }

    private String extractAfter(String text, String marker) {
        int idx = text.indexOf(marker);
        if (idx < 0) return null;
        String s = text.substring(idx + marker.length()).trim();
        // take first phrase
        int end = s.indexOf(" ");
        return end > 0 ? s.substring(0, end) : s;
    }

    private String capitalizeWords(String s) {
        String[] parts = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private String escapeLiteral(String s) {
        return s.replace("'", "''");
    }
}
