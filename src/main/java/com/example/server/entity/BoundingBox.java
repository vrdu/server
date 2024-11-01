package com.example.server.entity;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
@Embeddable
public class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;

    public BoundingBox(Rectangle rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
    }

    public BoundingBox() {

    }
}