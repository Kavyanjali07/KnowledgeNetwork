package com.knowledgenetwork.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.model.AuditLog;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.AuditLogResponse;
import com.knowledgenetwork.repository.AuditLogRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.*;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordhash", "oldpassword", "newpassword", "confirmPassword",
            "token", "accesstoken", "refreshtoken", "otp", "code", "resetcode",
            "authorization", "cookie", "secret", "credentials"
    );

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           UserRepository userRepository,
                           WorkspaceRepository workspaceRepository,
                           WorkspaceSecurityValidator workspaceSecurityValidator,
                           ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.objectMapper = objectMapper;
    }

    /**
     * Records an audit event automatically resolving current authenticated user and HTTP context.
     */
    @Transactional
    public AuditLog recordEvent(AuditAction action, AuditEntityType entityType, UUID entityId, UUID workspaceId, Map<String, Object> metadata) {
        User actor = SecurityUtils.getCurrentUserPrincipal()
                .flatMap(principal -> userRepository.findById(principal.getId()))
                .orElse(null);
        return recordEventInternal(action, entityType, entityId, workspaceId, actor, metadata);
    }

    /**
     * Records an audit event with an explicitly provided user (e.g. for login/register/reset flows).
     */
    @Transactional
    public AuditLog recordEventForUser(AuditAction action, AuditEntityType entityType, UUID entityId, UUID workspaceId, User user, Map<String, Object> metadata) {
        return recordEventInternal(action, entityType, entityId, workspaceId, user, metadata);
    }

    private AuditLog recordEventInternal(AuditAction action, AuditEntityType entityType, UUID entityId, UUID workspaceId, User user, Map<String, Object> metadata) {
        AuditLog logEntry = new AuditLog();
        logEntry.setAction(action);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setUser(user);
        logEntry.setTimestamp(Instant.now());

        if (workspaceId != null) {
            workspaceRepository.findById(workspaceId).ifPresent(logEntry::setWorkspace);
        }

        Map<String, Object> sanitizedMetadata = sanitizeAndEnrichMetadata(metadata);
        try {
            logEntry.setSnapshotDelta(objectMapper.writeValueAsString(sanitizedMetadata));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log metadata to JSON", e);
            logEntry.setSnapshotDelta("{}");
        }

        AuditLog saved = auditLogRepository.save(logEntry);
        log.debug("Recorded audit log [{}] action=[{}] entityType=[{}] entityId=[{}] workspaceId=[{}] actorId=[{}]",
                saved.getId(), action, entityType, entityId, workspaceId, user != null ? user.getId() : "ANONYMOUS");
        return saved;
    }

    /**
     * Retrieves paginated audit logs with authorization checks and filtering.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(UUID workspaceId,
                                                UUID actorId,
                                                AuditAction action,
                                                AuditEntityType entityType,
                                                UUID entityId,
                                                Instant startDate,
                                                Instant endDate,
                                                Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new AccessDeniedException("Workspace not found: " + workspaceId));
            workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);
        }

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (workspaceId != null) {
                predicates.add(cb.equal(root.get("workspace").get("id"), workspaceId));
            }
            if (actorId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), actorId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (entityType != null) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        return page.map(this::mapToResponse);
    }

    private Map<String, Object> sanitizeAndEnrichMetadata(Map<String, Object> input) {
        Map<String, Object> map = new HashMap<>();
        if (input != null) {
            for (Map.Entry<String, Object> entry : input.entrySet()) {
                if (entry.getKey() != null && !isSensitiveKey(entry.getKey())) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // Add HTTP Request context if available
        HttpServletRequest httpRequest = getCurrentHttpRequest();
        if (httpRequest != null) {
            if (!map.containsKey("ipAddress")) {
                map.put("ipAddress", getClientIpAddress(httpRequest));
            }
            if (!map.containsKey("userAgent")) {
                String ua = httpRequest.getHeader("User-Agent");
                map.put("userAgent", ua != null ? ua : "UNKNOWN");
            }
        }

        return map;
    }

    private boolean isSensitiveKey(String key) {
        String lower = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lower::contains);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        Map<String, Object> metadataMap = new HashMap<>();
        if (log.getSnapshotDelta() != null && !log.getSnapshotDelta().isBlank()) {
            try {
                metadataMap = objectMapper.readValue(log.getSnapshotDelta(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                metadataMap.put("raw", log.getSnapshotDelta());
            }
        }

        UUID workspaceId = log.getWorkspace() != null ? log.getWorkspace().getId() : null;
        UUID actorId = log.getUser() != null ? log.getUser().getId() : null;
        String actorEmail = log.getUser() != null ? log.getUser().getEmail() : "ANONYMOUS";
        String actorName = log.getUser() != null
                ? (log.getUser().getFirstName() + " " + log.getUser().getLastName()).trim()
                : "System / Anonymous";

        return new AuditLogResponse(
                log.getId(),
                workspaceId,
                actorId,
                actorEmail,
                actorName,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                metadataMap,
                log.getTimestamp()
        );
    }
}
