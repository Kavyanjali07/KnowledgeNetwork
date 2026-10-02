package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.ProfileUpdateRequest;
import com.knowledgenetwork.domain.payload.response.ProfileResponse;
import com.knowledgenetwork.repository.EdgeRepository;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.SocialFollowRepository;
import com.knowledgenetwork.repository.SocialPostRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private EdgeRepository edgeRepository;
    @Mock
    private SocialPostRepository socialPostRepository;
    @Mock
    private SocialFollowRepository socialFollowRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createUser("profile@example.com", "John", "Doe");
        testUser.setId(UUID.randomUUID());
        testUser.setBio("Initial bio");
        testUser.setStatus("online");

        workspace = TestFixtures.createWorkspace("John's Graph", testUser);
        workspace.setId(UUID.randomUUID());

        UserPrincipal principal = UserPrincipal.fromUser(testUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getCurrentProfile_Success() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(workspaceRepository.findByOwner(testUser)).thenReturn(List.of(workspace));
        when(nodeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(10L);
        when(edgeRepository.countByWorkspaceAndIsDeletedFalse(workspace)).thenReturn(5L);
        when(socialPostRepository.countByAuthorAndIsDeletedFalse(testUser)).thenReturn(3L);
        when(socialFollowRepository.countByFollowee(testUser)).thenReturn(12L);
        when(socialFollowRepository.countByFollower(testUser)).thenReturn(8L);

        ProfileResponse profile = profileService.getCurrentProfile();

        assertNotNull(profile);
        assertEquals(testUser.getId(), profile.getId());
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("profile@example.com", profile.getEmail());
        assertEquals(1, profile.getGraphs().size());
        assertEquals(1, profile.getStatistics().getGraphsOwned());
        assertEquals(10, profile.getStatistics().getNodesCreated());
        assertEquals(5, profile.getStatistics().getEdgesAuthored());
        assertEquals(3, profile.getStatistics().getPosts());
        assertEquals(12, profile.getStatistics().getFollowers());
        assertEquals(8, profile.getStatistics().getFollowing());
    }

    @Test
    void getProfile_NotFound_ThrowsException() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfile(randomId));
    }

    @Test
    void updateCurrentProfile_Success() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("  Jane  ");
        request.setLastName("  Smith ");
        request.setBio(" Updated bio text ");
        request.setStatus("  BUSY ");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.findByOwner(testUser)).thenReturn(List.of());

        ProfileResponse updated = profileService.updateCurrentProfile(request);

        assertNotNull(updated);
        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        assertEquals("Updated bio text", updated.getBio());
        assertEquals("busy", updated.getStatus());
    }

    @Test
    void updateCurrentProfile_NormalizeStatus_BlankOrNullDefaultsToOnline() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setBio("Bio");
        request.setStatus("   ");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(workspaceRepository.findByOwner(testUser)).thenReturn(List.of());

        ProfileResponse updated = profileService.updateCurrentProfile(request);

        assertEquals("online", updated.getStatus());
    }
}
