package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.model.SocialComment;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.request.SocialCommentRequest;
import com.knowledgenetwork.service.SocialService;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SocialService socialService;

    private Workspace workspace;
    private User author;
    private SocialPost post;

    @BeforeEach
    void setUp() {
        author = TestFixtures.createUser("author@example.com", "Author", "User");
        author.setId(UUID.randomUUID());

        workspace = TestFixtures.createWorkspace("Test Workspace", author);
        workspace.setId(UUID.randomUUID());

        post = TestFixtures.createSocialPost(workspace, author, "Controller test post");
        post.setId(UUID.randomUUID());
    }

    @Test
    void createPostShouldReturn201() throws Exception {
        when(socialService.createPost(eq(workspace.getId()), eq("Controller test post"), eq("GRAPH"), eq(workspace.getId())))
                .thenReturn(post);

        mockMvc.perform(post("/api/v1/social/posts")
                        .param("workspaceId", workspace.getId().toString())
                        .param("content", "Controller test post")
                        .param("entityType", "GRAPH")
                        .param("entityId", workspace.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Controller test post"));
    }

    @Test
    void getPostsShouldReturn200() throws Exception {
        when(socialService.getPosts(eq(workspace.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        mockMvc.perform(get("/api/v1/social/posts/" + workspace.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("Controller test post"));
    }

    @Test
    void likePostShouldReturn200() throws Exception {
        doNothing().when(socialService).likePost(post.getId());

        mockMvc.perform(post("/api/v1/social/posts/" + post.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unlikePostShouldReturn200() throws Exception {
        doNothing().when(socialService).unlikePost(post.getId());

        mockMvc.perform(delete("/api/v1/social/posts/" + post.getId() + "/unlike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void favoritePostShouldReturn200() throws Exception {
        doNothing().when(socialService).favoritePost(post.getId());

        mockMvc.perform(post("/api/v1/social/posts/" + post.getId() + "/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unfavoritePostShouldReturn200() throws Exception {
        doNothing().when(socialService).unfavoritePost(post.getId());

        mockMvc.perform(delete("/api/v1/social/posts/" + post.getId() + "/unfavorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addCommentShouldReturn201() throws Exception {
        SocialCommentRequest request = new SocialCommentRequest("Controller comment", null);
        SocialComment comment = new SocialComment(workspace, post, author, "Controller comment");
        comment.setId(UUID.randomUUID());

        when(socialService.addComment(eq(post.getId()), eq("Controller comment"), eq(null)))
                .thenReturn(comment);

        mockMvc.perform(post("/api/v1/social/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Controller comment"));
    }

    @Test
    void getCommentsShouldReturn200() throws Exception {
        SocialComment comment = new SocialComment(workspace, post, author, "Comment 1");
        comment.setId(UUID.randomUUID());

        when(socialService.getComments(eq(post.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        mockMvc.perform(get("/api/v1/social/posts/" + post.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("Comment 1"));
    }

    @Test
    void addTagShouldReturn200() throws Exception {
        doNothing().when(socialService).addTag(eq(post.getId()), eq("ai"));

        mockMvc.perform(post("/api/v1/social/posts/" + post.getId() + "/tags")
                        .param("tag", "ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void followUserShouldReturn200() throws Exception {
        UUID followeeId = UUID.randomUUID();
        doNothing().when(socialService).followUser(followeeId);

        mockMvc.perform(post("/api/v1/social/follow")
                        .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unfollowUserShouldReturn200() throws Exception {
        UUID followeeId = UUID.randomUUID();
        doNothing().when(socialService).unfollowUser(followeeId);

        mockMvc.perform(delete("/api/v1/social/follow")
                        .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void isFollowingShouldReturn200() throws Exception {
        UUID followeeId = UUID.randomUUID();
        when(socialService.isFollowing(followeeId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/social/follow/status")
                        .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void getSummaryShouldReturn200() throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("postId", post.getId());
        map.put("likes", 5);
        map.put("favorites", 2);
        map.put("comments", 3);
        map.put("tags", List.of("graph", "ai"));

        when(socialService.getSocialSummary(post.getId())).thenReturn(map);

        mockMvc.perform(get("/api/v1/social/posts/" + post.getId() + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.likes").value(5))
                .andExpect(jsonPath("$.data.favorites").value(2))
                .andExpect(jsonPath("$.data.comments").value(3))
                .andExpect(jsonPath("$.data.tags[0]").value("graph"));
    }
}
