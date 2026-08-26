package com.knowledgenetwork.common.mapper;

import com.knowledgenetwork.domain.model.Edge;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface EdgeMapper {

    @Mapping(target = "graphId", source = "workspace.id")
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "edgeTypeId", source = "edgeType.id")
    @Mapping(target = "edgeTypeName", source = "edgeType.name")
    @Mapping(target = "relationshipType", source = "edgeType.name")
    @Mapping(target = "directed", source = "edgeType.directed")
    @Mapping(target = "sourceNodeId", source = "sourceNode.id")
    @Mapping(target = "sourceNodeLabel", source = "sourceNode.label")
    @Mapping(target = "sourceNodeTitle", source = "sourceNode.label")
    @Mapping(target = "targetNodeId", source = "targetNode.id")
    @Mapping(target = "targetNodeLabel", source = "targetNode.label")
    @Mapping(target = "targetNodeTitle", source = "targetNode.label")
    @Mapping(target = "label", expression = "java(getEdgeLabel(edge))")
    @Mapping(target = "description", expression = "java(getEdgeDescription(edge))")
    @Mapping(target = "deleted", source = "deleted")
    EdgeResponse toEdgeResponse(Edge edge);

    default String getEdgeLabel(Edge edge) {
        if (edge != null && edge.getAttributes() != null && edge.getAttributes().containsKey("label")) {
            Object val = edge.getAttributes().get("label");
            return val != null ? val.toString() : null;
        }
        return null;
    }

    default String getEdgeDescription(Edge edge) {
        if (edge != null && edge.getAttributes() != null && edge.getAttributes().containsKey("description")) {
            Object val = edge.getAttributes().get("description");
            return val != null ? val.toString() : null;
        }
        return null;
    }
}
