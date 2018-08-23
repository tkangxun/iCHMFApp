package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */

public class SVMResult {

    private int classIndex;
    private double probability;

    public void setIndex(int index) {
        classIndex = index;
    }

    public int getIndex() {
        return classIndex;
    }

    public void setProb(double prob) {
        probability = prob;
    }

    public double getProb() {
        return probability;
    }
}

