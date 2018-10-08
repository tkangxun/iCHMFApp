package com.example.travis.ichmfapp.symbollib;

//import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Travis on 31/8/2018.
 */

public class Baseline {
    protected int startX;
    protected int startY;
    protected int width;
    protected int height;
    protected List<RecognizedSymbol> symbolList = new ArrayList();
    protected StrokePoint center;
    protected Box rec;

    public Baseline(RecognizedSymbol symbol, StrokePoint center) {
        this.rec = symbol.getBox();
        startX = rec.x;
        startY = rec.y;
        this.height = rec.height;
        this.width = rec.width;
        symbolList.add(symbol);
        this.center = center;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getLUX() {
        return startX;
    }

    public int getLUY() {
        return startY;
    }

    public void setLUX(int x) {
        startX = x;
    }

    public Box getRect() {
        return rec;
    }

    public StrokePoint getCenter() {
        return center;
    }

    public List<RecognizedSymbol> getSymbolList() {
        return symbolList;
    }

    public RecognizedSymbol getSymbol() {
        return symbolList.get(0);
    }

    public void setSymbol( RecognizedSymbol symbol) {
        symbolList.set(0, symbol);
    }

    public void setSymbol(int index, RecognizedSymbol symbol) {
        symbolList.set(index, symbol);
    }

    public void addSymbol(RecognizedSymbol symbol){
        symbolList.add(symbol);
    }
    public void removeSymbol(RecognizedSymbol symbol){
        symbolList.remove(symbol);
    }
}
