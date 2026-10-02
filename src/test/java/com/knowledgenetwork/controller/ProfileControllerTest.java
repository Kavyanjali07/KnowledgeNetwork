package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.payload.request.ProfileUpdateRequest;
import com.knowledgenetwork.domain.payload.response.ProfileResponse;
import com.knowledgenetwork.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @Test
    void getCurrentProfileShouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        ProfileResponse response = new ProfileResponse();
        response.setId(userId);
        response.setEmail("me@example.com");
        response.setFirstName("Current");
        response.setLastName("User");

        when(profileService.getCurrentProfile()).thenReturn(response);

        mockMvc.perform(get("/api/v1/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Current"));
    }

    @Test
    void updateCurrentProfileShouldReturn200() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setBio("New bio description");
        request.setStatus("away");

        ProfileResponse response = new ProfileResponse();
        response.setId(UUID.randomUUID());
        response.setFirstName("Updated");
        response.setLastName("Name");
        response.setBio("New bio description");
        response.setStatus("away");

        when(profileService.updateCurrentProfile(any(ProfileUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.status").value("away"));
    }

    @Test
    void updateCurrentProfileValidationFailureShouldReturn422() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName(""); // Blank first name violates @NotBlank
        request.setLastName("LastName");

        mockMvc.perform(put("/api/v1/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void getPublicProfileShouldReturn200() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        ProfileResponse response = new ProfileResponse();
        response.setId(targetUserId);
        response.setEmail("public@example.com");
        response.setFirstName("Public");
        response.setLastName("Profile");

        when(profileService.getProfile(eq(targetUserId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/profile/users/" + targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Public"));
    }
}
