package com.tilingroofing.api.mapper;

import com.tilingroofing.api.dto.response.UserInfo;
import com.tilingroofing.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for User entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Converts a User entity to UserInfo DTO.
     */
    @Named("userToUserInfo")
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getName() : null)")
    UserInfo toUserInfo(User user);
}

