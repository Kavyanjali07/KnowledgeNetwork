package com.knowledgenetwork.common.mapper;

import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserResponse toUserResponse(User user);
}
