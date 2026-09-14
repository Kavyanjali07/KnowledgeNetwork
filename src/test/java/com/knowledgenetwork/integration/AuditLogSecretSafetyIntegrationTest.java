package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.model.AuditLog;
import com.knowledgenetwork.repository.AuditLogRepository;
import com.knowledgenetwork.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogSecretSafetyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void recordEvent_ShouldSanitizeSensitiveKeysBeforePersistingToPostgres() {
        UUID entityId = UUID.randomUUID();
        Map<String, Object> payloadWithSecrets = Map.of(
                "username", "john_doe",
                "password", "SuperSecretPassword123!",
                "token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "otp", "987654",
                "normalField", "Safe Value"
        );

        auditLogService.recordEvent(
                AuditAction.LOGIN_FAILURE,
                AuditEntityType.AUTH,
                entityId,
                null,
                payloadWithSecrets
        );

        // Fetch audit log directly from PostgreSQL
        AuditLog persistedLog = auditLogRepository.findAll().stream()
                .filter(l -> AuditAction.LOGIN_FAILURE.equals(l.getAction()))
                .findFirst()
                .orElse(null);

        assertNotNull(persistedLog, "Expected audit log record persisted in PostgreSQL");
        String snapshotDelta = persistedLog.getSnapshotDelta();
        assertNotNull(snapshotDelta);

        assertTrue(snapshotDelta.contains("Safe Value"));
        assertTrue(snapshotDelta.contains("[REDACTED]"), "Sensitive payload keys must be redacted in PostgreSQL audit log JSON");
        assertFalse(snapshotDelta.contains("SuperSecretPassword123!"), "Raw password must not be present in PostgreSQL audit log JSON");
    }
}
