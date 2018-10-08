package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 29/9/2018.
 */

public class Box {

    public int x;
    public int y;
    public int width;
    public int height;

    public Box (int X, int Y, int Width, int Height){

        x = X;
        y = Y;
        width = Width;
        height =Height;

    }

    public double getX(){return x;}
    public double getY(){return y;}
    public double getWidth(){return width;}
    public double getHeight(){return height;}
    public double getCenterX() {return ((width/2)+x);}
    public double getCenterY() {return ((height/2)-y);}


}
