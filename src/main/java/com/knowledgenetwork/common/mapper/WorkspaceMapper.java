package com.knowledgenetwork.common.mapper;

import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface WorkspaceMapper {

    @Mapping(target = "title", source = "name")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "ownerName", expression = "java(workspace.getOwner() != null ? workspace.getOwner().getFirstName() + \" \" + workspace.getOwner().getLastName() : null)")
    WorkspaceResponse toWorkspaceResponse(Workspace workspace);
}
