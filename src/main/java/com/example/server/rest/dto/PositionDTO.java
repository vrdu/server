package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class PositionDTO {
    private BoundingRectDTO boundingRect;
    private List<RectDTO> rects;
    private Integer pageNumber;
}

