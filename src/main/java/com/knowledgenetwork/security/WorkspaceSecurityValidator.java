package com.knowledgenetwork.security;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WorkspaceSecurityValidator {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    public WorkspaceSecurityValidator(WorkspaceMemberRepository workspaceMemberRepository,
                                       UserRepository userRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
    }

    public void validateReadAccess(Workspace workspace, UUID userId) {
        if (workspace.isDeleted()) {
            throw new AccessDeniedException("Workspace has been deleted");
        }
        if (workspace.getVisibility() == com.knowledgenetwork.domain.model.Visibility.PUBLIC) {
            return;
        }
        if (userId != null && isOwner(workspace, userId)) {
            return;
        }
        if (userId == null) {
            throw new AccessDeniedException("Authentication required to access this workspace");
        }
        User user = getUser(userId);
        boolean isMember = workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user);
        if (!isMember) {
            throw new AccessDeniedException("User does not have access to this workspace");
        }
    }

    public void validateWriteAccess(Workspace workspace, UUID userId) {
        if (workspace.isDeleted()) {
            throw new AccessDeniedException("Workspace has been deleted");
        }
        if (isOwner(workspace, userId)) {
            return;
        }
        User user = getUser(userId);
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
        if (memberOpt.isEmpty()) {
            throw new AccessDeniedException("User does not have access to this workspace");
        }
        WorkspaceRole role = memberOpt.get().getRole();
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.EDITOR) {
            throw new AccessDeniedException("User does not have permission to modify this workspace");
        }
    }

    public void validateOwnerAccess(Workspace workspace, UUID userId) {
        if (workspace.isDeleted()) {
            throw new AccessDeniedException("Workspace has been deleted");
        }
        if (isOwner(workspace, userId)) {
            return;
        }
        User user = getUser(userId);
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
        if (memberOpt.isEmpty() || memberOpt.get().getRole() != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("User must be an owner of this workspace to perform this action");
        }
    }

    private boolean isOwner(Workspace workspace, UUID userId) {
        return workspace.getOwner() != null && workspace.getOwner().getId().equals(userId);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + userId));
    }
}
