package com.example.server.rest.mapper;

import com.example.server.entity.*;
import com.example.server.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import javax.print.Doc;

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


    @Mapping(source = "toImport", target = "toImport")
    @Mapping(source = "projectName", target = "projectName")
    Project convertProjectUpdatePostDTOToEntity(ProjectUpdatePostDTO projectUpdatePostDTO);
    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "f1", target = "f1")
    @Mapping(source = "anls", target = "anls")
    ProjectGetDTO convertEntityProjectGetDTO(Project project);

    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "documentName", target = "documentName")
    @Mapping(source = "owner", target = "owner")
    Document convertDocumentPostDTOToEntity(DocumentPostDTO documentPostDTO);

    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "documentName", target = "documentName")
    @Mapping(source = "owner", target = "owner")
    Document convertDocumentDeleteDTOToEntity(DocumentDeleteDTO documentDeleteDTO);

    @Mapping(source = "documentName", target = "name")
    DocumentGetDTO convertEntityToDocumentGetDTO(Document document);



    @Mapping(source = "labelName", target = "labelName")
    @Mapping(source = "oldLabelName", target = "oldLabelName")
    @Mapping(source = "register", target = "register")
    @Mapping(source = "familyName", target = "familyName")
    @Mapping(source = "labelDescription", target = "labelDescription")
    @Mapping(source = "index", target = "index")
    Label convertLabelPostDTOToEntity(LabelPostDTO labelPostDTO);

    @Mapping(source = "labelName", target = "labelName")
    LabelNameGetDTO convertEntityToLabelGetDTOTo(Label label);

    @Mapping(source = "labelName", target = "labelName")
    @Mapping(source = "oldLabelName", target = "oldLabelName")
    @Mapping(source = "register", target = "register")
    @Mapping(source = "familyName", target = "familyName")
    @Mapping(source = "labelDescription", target = "labelDescription")
    @Mapping(source = "index", target = "index")
    LabelGetDTO convertEntityToLabelGetDTO(Label label);


    @Mapping(source = "index", target = "index")
    @Mapping(source = "labelFamilyName", target = "labelFamilyName")
    @Mapping(source = "oldLabelFamilyName", target = "oldLabelFamilyName")
    @Mapping(source = "register", target = "register")
    @Mapping(source = "labelFamilyDescription", target = "labelFamilyDescription")
    @Mapping(source = "labels", target = "labels")
    LabelFamily convertLabelFamilyPostDTOToEntity(LabelFamilyPostDTO labelFamilyPostDTO);


    @Mapping(source = "labelFamilyName", target = "labelFamilyName")
    @Mapping(source = "projectName", target = "projectName")
    @Mapping(source = "toImport", target = "toImport")
    LabelFamily convertLabelFamilyUpdatePostDTOToEntity(LabelFamilyUpdatePostDTO labelFamilyUpdatePostDTO);

    @Mapping(source = "index", target = "index")
    @Mapping(source = "labelFamilyName", target = "labelFamilyName")
    @Mapping(source = "oldLabelFamilyName", target = "oldLabelFamilyName")
    @Mapping(source = "register", target = "register")
    @Mapping(source = "labelFamilyDescription", target = "labelFamilyDescription")
    @Mapping(source = "labels", target = "labels")
    LabelFamilyGetDTO convertEntityToLabelFamilyGetDTO(LabelFamily labelFamily);


    @Mapping(source = "labelFamilyName", target = "labelFamilyName")
    LabelFamilyNameGetDTO convertEntityToLabelFamilyNameGetDTO(LabelFamily labelFamily);

    @Mapping(source = "extractionName", target = "name")
    ExtractionGetDTO convertEntityToExtractionGetDTO(Extraction extraction);

    @Mapping(source = "name", target = "extractionName")
    Extraction convertExtractionPostDTOToEntity(ExtractionPostDTO extractionPostDTO);

    @Mapping(source = "name", target = "documentName")
    Document convertCustomFileDTOToEntity(CustomFileDTO customFileDTO);

    @Mapping(source = "extractionName", target = "name")

    DocumentAndReportDTO convertEntityToDocumentAndReportDTO(Extraction extraction);

    ExtractionCorrectionGetDTO convertEntityToExtractionCorrectionGetDTO(Document document);
    Document convertExtractionCorrectionPostDTOToEntity(ExtractionCorrectionPostDTO extractionCorrectionPostDTO);
}
