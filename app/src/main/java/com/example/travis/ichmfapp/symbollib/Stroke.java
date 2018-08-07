package com.example.travis.ichmfapp.symbollib;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Travis on 7/8/2018.
 */

public class Stroke implements Cloneable, Serializable {

    /**
     * List of points composed in stroke.
     */
    protected StrokePointList _Points;
    protected int symbolID;

    /**
     * Default construdctor
     * */
    public Stroke() {
        _Points = new StrokePointList();
    }

    public int getTotalStrokePoints() {
        return _Points.size();
    }

    public StrokePointList getStrokePoints() {
        return _Points;
    }

    public void setStrokePoints(StrokePointList value) {
        _Points = value;
    }

    public void addStrokePoint(StrokePoint p) {
        _Points.add(p);
    }

    public StrokePoint getStrokePoint(int index) {
        return (StrokePoint) _Points.get(index);
    }

    public void setStrokePoint(StrokePoint newP, int index) {
        _Points.set(index, newP);

    }

    public void removeStrokePoint(int index) {
        _Points.remove(index);
    }

    public String ToString() {
        return "Stroke [" + this.getTotalStrokePoints() + "]";
    }

    public int getSymbolID() {

        return symbolID;
    }

    public void setSymbolID(int ID) {
        symbolID = ID;
    }

    public StrokePoint getCenter(){
        Rectangle rect = this.CalculateBoundingBox();
        int centerX = rect.x+rect.width/2;
        int centerY = rect.y+rect.height/2;
        StrokePoint sp = new StrokePoint(centerX,centerY );

        return sp;
    }

    public Rectangle CalculateBoundingBox() {
        double _boundingBoxWidth = -1;
        double _boundingBoxHeight = -1;
        double leftMostX = Integer.MAX_VALUE;
        double rightMostX = 0;
        double topMostY = Integer.MAX_VALUE;
        double bottomMostY = 0;

        ///For each points
        for (int j = 0; j < getTotalStrokePoints(); j++) {

            if (getStrokePoint(j).X < leftMostX) {
                leftMostX = getStrokePoint(j).X;
            }
            if (getStrokePoint(j).X > rightMostX) {
                rightMostX = getStrokePoint(j).X;
            }
            if (getStrokePoint(j).Y < topMostY) {
                topMostY = getStrokePoint(j).Y;
            }
            if (getStrokePoint(j).Y > bottomMostY) {
                bottomMostY = getStrokePoint(j).Y;
            }
        }

        _boundingBoxWidth = rightMostX - leftMostX + 1;//Add 1 to meet the accuracy
        _boundingBoxHeight = bottomMostY - topMostY + 1;//Add 1 to Meet the accuracy

        Rectangle _boundingBox = new Rectangle((int)leftMostX, (int)topMostY,
                (int)_boundingBoxWidth, (int)_boundingBoxHeight);
        return _boundingBox;
    }


    @Override
    public Stroke clone() throws CloneNotSupportedException {
        return (Stroke) super.clone();
    }
}
