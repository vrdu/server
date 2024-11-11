package com.example.server.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoundingRectDTO {
    private Double x1;
    private Double x2;
    private Double y1;
    private Double y2;
    private Double width;
    private Double height;
    private int pageNumber;
}
