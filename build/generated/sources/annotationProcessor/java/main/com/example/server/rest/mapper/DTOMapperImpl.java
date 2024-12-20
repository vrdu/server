package com.example.server.rest.mapper;

import com.example.server.entity.Document;
import com.example.server.entity.Extraction;
import com.example.server.entity.Label;
import com.example.server.entity.LabelFamily;
import com.example.server.entity.Project;
import com.example.server.entity.User;
import com.example.server.rest.dto.CustomFileDTO;
import com.example.server.rest.dto.DocumentAndReportDTO;
import com.example.server.rest.dto.DocumentDeleteDTO;
import com.example.server.rest.dto.DocumentGetDTO;
import com.example.server.rest.dto.DocumentPostDTO;
import com.example.server.rest.dto.ExtractionCorrectionGetDTO;
import com.example.server.rest.dto.ExtractionCorrectionPostDTO;
import com.example.server.rest.dto.ExtractionGetDTO;
import com.example.server.rest.dto.ExtractionPostDTO;
import com.example.server.rest.dto.LabelFamilyGetDTO;
import com.example.server.rest.dto.LabelFamilyNameGetDTO;
import com.example.server.rest.dto.LabelFamilyPostDTO;
import com.example.server.rest.dto.LabelFamilyUpdatePostDTO;
import com.example.server.rest.dto.LabelGetDTO;
import com.example.server.rest.dto.LabelNameGetDTO;
import com.example.server.rest.dto.LabelPostDTO;
import com.example.server.rest.dto.ProjectGetDTO;
import com.example.server.rest.dto.ProjectPostDTO;
import com.example.server.rest.dto.ProjectUpdatePostDTO;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-20T16:38:44+0100",
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
    public Project convertProjectUpdatePostDTOToEntity(ProjectUpdatePostDTO projectUpdatePostDTO) {
        if ( projectUpdatePostDTO == null ) {
            return null;
        }

        Project project = new Project();

        project.setToImport( projectUpdatePostDTO.isToImport() );
        project.setProjectName( projectUpdatePostDTO.getProjectName() );

        return project;
    }

    @Override
    public ProjectGetDTO convertEntityProjectGetDTO(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectGetDTO projectGetDTO = new ProjectGetDTO();

        projectGetDTO.setProjectName( project.getProjectName() );
        if ( project.getF1() != null ) {
            projectGetDTO.setF1( project.getF1().intValue() );
        }
        if ( project.getAnls() != null ) {
            projectGetDTO.setAnls( project.getAnls().intValue() );
        }

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
    public Document convertDocumentDeleteDTOToEntity(DocumentDeleteDTO documentDeleteDTO) {
        if ( documentDeleteDTO == null ) {
            return null;
        }

        Document document = new Document();

        document.setProjectName( documentDeleteDTO.getProjectName() );
        document.setDocumentName( documentDeleteDTO.getDocumentName() );
        document.setOwner( documentDeleteDTO.getOwner() );

        return document;
    }

    @Override
    public DocumentGetDTO convertEntityToDocumentGetDTO(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentGetDTO documentGetDTO = new DocumentGetDTO();

        documentGetDTO.setName( document.getDocumentName() );

        return documentGetDTO;
    }

    @Override
    public Label convertLabelPostDTOToEntity(LabelPostDTO labelPostDTO) {
        if ( labelPostDTO == null ) {
            return null;
        }

        Label label = new Label();

        label.setLabelName( labelPostDTO.getLabelName() );
        label.setOldLabelName( labelPostDTO.getOldLabelName() );
        label.setRegister( labelPostDTO.isRegister() );
        label.setFamilyName( labelPostDTO.getFamilyName() );
        label.setLabelDescription( labelPostDTO.getLabelDescription() );
        label.setIndex( labelPostDTO.getIndex() );

        return label;
    }

    @Override
    public LabelNameGetDTO convertEntityToLabelGetDTOTo(Label label) {
        if ( label == null ) {
            return null;
        }

        LabelNameGetDTO labelNameGetDTO = new LabelNameGetDTO();

        labelNameGetDTO.setLabelName( label.getLabelName() );

        return labelNameGetDTO;
    }

    @Override
    public LabelGetDTO convertEntityToLabelGetDTO(Label label) {
        if ( label == null ) {
            return null;
        }

        LabelGetDTO labelGetDTO = new LabelGetDTO();

        labelGetDTO.setLabelName( label.getLabelName() );
        labelGetDTO.setOldLabelName( label.getOldLabelName() );
        if ( label.getRegister() != null ) {
            labelGetDTO.setRegister( label.getRegister() );
        }
        labelGetDTO.setFamilyName( label.getFamilyName() );
        labelGetDTO.setLabelDescription( label.getLabelDescription() );
        labelGetDTO.setIndex( label.getIndex() );

        return labelGetDTO;
    }

    @Override
    public LabelFamily convertLabelFamilyPostDTOToEntity(LabelFamilyPostDTO labelFamilyPostDTO) {
        if ( labelFamilyPostDTO == null ) {
            return null;
        }

        LabelFamily labelFamily = new LabelFamily();

        labelFamily.setIndex( labelFamilyPostDTO.getIndex() );
        labelFamily.setLabelFamilyName( labelFamilyPostDTO.getLabelFamilyName() );
        labelFamily.setOldLabelFamilyName( labelFamilyPostDTO.getOldLabelFamilyName() );
        labelFamily.setRegister( labelFamilyPostDTO.isRegister() );
        labelFamily.setLabelFamilyDescription( labelFamilyPostDTO.getLabelFamilyDescription() );
        labelFamily.setLabels( labelPostDTOListToLabelList( labelFamilyPostDTO.getLabels() ) );

        return labelFamily;
    }

    @Override
    public LabelFamily convertLabelFamilyUpdatePostDTOToEntity(LabelFamilyUpdatePostDTO labelFamilyUpdatePostDTO) {
        if ( labelFamilyUpdatePostDTO == null ) {
            return null;
        }

        LabelFamily labelFamily = new LabelFamily();

        labelFamily.setLabelFamilyName( labelFamilyUpdatePostDTO.getLabelFamilyName() );
        labelFamily.setProjectName( labelFamilyUpdatePostDTO.getProjectName() );
        labelFamily.setToImport( labelFamilyUpdatePostDTO.isToImport() );

        return labelFamily;
    }

    @Override
    public LabelFamilyGetDTO convertEntityToLabelFamilyGetDTO(LabelFamily labelFamily) {
        if ( labelFamily == null ) {
            return null;
        }

        LabelFamilyGetDTO labelFamilyGetDTO = new LabelFamilyGetDTO();

        labelFamilyGetDTO.setIndex( labelFamily.getIndex() );
        labelFamilyGetDTO.setLabelFamilyName( labelFamily.getLabelFamilyName() );
        labelFamilyGetDTO.setOldLabelFamilyName( labelFamily.getOldLabelFamilyName() );
        if ( labelFamily.getRegister() != null ) {
            labelFamilyGetDTO.setRegister( labelFamily.getRegister() );
        }
        labelFamilyGetDTO.setLabelFamilyDescription( labelFamily.getLabelFamilyDescription() );
        labelFamilyGetDTO.setLabels( labelListToLabelGetDTOList( labelFamily.getLabels() ) );

        return labelFamilyGetDTO;
    }

    @Override
    public LabelFamilyNameGetDTO convertEntityToLabelFamilyNameGetDTO(LabelFamily labelFamily) {
        if ( labelFamily == null ) {
            return null;
        }

        LabelFamilyNameGetDTO labelFamilyNameGetDTO = new LabelFamilyNameGetDTO();

        labelFamilyNameGetDTO.setLabelFamilyName( labelFamily.getLabelFamilyName() );

        return labelFamilyNameGetDTO;
    }

    @Override
    public ExtractionGetDTO convertEntityToExtractionGetDTO(Extraction extraction) {
        if ( extraction == null ) {
            return null;
        }

        ExtractionGetDTO extractionGetDTO = new ExtractionGetDTO();

        extractionGetDTO.setName( extraction.getExtractionName() );

        return extractionGetDTO;
    }

    @Override
    public Extraction convertExtractionPostDTOToEntity(ExtractionPostDTO extractionPostDTO) {
        if ( extractionPostDTO == null ) {
            return null;
        }

        Extraction extraction = new Extraction();

        extraction.setExtractionName( extractionPostDTO.getName() );
        extraction.setId( (long) extractionPostDTO.getId() );
        extraction.setProjectName( extractionPostDTO.getProjectName() );

        return extraction;
    }

    @Override
    public Document convertCustomFileDTOToEntity(CustomFileDTO customFileDTO) {
        if ( customFileDTO == null ) {
            return null;
        }

        Document document = new Document();

        document.setDocumentName( customFileDTO.getName() );
        if ( customFileDTO.getId() != null ) {
            document.setId( Long.parseLong( customFileDTO.getId() ) );
        }
        document.setExtractionResult( customFileDTO.getExtractionResult() );

        return document;
    }

    @Override
    public DocumentAndReportDTO convertEntityToDocumentAndReportDTO(Extraction extraction) {
        if ( extraction == null ) {
            return null;
        }

        DocumentAndReportDTO documentAndReportDTO = new DocumentAndReportDTO();

        documentAndReportDTO.setName( extraction.getExtractionName() );
        if ( extraction.getF1() != null ) {
            documentAndReportDTO.setF1( extraction.getF1().floatValue() );
        }
        documentAndReportDTO.setAnls( extraction.getAnls() );

        return documentAndReportDTO;
    }

    @Override
    public ExtractionCorrectionGetDTO convertEntityToExtractionCorrectionGetDTO(Document document) {
        if ( document == null ) {
            return null;
        }

        ExtractionCorrectionGetDTO extractionCorrectionGetDTO = new ExtractionCorrectionGetDTO();

        extractionCorrectionGetDTO.setExtractionResult( document.getExtractionResult() );

        return extractionCorrectionGetDTO;
    }

    @Override
    public Document convertExtractionCorrectionPostDTOToEntity(ExtractionCorrectionPostDTO extractionCorrectionPostDTO) {
        if ( extractionCorrectionPostDTO == null ) {
            return null;
        }

        Document document = new Document();

        document.setExtractionSolution( extractionCorrectionPostDTO.getExtractionSolution() );

        return document;
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

    protected List<LabelGetDTO> labelListToLabelGetDTOList(List<Label> list) {
        if ( list == null ) {
            return null;
        }

        List<LabelGetDTO> list1 = new ArrayList<LabelGetDTO>( list.size() );
        for ( Label label : list ) {
            list1.add( convertEntityToLabelGetDTO( label ) );
        }

        return list1;
    }
}
