package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.AuditLog;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.repository.AuditLogRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConstraintIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void userEmailUniqueness_ShouldThrowDataIntegrityViolationExceptionOnDuplicate() {
        String duplicateEmail = "unique_" + UUID.randomUUID() + "@example.com";
        User u1 = TestFixtures.createUser(duplicateEmail, "User", "One");
        userRepository.saveAndFlush(u1);

        User u2 = TestFixtures.createUser(duplicateEmail, "User", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(u2));
    }

    @Test
    void workspaceMemberUniqueness_ShouldThrowDataIntegrityViolationExceptionOnDuplicate() {
        User owner = userRepository.saveAndFlush(TestFixtures.createUser("owner_" + UUID.randomUUID() + "@example.com", "Owner", "One"));
        Workspace workspace = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("WS_" + UUID.randomUUID(), owner));

        User memberUser = userRepository.saveAndFlush(TestFixtures.createUser("member_" + UUID.randomUUID() + "@example.com", "Member", "One"));
        WorkspaceMember m1 = TestFixtures.createWorkspaceMember(workspace, memberUser, WorkspaceRole.EDITOR);
        workspaceMemberRepository.saveAndFlush(m1);

        WorkspaceMember m2 = TestFixtures.createWorkspaceMember(workspace, memberUser, WorkspaceRole.VIEWER);
        assertThrows(DataIntegrityViolationException.class, () -> workspaceMemberRepository.saveAndFlush(m2));
    }

    @Test
    void auditLogOnDeleteSetNull_ShouldPreserveAuditLogWhenUserOrWorkspaceDeleted() {
        User owner = userRepository.saveAndFlush(TestFixtures.createUser("ws_owner_" + UUID.randomUUID() + "@example.com", "Owner", "User"));
        User auditActor = userRepository.saveAndFlush(TestFixtures.createUser("audit_actor_" + UUID.randomUUID() + "@example.com", "Audit", "Actor"));
        Workspace workspace = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("Audit_WS_" + UUID.randomUUID(), owner));

        AuditLog auditLog = new AuditLog();
        auditLog.setWorkspace(workspace);
        auditLog.setUser(auditActor);
        auditLog.setAction(AuditAction.WORKSPACE_CREATED);
        auditLog.setEntityType(AuditEntityType.WORKSPACE);
        auditLog.setEntityId(workspace.getId());
        auditLog.setSnapshotDelta("{\"name\": \"" + workspace.getName() + "\"}");
        auditLog.setTimestamp(Instant.now());

        auditLog = auditLogRepository.saveAndFlush(auditLog);
        UUID auditId = auditLog.getId();

        // Delete audit actor user
        userRepository.delete(auditActor);
        userRepository.flush();

        AuditLog preservedLog = auditLogRepository.findById(auditId).orElse(null);
        assertNotNull(preservedLog, "Audit log should not be deleted when associated user is deleted");
        assertNull(preservedLog.getUser(), "Audit log user reference should set to NULL when user deleted");
    }
}
