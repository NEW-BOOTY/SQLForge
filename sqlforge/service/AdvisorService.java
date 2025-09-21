/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.service;

import com.sqlforge.model.AdvisorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Simple rule-based advisor that analyzes SQL text and suggests improvements.
 * For production replace with deeper static analysis and cost models.
 */
@Service
public class AdvisorService {
    private static final Logger log = LoggerFactory.getLogger(AdvisorService.class);

    public AdvisorResponse advise(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return AdvisorResponse.error("SQL empty");
        }
        String s = sql.toLowerCase(Locale.ROOT);
        AdvisorResponse resp = new AdvisorResponse();
        resp.setOriginalSql(sql);
        List<String> tips = new ArrayList<>();
        try {
            // Suggest using indexes if WHERE on non-indexed columns (heuristic)
            if (s.contains("where") && !s.contains("idx_")) {
                tips.add("Check if WHERE columns are indexed. Consider adding appropriate indexes for selective filters.");
            }
            if (s.contains("select *")) {
                tips.add("Avoid SELECT *. Specify columns to reduce I/O and network transfer.");
            }
            if (s.contains("order by") && !s.contains("limit")) {
                tips.add("Consider adding LIMIT when ordering large result sets to avoid full sort spills.");
            }
            if (s.contains("join") && s.contains("on")) {
                tips.add("Ensure JOIN conditions use indexed columns and correct join types.");
            }
            if (s.length() > 1000) {
                tips.add("Query is long — consider breaking into CTEs for readability and optimizer hints.");
            }

            // simple cardinality guess
            if (s.contains("count(")) {
                tips.add("COUNT can be expensive on large tables; consider using indexed counters or summarized tables.");
            }

            // advisory scoring
            int score = Math.max(0, 100 - (tips.size() * 10));
            resp.setScore(score);
            resp.setTips(tips);
            resp.setTimestamp(new Date().toString());
            return resp;
        } catch (Exception ex) {
            log.error("Advice analysis failed", ex);
            return AdvisorResponse.error("Advice analysis failed: " + ex.getMessage());
        }
    }
}
