package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HighlightDTO  {
    private String id;
    private AnnotationDTO annotation;
    private CommentDTO comment;
    private ContentDTO content;
    private PositionDTO position;
}


