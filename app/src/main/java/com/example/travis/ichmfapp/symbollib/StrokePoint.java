package com.example.travis.ichmfapp.symbollib;

import java.io.Serializable;

/**
 * Created by Travis on 7/8/2018.
 */

public class StrokePoint implements Cloneable, Serializable {

    /**
     * Defautl constructor
     * @param x X location.
     * @param y Y location.
     */
    public StrokePoint (double x, double y){
        X = x;
        Y = y;
    }

    public double X;
    public double Y;

    public String ToString()
    {
        return "Point [" + this.X + ", " + this.Y + "]";
    }

//    public Point ToPoint()
//    {
//        return new Point(this.X, this.Y);
//    }

    @Override
    public StrokePoint clone() throws CloneNotSupportedException{
        return (StrokePoint)super.clone();
    }

}

