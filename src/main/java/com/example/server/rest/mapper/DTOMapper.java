package com.example.server.rest.mapper;

import com.example.server.entity.User;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper
public interface DTOMapper {
    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "email", target = "email")
    User convertUserPostDTOToEntity(UserPostDTO userPostDTO);

    @Mapping(source = "username", target = "username")
    UserGetDTO convertEntityToUserGetDTO(User user);




}
