/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
package com.sqlforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SQLForge main entrypoint.
 * Produces embedded web server, serves SPA and REST API.
 */
@SpringBootApplication
public class SqlForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqlForgeApplication.class, args);
    }
}
/*
 * Copyright © 2025 Devin B. Royal.
 * All Rights Reserved.
 */
