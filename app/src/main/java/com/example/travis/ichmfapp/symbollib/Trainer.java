package com.example.travis.ichmfapp.symbollib;


import android.content.Context;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.preprocessor.PreprocessorSVM;
import com.example.travis.ichmfapp.preprocessor.SymbolRecognizer_SVM;



import symbolFeature.*;


/**
 * Created by Travis on 13/9/2018.
 */

public class Trainer{

    private SymbolLib objSymbolLib;
    private String fileSymbolLib = null;
    private SymbolList jList1 = null;
    private Context context = MainActivity.getAppContext();

    public Trainer (){
        this.generateDefaultSetSVM();
    }




    private void generateDefaultSetSVM() {


        try {
            this.objSymbolLib = SymbolLib.GenerateDefaultSetSVM(SymbolLib.LibraryTypes.Binary);
            //An array of all the basic symbols
            jList1 = objSymbolLib.getSymbols();

            Toast.makeText(context, "trainer size: " + jList1.size(), Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //SVM doesn't require saving button, save when write features
    public void trainSymbolSVM( int index) {
        if (jList1.get(index) == null) {
            return;
        }
        Symbol sbl = (Symbol) jList1.get(index);
        if (SymbolRecognizer_SVM.checkStrokeNO(sbl.getSymbolCharDecimal(), sbl.getStrokes().size())) {

            SymbolFeature.writeFeatures(SymbolFeature.getFeature(sbl.getSymbolCharDecimal(), sbl.getStrokes()));

            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Invalid Stroke Number", Toast.LENGTH_SHORT).show();
        }
    }

    public void generateDefaultSetElastic() {

        fileSymbolLib = ConstantData.ElasticFileDefaultString;
        try {
            SymbolLib.GenerateDefaultSetElastic(fileSymbolLib, SymbolLib.LibraryTypes.Binary);
            Toast.makeText(context, " New Default Elastic File Generated!", Toast.LENGTH_SHORT).show();
            this.objSymbolLib = SymbolLib.Load(fileSymbolLib, SymbolLib.LibraryTypes.Binary);
            Toast.makeText(context, " Default Elastic File loaded", Toast.LENGTH_SHORT).show();
            jList1=objSymbolLib.getSymbols();
            //Toast.makeText(context, "trainer for elastic size: " + jList1.size() + jList1.get(120), Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
            }
        }


    private void openSymbolLib() {
        //Choose Elastic file
        fileSymbolLib = ConstantData.ElasticFileString;
        try {
            objSymbolLib = SymbolLib.Load( fileSymbolLib,
                    SymbolLib.LibraryTypes.Binary);
            jList1 = objSymbolLib.getSymbols();
            //jList1.setSelectedIndex(0);
        } catch (Exception ex) {
            objSymbolLib = null;
            Toast.makeText(context, "Error in loading Elastic file library.", Toast.LENGTH_SHORT).show();
        }
    }


    public void saveSymbolLib() {
        if (objSymbolLib != null) {
            if (fileSymbolLib == ConstantData.ElasticFileDefaultString){
                fileSymbolLib = ConstantData.ElasticFileString;
            }
            try {
                objSymbolLib.setTitle(objSymbolLib.getTitle());
                objSymbolLib.Save(fileSymbolLib, SymbolLib.LibraryTypes.Binary);
                Toast.makeText(context, "Library is saved!.", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(context, "Error in saving library.", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }
    }

    public void removeSymbol(int removedIndex) {
        if (objSymbolLib == null) {
            return;
        }

        objSymbolLib.removeSymbol(removedIndex);
        jList1.clear();
        jList1= objSymbolLib.getSymbols();
        if (removedIndex != 0) {
            //jList1.setSelectedIndex(removedIndex - 1);
        } else {
            //jList1.setSelectedIndex(0);
        }

    }

    public void trainElasticSymbol(char sym, StrokeList strokes){
        if (objSymbolLib == null) {
            return;
        }

        int unicode = ((int)sym);
        Symbol sbl = new Symbol(SymbolLib.getHexToChar(unicode));
        sbl.setStrokes(PreprocessorSVM.preProcessing(strokes));
        objSymbolLib.setSymbol(sbl, objSymbolLib.findSymbol(unicode));

        Toast.makeText(context, "Symbol: " + sym + "ädded", Toast.LENGTH_SHORT).show();
    }

    // adds a brand new symbol apart from the default list
    public void addSymbol(char sym) {
        if (objSymbolLib == null) {
            return;
        }

        int unicode = ((int)sym);
        if (sym != 0) {
            try {
                Symbol sbl = new Symbol(SymbolLib.getHexToChar(unicode));
                objSymbolLib.addSymbol(sbl);
                jList1.clear();
                jList1 = objSymbolLib.getSymbols();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**private void trainSymbolSVM() {
        if (jList1.getSelectedValue() == null) {
            return;
        }
        Symbol sbl = (Symbol) jList1.getSelectedValue();
        if (SymbolRecognizer_SVM.checkStrokeNO(sbl.getSymbolCharDecimal(), sbl.getStrokes().size())) {
            SymbolFeature.writeFeatures(SymbolFeature.getFeature(sbl.getSymbolCharDecimal(), sbl.getStrokes()));
            this.jLabel4.setText("Success");
        } else {
            jLabel4.setText("Invalid Stroke Number");
        }
    }*/




}
