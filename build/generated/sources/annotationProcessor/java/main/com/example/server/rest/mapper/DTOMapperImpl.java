package com.example.server.rest.mapper;

import com.example.server.entity.Project;
import com.example.server.entity.User;
import com.example.server.rest.dto.ProjectGetDTO;
import com.example.server.rest.dto.ProjectPostDTO;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-24T15:58:34+0200",
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

        return userGetDTO;
    }

    @Override
    public Project convertProjectPostDTOToEntity(ProjectPostDTO projectPostDTO) {
        if ( projectPostDTO == null ) {
            return null;
        }

        Project project = new Project();

        project.setProjectName( projectPostDTO.getProjectName() );

        return project;
    }

    @Override
    public ProjectGetDTO convertEntityProjectGetDTO(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectGetDTO projectGetDTO = new ProjectGetDTO();

        projectGetDTO.setProjectName( project.getProjectName() );
        projectGetDTO.setF1( project.getF1() );
        projectGetDTO.setAnls( project.getAnls() );

        return projectGetDTO;
    }
}
