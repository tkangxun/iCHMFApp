package com.example.travis.ichmfapp.symbollib;

import java.io.Serializable;

/**
 * Created by Travis on 7/8/2018.
 */

/* represents a mathematical symbol */

public class Symbol implements Cloneable, Serializable {

    /**
     * The list of strokes related to the symbol.
     */
    protected StrokeList _Strokes;
    /**
     * The chracter of the symbol.
     */
    protected char _charSymbol;
    /**
     * Default constructor.
     */
    public Symbol() {
        _Strokes = new StrokeList();
    }

    /**
     * Overloaded constructor
     * @param symbolChar Character to assign for the symbol.
     */
    public Symbol(char symbolChar) {
        _Strokes = new StrokeList();
        _charSymbol = symbolChar;
    }

    public char getSymbolChar() {
        return _charSymbol;
    }

    public void setSymbolChar(char value) {
        _charSymbol = value;
    }

    /**
     * To get the HEX code of symbol chracter
     * @return HEX formatted string.

    public String getSymbolCharHex() {
        return SymbolLib.getCharToHex(_charSymbol);
    }
    */


    /**
     * To get decimal value of symbol character
     * @return Decimal value

    public int getSymbolCharDecimal() {
        return SymbolLib.getCharToDecimal(_charSymbol);
    }
     */

    @Override
    public String toString() {
        return String.valueOf(this._charSymbol);
    }

    public StrokeList getStrokes() {
        return _Strokes;
    }

    public void setStrokes(StrokeList value) {
        _Strokes = value;
    }

    public int getTotalStrokes() {
        return _Strokes.size();
    }

    public StrokePointList getTotalStrokePoints() {
        StrokePointList sl = new StrokePointList();
        for (int i = 0; i < _Strokes.size(); i++) {
            for (int j = 0; j < _Strokes.get(i).getTotalStrokePoints(); j++) {
                sl.add(_Strokes.get(i).getStrokePoint(j));
            }
        }
        return sl;
    }

    public void addStroke(Stroke s) {
        _Strokes.add(s);
    }

    public Stroke getStroke(int index) {
        return (Stroke) _Strokes.get(index);
    }

    public void setStroke(Stroke s, int index) {
        _Strokes.set(index, s);
    }

    public void removeStroke(int index) {
        _Strokes.remove(index);
    }

    /**
     * Get a SymbolDisplay object for Symbol object.
     * @return GUI component.
     * @deprecated
     * see SymbolDisplay

    @Deprecated
    public SymbolDisplay getSymbolDisplay() {
        //return new SymbolDisplay(this);
        return null;
    }
    */

    public String ToString() {
        return String.valueOf(_charSymbol);
    }

    @Override
    public Symbol clone() throws CloneNotSupportedException {
        return (Symbol) super.clone();
    }

    /**
     * Get the symbol chracter in string data type.
     * @return String value of symbol character.
     */
    public String getSymbolCharString() {
        return String.valueOf(_charSymbol);
    }


}
