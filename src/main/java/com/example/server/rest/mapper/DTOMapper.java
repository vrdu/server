package com.example.server.rest.mapper;

import com.example.server.entity.*;
import com.example.server.rest.dto.*;
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


    @Mapping(source = "projectName", target = "projectName")
    Project convertProjectPostDTOToEntity(ProjectPostDTO projectPostDTO);
    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "f1", target = "f1")
    @Mapping(source = "anls", target = "anls")
    ProjectGetDTO convertEntityProjectGetDTO(Project project);

    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "documentName", target = "documentName")
    @Mapping(source = "owner", target = "owner")
    Document convertDocumentPostDTOToEntity(DocumentPostDTO documentPostDTO);


    @Mapping(source= "id", target= "id")
    @Mapping(source = "labelName", target = "labelName")
    @Mapping(source = "labelDescription", target = "labelDescription")
    @Mapping(source = "index", target = "index")
    Label convertLabelPostDTOToEntity(LabelPostDTO labelPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "index", target = "index")
    @Mapping(source = "labelFamilyName", target = "labelFamilyName")
    @Mapping(source = "labelFamilyDescription", target = "labelFamilyDescription")
    @Mapping(source = "labels", target = "labels")
    LabelFamily convertLabelFamilyPostDTOToEntity(LabelFamilyPostDTO labelFamilyPostDTO);


}
