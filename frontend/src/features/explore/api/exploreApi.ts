import apiClient from "../../../lib/api-client";

export interface ConceptOccurrence {
  networkId: string;
  networkTitle: string;
  licenseType: string;
  creatorName: string;
  creatorUsername: string;
  nodeId: string;
  nodeType: string;
  positionX: number;
  positionY: number;
}

export interface RelatedConcept {
  label: string;
  coOccurrenceCount: number;
}

export interface PublicConceptExploreResponse {
  conceptLabel: string;
  totalPublicNetworks: number;
  occurrences: ConceptOccurrence[];
  relatedConcepts: RelatedConcept[];
}

export interface PublicNetworkItem {
  id: string;
  title: string;
  name: string;
  description: string;
  ownerId: string;
  ownerName: string;
  ownerEmail: string;
  licenseType: string;
  isPublished: boolean;
  publishedAt: string;
  nodeCount: number;
  edgeCount: number;
  derivativeCount: number;
  referenceCount: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PublicCreatorProfile {
  id: string;
  username: string;
  fullName: string;
  bio: string | null;
  avatarUrl: string | null;
  totalPublicNetworks: number;
  publicNetworks: PublicNetworkItem[];
  topConcepts: string[];
}

export interface PublicRelatedNetwork {
  id: string;
  title: string;
  description: string;
  ownerName: string;
  ownerUsername: string;
  licenseType: string;
  nodeCount: number;
  edgeCount: number;
  relationReason: string;
  publishedAt: string;
}

export const exploreApi = {
  exploreConcepts: async (query?: string, page = 0, size = 20) => {
    const res = await apiClient.get<{ success: boolean; data: PublicConceptExploreResponse }>("/public/concepts/explore", {
      params: { query, page, size }
    });
    return res.data.data;
  },

  discoverPublicNetworks: async (params?: {
    concept?: string;
    creator?: string;
    license?: string;
    allowDerivatives?: boolean;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<{ success: boolean; data: PageResponse<PublicNetworkItem> }>("/public/networks", {
      params
    });
    return res.data.data;
  },

  getPublicCreatorProfile: async (username: string) => {
    const res = await apiClient.get<{ success: boolean; data: PublicCreatorProfile }>("/public/creators/" + encodeURIComponent(username));
    return res.data.data;
  },

  getRelatedPublicNetworks: async (networkId: string, limit = 5) => {
    const res = await apiClient.get<{ success: boolean; data: PublicRelatedNetwork[] }>("/public/networks/" + networkId + "/related", {
      params: { limit }
    });
    return res.data.data;
  }
};

