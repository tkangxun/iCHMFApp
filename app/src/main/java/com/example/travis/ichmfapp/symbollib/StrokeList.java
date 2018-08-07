package com.example.travis.ichmfapp.symbollib;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Travis on 7/8/2018.
 */

public class StrokeList
        extends ArrayList<Stroke>
        implements Cloneable, Serializable{

    /**
     * Default constructor
     */
    public StrokeList() {
    }


    /**
     * To get the bounding box rectangle of current stroke list.
     * @return
     */
    public Rectangle getBoundingBox() {
        return CalculateBoundingBox();
    }

    /**
     * To get a subset of items from the lists starting from given
     * start index to end index. But extraction doesn't remove
     * the items from original list.
     * @param startIndex Start location of subset to extract.
     * @param endIndex End location of sub to extract.
     * @return New StrokeList containing the extract items.
     * @throws java.lang.CloneNotSupportedException
     */
    public StrokeList getRange(int startIndex, int endIndex)
            throws CloneNotSupportedException {
        StrokeList newSl = new StrokeList();
        for (int i = startIndex; i < endIndex; i++) {
            newSl.add(this.get(i).clone());
        }
        return newSl;
    }

    /**
     * To retrive last four items from the list.
     * @return A new stroke list with last 4 items from current list.
     * @throws java.lang.CloneNotSupportedException
     */
    public StrokeList GetLast4Strokes() throws CloneNotSupportedException {
        if (this.size() <= 4) {
            //StrokeList temp = new StrokeList();
            //temp.Add(this[this.Count - 1]);
            //temp.Add(this[this.Count - 1]);
            //temp.Add(this[this.Count - 1]);
            //temp.Add(this[this.Count - 1]);
            //return temp;
            return (StrokeList) this.clone();
        } else {
            StrokeList temp = new StrokeList();
            temp.add(this.get(this.size() - 4).clone());
            temp.add(this.get(this.size() - 3).clone());
            temp.add(this.get(this.size() - 2).clone());
            temp.add(this.get(this.size() - 1).clone());
            return temp;
        }
    }


    //find out the MST among strokes
    public StrokePoint[][] getMST() {
        int strokeNO = this.size();
        double d;
        double minimumm;
        int numberinst = 1;
        double xspots[] = new double[strokeNO];
        double yspots[] = new double[strokeNO];
        boolean visited[] = new boolean[strokeNO];
        int current[] = new int[strokeNO];
        double D[][] = new double[strokeNO][strokeNO];
        int  selstart=0;
        int  selend =0;
        StrokePoint[][] pointLists = new StrokePoint[2][strokeNO];
        for (int i = 0; i < strokeNO; i++) {
            for (int j = 0; j < strokeNO; j++) {
                D[i][j] = 0;
            }
        }


        for (int i = 0; i < strokeNO; i++) {
            visited[i] = false;
            xspots[i] = this.get(i).getCenter().X;
            yspots[i] = this.get(i).getCenter().Y;
        }
        current[0] = 0;
        visited[0] = true;

        for (int i = 0; i < strokeNO; i++) {
            for (int j = 0; j < strokeNO; j++) {
                if (j != i) {
                    d = (double) (Math.pow((xspots[i] - xspots[j]), 2) +
                            Math.pow((yspots[i] - yspots[j]), 2));
                    D[i][j] = Math.sqrt(d);
                }
            }
        }

        for(int k = 0;k < strokeNO - 1; k++) {
            minimumm = Integer.MAX_VALUE;
            for (int l = 0; l < numberinst; l++) {
                for (int j = 0; j < strokeNO; j++) {
                    if (visited[j] != true) {
                        if (minimumm > D[current[l]][j]) {
                            minimumm = D[current[l]][j];
                            selend = j;
                            selstart = current[l];
                        }
                    }
                }
            }
            visited[selend] = true;
            pointLists[0][k] = new StrokePoint(xspots[selstart], yspots[selstart]);
            pointLists[1][k] = new StrokePoint(xspots[selend], yspots[selend]);
            //distance += (int) D[selstart][selend];
            current[numberinst++] = selend;
        }
        return pointLists;
    }

    private Rectangle CalculateBoundingBox() {
        Stroke temp;
        double _boundingBoxWidth = -1;
        double _boundingBoxHeight = -1;
        double leftMostX = Integer.MAX_VALUE;
        double rightMostX = 0;
        double topMostY = Integer.MAX_VALUE;
        double bottomMostY = 0;

        /// For each stroke
        for (int i = 0; i < this.size(); i++) {

            temp = this.get(i);

            ///For each points
            for (int j = 0; j < temp.getTotalStrokePoints(); j++) {

                if (temp.getStrokePoint(j).X < leftMostX) {
                    leftMostX = temp.getStrokePoint(j).X;
                }
                if (temp.getStrokePoint(j).X > rightMostX) {
                    rightMostX = temp.getStrokePoint(j).X;
                }
                if (temp.getStrokePoint(j).Y < topMostY) {
                    topMostY = temp.getStrokePoint(j).Y;
                }
                if (temp.getStrokePoint(j).Y > bottomMostY) {
                    bottomMostY = temp.getStrokePoint(j).Y;
                }
            }
        }
        _boundingBoxWidth = rightMostX - leftMostX + 1;//Add 1 to meet the accuracy
        _boundingBoxHeight = bottomMostY - topMostY + 1;//Add 1 to Meet the accuracy

        Rectangle _boundingBox = new Rectangle((int)leftMostX, (int)topMostY,
                (int)_boundingBoxWidth, (int)_boundingBoxHeight);
        return _boundingBox;
    }

    @Override
    public StrokeList clone() {
        return (StrokeList) super.clone();
    }
}
