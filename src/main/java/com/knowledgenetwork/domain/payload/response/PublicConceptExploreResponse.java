package com.knowledgenetwork.domain.payload.response;

import java.util.List;
import java.util.UUID;

public class PublicConceptExploreResponse {

    private String conceptLabel;
    private long totalPublicNetworks;
    private List<ConceptOccurrence> occurrences;
    private List<RelatedConcept> relatedConcepts;

    public PublicConceptExploreResponse() {
    }

    public PublicConceptExploreResponse(String conceptLabel, long totalPublicNetworks,
                                        List<ConceptOccurrence> occurrences, List<RelatedConcept> relatedConcepts) {
        this.conceptLabel = conceptLabel;
        this.totalPublicNetworks = totalPublicNetworks;
        this.occurrences = occurrences;
        this.relatedConcepts = relatedConcepts;
    }

    public String getConceptLabel() {
        return conceptLabel;
    }

    public void setConceptLabel(String conceptLabel) {
        this.conceptLabel = conceptLabel;
    }

    public long getTotalPublicNetworks() {
        return totalPublicNetworks;
    }

    public void setTotalPublicNetworks(long totalPublicNetworks) {
        this.totalPublicNetworks = totalPublicNetworks;
    }

    public List<ConceptOccurrence> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<ConceptOccurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public List<RelatedConcept> getRelatedConcepts() {
        return relatedConcepts;
    }

    public void setRelatedConcepts(List<RelatedConcept> relatedConcepts) {
        this.relatedConcepts = relatedConcepts;
    }

    public static class ConceptOccurrence {
        private UUID networkId;
        private String networkTitle;
        private com.knowledgenetwork.domain.enums.LicenseType licenseType;
        private String creatorName;
        private String creatorUsername;
        private UUID nodeId;
        private String nodeType;
        private Double positionX;
        private Double positionY;

        public ConceptOccurrence() {
        }

        public ConceptOccurrence(UUID networkId, String networkTitle,
                                 com.knowledgenetwork.domain.enums.LicenseType licenseType,
                                 String creatorName, String creatorUsername,
                                 UUID nodeId, String nodeType, Double positionX, Double positionY) {
            this.networkId = networkId;
            this.networkTitle = networkTitle;
            this.licenseType = licenseType;
            this.creatorName = creatorName;
            this.creatorUsername = creatorUsername;
            this.nodeId = nodeId;
            this.nodeType = nodeType;
            this.positionX = positionX;
            this.positionY = positionY;
        }

        public UUID getNetworkId() { return networkId; }
        public void setNetworkId(UUID networkId) { this.networkId = networkId; }
        public String getNetworkTitle() { return networkTitle; }
        public void setNetworkTitle(String networkTitle) { this.networkTitle = networkTitle; }
        public com.knowledgenetwork.domain.enums.LicenseType getLicenseType() { return licenseType; }
        public void setLicenseType(com.knowledgenetwork.domain.enums.LicenseType licenseType) { this.licenseType = licenseType; }
        public String getCreatorName() { return creatorName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
        public String getCreatorUsername() { return creatorUsername; }
        public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }
        public UUID getNodeId() { return nodeId; }
        public void setNodeId(UUID nodeId) { this.nodeId = nodeId; }
        public String getNodeType() { return nodeType; }
        public void setNodeType(String nodeType) { this.nodeType = nodeType; }
        public Double getPositionX() { return positionX; }
        public void setPositionX(Double positionX) { this.positionX = positionX; }
        public Double getPositionY() { return positionY; }
        public void setPositionY(Double positionY) { this.positionY = positionY; }
    }

    public static class RelatedConcept {
        private String label;
        private long coOccurrenceCount;

        public RelatedConcept() {
        }

        public RelatedConcept(String label, long coOccurrenceCount) {
            this.label = label;
            this.coOccurrenceCount = coOccurrenceCount;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public long getCoOccurrenceCount() { return coOccurrenceCount; }
        public void setCoOccurrenceCount(long coOccurrenceCount) { this.coOccurrenceCount = coOccurrenceCount; }
    }
}
