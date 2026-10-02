package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.domain.payload.response.PublicConceptExploreResponse;
import com.knowledgenetwork.domain.payload.response.PublicCreatorProfileResponse;
import com.knowledgenetwork.service.PublicDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PublicDiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicDiscoveryService publicDiscoveryService;

    @Test
    void exploreConcepts_ShouldReturnAggregatedConcepts() throws Exception {
        PublicConceptExploreResponse response = new PublicConceptExploreResponse(
                "Recursion",
                2L,
                List.of(
                        new PublicConceptExploreResponse.ConceptOccurrence(
                                UUID.randomUUID(), "Data Structures",
                                com.knowledgenetwork.domain.enums.LicenseType.CC_BY_4_0,
                                "Alice Dev", "alice",
                                UUID.randomUUID(), "Concept", 10.0, 20.0
                        )
                ),
                List.of(new PublicConceptExploreResponse.RelatedConcept("Base Case", 3L))
        );

        when(publicDiscoveryService.exploreConcepts(eq("Recursion"), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/public/concepts/explore")
                        .param("query", "Recursion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conceptLabel").value("Recursion"))
                .andExpect(jsonPath("$.data.totalPublicNetworks").value(2))
                .andExpect(jsonPath("$.data.occurrences[0].networkTitle").value("Data Structures"))
                .andExpect(jsonPath("$.data.relatedConcepts[0].label").value("Base Case"));
    }

    @Test
    void discoverPublicNetworks_ShouldReturnPublicNetworks() throws Exception {
        GraphResponse graphResponse = new GraphResponse();
        graphResponse.setId(UUID.randomUUID());
        graphResponse.setTitle("Public Graph");
        graphResponse.setPublished(true);
        graphResponse.setLicenseType(com.knowledgenetwork.domain.enums.LicenseType.CC_BY_4_0);

        PageResponse<GraphResponse> pageResponse = PageResponse.<GraphResponse>builder()
                .content(List.of(graphResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(publicDiscoveryService.discoverPublicNetworks(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/public/networks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Public Graph"))
                .andExpect(jsonPath("$.data.content[0].published").value(true));
    }

    @Test
    void getPublicCreatorProfile_ShouldReturnCreatorData() throws Exception {
        PublicCreatorProfileResponse profileResponse = new PublicCreatorProfileResponse(
                UUID.randomUUID(),
                "alice",
                "Alice Dev",
                "Graph researcher",
                null,
                1L,
                List.of(),
                List.of("Algorithms", "Graph Theory")
        );

        when(publicDiscoveryService.getPublicCreatorProfile("alice"))
                .thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/public/creators/alice")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.fullName").value("Alice Dev"))
                .andExpect(jsonPath("$.data.topConcepts[0]").value("Algorithms"));
    }

    @Test
    void getRelatedPublicNetworks_ShouldReturnRelatedNetworks() throws Exception {
        UUID networkId = UUID.randomUUID();
        com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse relatedResp =
                new com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse(
                        UUID.randomUUID(), "Related Net", "Desc", "Alice Dev", "alice",
                        com.knowledgenetwork.domain.enums.LicenseType.CC_BY_4_0, 4L, 2L,
                        "Shared Concept Labels", java.time.Instant.now()
                );

        when(publicDiscoveryService.getRelatedPublicNetworks(eq(networkId), anyInt()))
                .thenReturn(List.of(relatedResp));

        mockMvc.perform(get("/api/v1/public/networks/" + networkId + "/related")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Related Net"))
                .andExpect(jsonPath("$.data[0].relationReason").value("Shared Concept Labels"));
    }
}

