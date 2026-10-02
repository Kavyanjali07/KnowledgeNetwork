package com.knowledgenetwork.controller;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.domain.payload.response.PublicConceptExploreResponse;
import com.knowledgenetwork.domain.payload.response.PublicCreatorProfileResponse;
import com.knowledgenetwork.service.PublicDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
public class PublicDiscoveryController {

    private final PublicDiscoveryService publicDiscoveryService;

    public PublicDiscoveryController(PublicDiscoveryService publicDiscoveryService) {
        this.publicDiscoveryService = publicDiscoveryService;
    }

    /**
     * Aggregated concept search and co-occurring concept discovery across public networks.
     */
    @GetMapping("/concepts/explore")
    public ResponseEntity<ApiResponse<PublicConceptExploreResponse>> exploreConcepts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PublicConceptExploreResponse response = publicDiscoveryService.exploreConcepts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Public concept exploration data retrieved", response));
    }

    /**
     * Discovery and listing of public knowledge networks with filters.
     */
    @GetMapping("/networks")
    public ResponseEntity<ApiResponse<PageResponse<GraphResponse>>> discoverPublicNetworks(
            @RequestParam(value = "concept", required = false) String concept,
            @RequestParam(value = "creator", required = false) String creator,
            @RequestParam(value = "license", required = false) String license,
            @RequestParam(value = "allowDerivatives", required = false) Boolean allowDerivatives,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResponse<GraphResponse> response = publicDiscoveryService.discoverPublicNetworks(
                concept, creator, license, allowDerivatives, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("Public knowledge networks retrieved", response));
    }

    /**
     * Creator public knowledge profile.
     */
    @GetMapping("/creators/{username}")
    public ResponseEntity<ApiResponse<PublicCreatorProfileResponse>> getPublicCreatorProfile(
            @PathVariable("username") String username) {
        PublicCreatorProfileResponse response = publicDiscoveryService.getPublicCreatorProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Creator public profile retrieved", response));
    }

    /**
     * Discovers related public knowledge networks for a given network ID.
     */
    @GetMapping("/networks/{networkId}/related")
    public ResponseEntity<ApiResponse<java.util.List<com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse>>> getRelatedPublicNetworks(
            @PathVariable("networkId") java.util.UUID networkId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        java.util.List<com.knowledgenetwork.domain.payload.response.PublicRelatedNetworkResponse> response =
                publicDiscoveryService.getRelatedPublicNetworks(networkId, limit);
        return ResponseEntity.ok(ApiResponse.success("Related public networks retrieved", response));
    }
}

