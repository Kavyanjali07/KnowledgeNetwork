package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlywayMigrationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrations_ShouldSuccessfullyApplyAllMigrationsOnCleanDatabase() {
        // Verify flyway schema history table exists and contains V1 to V15
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true", Integer.class);

        assertNotNull(count);
        assertTrue(count >= 15, "Expected at least 15 successful Flyway migrations, found: " + count);

        // Verify core tables exist in PostgreSQL
        List<String> expectedTables = List.of(
                "users", "workspaces", "workspace_members",
                "node_types", "edge_types", "nodes", "edges",
                "graph_versions", "graph_snapshots", "graph_forks",
                "audit_logs", "verification_otps", "refresh_tokens"
        );

        for (String table : expectedTables) {
            Boolean tableExists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)",
                    Boolean.class, table);
            assertTrue(Boolean.TRUE.equals(tableExists), "Expected database table missing: " + table);
        }
    }

    @Test
    void auditLogIndexes_ShouldExistInPostgresSchema() {
        Boolean indexExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_audit_logs_workspace_timestamp')",
                Boolean.class);
        assertTrue(Boolean.TRUE.equals(indexExists), "Expected audit log index idx_audit_logs_workspace_timestamp missing");
    }
}
