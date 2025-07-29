package uk.ac.ed.eci.libCZI.bitmaps;

import uk.ac.ed.eci.libCZI.IntRect;

public class Roi {
    private int x;
    private int y;
    private int w;
    private int h;
    private final IntRect boundingBox;


    public Roi(IntRect boundingBox) {
        this.boundingBox = boundingBox;
        this.x = 0;
        this.y = 0;
        this.w = 0;
        this.h = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }
    
    public Roi setX(int x) {
        this.x = x;
        return this;
    }

    public Roi setY(int y) {
        this.y = y;
        return this;
    }

    public Roi setWidth(int w) {
        this.w = w;
        return this;
    }

    public Roi setHeight(int h) {
        this.h = h;
        return this;
    }

    public static Roi setBoundingBox(IntRect boundingBox) {
        return new Roi(boundingBox);
    }


    public IntRect toIntRect() {
        return new IntRect(
            x + boundingBox.x(), 
            y + boundingBox.y(), 
            w, h);
    }
}