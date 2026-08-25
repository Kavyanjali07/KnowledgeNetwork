package com.knowledgenetwork.common.mapper;

import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.response.WorkspaceMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface WorkspaceMemberMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userName", expression = "java(member.getUser() != null ? member.getUser().getFirstName() + \" \" + member.getUser().getLastName() : null)")
    WorkspaceMemberResponse toWorkspaceMemberResponse(WorkspaceMember member);
}
