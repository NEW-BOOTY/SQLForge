/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge.controller;

import com.sqlforge.model.AdvisorResponse;
import com.sqlforge.model.QueryRequest;
import com.sqlforge.model.QueryResponse;
import com.sqlforge.service.AdvisorService;
import com.sqlforge.service.DatabaseService;
import com.sqlforge.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API endpoints for SQLForge SPA.
 */
@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private final DatabaseService dbService;
    private final QueryService queryService;
    private final AdvisorService advisorService;

    public ApiController(DatabaseService dbService,
                         QueryService queryService,
                         AdvisorService advisorService) {
        this.dbService = dbService;
        this.queryService = queryService;
        this.advisorService = advisorService;
    }

    @PostMapping("/run")
    public ResponseEntity<QueryResponse> runQuery(@Valid @RequestBody QueryRequest req) {
        try {
            log.info("Run request received (userId={}, mode={})", req.getUserId(), req.getMode());
            QueryResponse resp = dbService.executeSafe(req.getSql(), req.getMode());
            queryService.recordHistory(req.getUserId(), req.getSql());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            log.warn("Bad request: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(QueryResponse.error(iae.getMessage()));
        } catch (Exception ex) {
            log.error("Error running query", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(QueryResponse.error("Internal server error: " + ex.getMessage()));
        }
    }

    @PostMapping("/explain")
    public ResponseEntity<QueryResponse> explain(@Valid @RequestBody QueryRequest req) {
        try {
            QueryResponse resp = dbService.explain(req.getSql());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(QueryResponse.error(iae.getMessage()));
        } catch (Exception ex) {
            log.error("Explain failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(QueryResponse.error("Internal server error: " + ex.getMessage()));
        }
    }

    @PostMapping("/advice")
    public ResponseEntity<AdvisorResponse> advice(@Valid @RequestBody QueryRequest req) {
        try {
            AdvisorResponse resp = advisorService.advise(req.getSql());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Advice failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdvisorResponse.error("Internal server error: " + ex.getMessage()));
        }
    }

    @PostMapping("/nl-to-sql")
    public ResponseEntity<QueryResponse> nlToSql(@Valid @RequestBody QueryRequest req) {
        try {
            String sql = queryService.nlToSql(req.getSql()); // treat req.sql as NL text
            return ResponseEntity.ok(QueryResponse.ok().withSql(sql));
        } catch (Exception ex) {
            log.error("NL->SQL conversion failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(QueryResponse.error("Conversion failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<String>> history(@RequestParam(name = "userId", required = false) String userId) {
        return ResponseEntity.ok(queryService.getHistory(userId));
    }
}
/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
