package com.example.travis.ichmfapp.symbollib;

import java.util.ArrayList;

/**
 * Created by Travis on 9/8/2018.
 */

public class SymbolList extends
        ArrayList<Symbol>
        implements Cloneable{

    /** Creates a new instance of SymbolList */
    public SymbolList() {
    }

    @Override
    public SymbolList clone(){
        return (SymbolList)super.clone();
    }


}
