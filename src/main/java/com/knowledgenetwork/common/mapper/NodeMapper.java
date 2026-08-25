package com.knowledgenetwork.common.mapper;

import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface NodeMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "nodeTypeId", source = "nodeType.id")
    @Mapping(target = "nodeTypeName", source = "nodeType.name")
    @Mapping(target = "nodeTypeColor", source = "nodeType.colorCode")
    @Mapping(target = "nodeTypeIcon", source = "nodeType.icon")
    @Mapping(target = "deleted", source = "deleted")
    NodeResponse toNodeResponse(Node node);
}
