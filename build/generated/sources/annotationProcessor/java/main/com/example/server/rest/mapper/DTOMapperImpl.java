package com.example.server.rest.mapper;

import com.example.server.entity.Document;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.entity.User;
import com.example.server.rest.dto.DocumentPostDTO;
import com.example.server.rest.dto.LabelFamilyPostDTO;
import com.example.server.rest.dto.LabelPostDTO;
import com.example.server.rest.dto.ProjectGetDTO;
import com.example.server.rest.dto.ProjectPostDTO;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-06T13:51:00+0200",
    comments = "version: 1.5.2.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.1.jar, environment: Java 17.0.6 (Amazon.com Inc.)"
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

    @Override
    public Document convertDocumentPostDTOToEntity(DocumentPostDTO documentPostDTO) {
        if ( documentPostDTO == null ) {
            return null;
        }

        Document document = new Document();

        document.setProjectName( documentPostDTO.getProjectName() );
        document.setDocumentName( documentPostDTO.getDocumentName() );
        document.setOwner( documentPostDTO.getOwner() );

        return document;
    }

    @Override
    public Label convertLabelPostDTOToEntity(LabelPostDTO labelPostDTO) {
        if ( labelPostDTO == null ) {
            return null;
        }

        Label label = new Label();

        label.setLabelName( labelPostDTO.getLabelName() );
        label.setLabelDescription( labelPostDTO.getLabelDescription() );
        label.setIndex( labelPostDTO.getIndex() );

        return label;
    }

    @Override
    public LabelFamily convertLabelFamilyPostDTOToEntity(LabelFamilyPostDTO labelFamilyPostDTO) {
        if ( labelFamilyPostDTO == null ) {
            return null;
        }

        LabelFamily labelFamily = new LabelFamily();

        labelFamily.setIndex( labelFamilyPostDTO.getIndex() );
        labelFamily.setLabelFamilyName( labelFamilyPostDTO.getLabelFamilyName() );
        labelFamily.setLabelFamilyDescription( labelFamilyPostDTO.getLabelFamilyDescription() );
        labelFamily.setLabels( labelPostDTOListToLabelList( labelFamilyPostDTO.getLabels() ) );

        return labelFamily;
    }

    protected List<Label> labelPostDTOListToLabelList(List<LabelPostDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Label> list1 = new ArrayList<Label>( list.size() );
        for ( LabelPostDTO labelPostDTO : list ) {
            list1.add( convertLabelPostDTOToEntity( labelPostDTO ) );
        }

        return list1;
    }
}
