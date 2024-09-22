package com.example.server.rest.mapper;

import com.example.server.entity.User;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-21T19:36:08+0200",
    comments = "version: 1.5.2.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.1.jar, environment: Java 17.0.4.1 (Eclipse Adoptium)"
)
public class DTOMapperImpl implements DTOMapper {

    @Override
    public User convertUserPostDTOToEntity(UserPostDTO userPostDTO) {
        if ( userPostDTO == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( userPostDTO.getUsername() );
        user.setPassword( userPostDTO.getPassword() );
        user.setEmail( userPostDTO.getEmail() );

        return user;
    }

    @Override
    public UserGetDTO convertEntityToUserGetDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserGetDTO userGetDTO = new UserGetDTO();

        userGetDTO.setUsername( user.getUsername() );
        userGetDTO.setToken( user.getToken() );

        return userGetDTO;
    }
}
