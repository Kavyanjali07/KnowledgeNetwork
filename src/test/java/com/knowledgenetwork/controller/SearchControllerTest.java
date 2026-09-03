package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.response.SearchResultResponse;
import com.knowledgenetwork.service.GraphSearchService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GraphSearchService graphSearchService;

    @Test
    void searchShouldReturn200WithResults() throws Exception {
        SearchResultResponse graphItem = SearchResultResponse.fromGraph(
                UUID.randomUUID(), "Spring Boot Learning Path", "Learn Spring Boot", "PUBLIC", 5, 3, null, null);
        SearchResultResponse nodeItem = SearchResultResponse.fromNode(
                UUID.randomUUID(), "Spring Security", "Auth framework", UUID.randomUUID(), "Spring Boot Learning Path",
                "Concept", "#22D3EE", "shield", 100.0, 200.0, null, null);

        PageResponse<SearchResultResponse> pageResponse = PageResponse.<SearchResultResponse>builder()
                .content(List.of(graphItem, nodeItem))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(2)
                .totalPages(1)
                .isLast(true)
                .build();

        when(graphSearchService.unifiedSearch(eq("Spring"), eq("all"), eq(null), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/search?q=Spring&type=all&page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].resultType").value("GRAPH"))
                .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot Learning Path"))
                .andExpect(jsonPath("$.data.content[1].resultType").value("NODE"))
                .andExpect(jsonPath("$.data.content[1].title").value("Spring Security"));
    }

    @Test
    void searchWithEmptyQueryShouldReturn200WithEmptyPage() throws Exception {
        PageResponse<SearchResultResponse> pageResponse = PageResponse.<SearchResultResponse>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .isLast(true)
                .build();

        when(graphSearchService.unifiedSearch(eq(""), eq("all"), eq(null), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/search?q=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    void searchWithInvalidPageOrSizeHandledGracefully() throws Exception {
        PageResponse<SearchResultResponse> pageResponse = PageResponse.<SearchResultResponse>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .isLast(true)
                .build();

        when(graphSearchService.unifiedSearch(eq("test"), eq("all"), eq(null), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/search?q=test&page=-5&size=500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
