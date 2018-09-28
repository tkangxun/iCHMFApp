package com.example.travis.ichmfapp.symbollib;


import android.content.Context;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.preprocessor.SymbolRecognizer_SVM;

import java.io.File;

import symbolFeature.*;


/**
 * Created by Travis on 13/9/2018.
 */

public class Trainer{

    private SymbolLib objSymbolLib;
    private File fileSymbolLib = null;
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
            Toast.makeText(context, "trainer size: " + jList1.size() +"   symbol: " + objSymbolLib.getSymbolFromChar('='), Toast.LENGTH_SHORT).show();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void trainSymbolSVM( int index) {
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

    private void generateDefaultSetElastic() {
        //JFileChooser jfc = new JFileChooser();
        //jfc.setSelectedFile(ConstantData.ElasticFileDefault);
        fileSymbolLib = ConstantData.ElasticFileDefault;
        try {
            SymbolLib.GenerateDefaultSetElastic(ConstantData.ElasticFileDefaultString, SymbolLib.LibraryTypes.Binary);
            this.objSymbolLib = SymbolLib.Load(ConstantData.ElasticFileDefaultString, SymbolLib.LibraryTypes.Binary);
            jList1=objSymbolLib.getSymbols();
        } catch (Exception ex) {
            ex.printStackTrace();
            }
        }


    private void openSymbolLib() {
        //JFileChooser jfc = new JFileChooser();
        //jfc.setMultiSelectionEnabled(false);
        //jfc.setSelectedFile(ConstantData.ElasticFile);
        //if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        fileSymbolLib = ConstantData.ElasticFile;
        try {
            objSymbolLib = SymbolLib.Load( ConstantData.ElasticFileString,
                    SymbolLib.LibraryTypes.Binary);
            jList1 = objSymbolLib.getSymbols();
            //jList1.setSelectedIndex(0);
        } catch (Exception ex) {
            objSymbolLib = null;
            Toast.makeText(context, "Error in loading Elastic file library.", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveSymbolLib() {
        if (fileSymbolLib != null && objSymbolLib != null) {
            try {
                objSymbolLib.setTitle(objSymbolLib.getTitle());
                objSymbolLib.Save(ConstantData.ElasticFileString, SymbolLib.LibraryTypes.Binary);
                Toast.makeText(context, "Elastic file Library is saved!.", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(context, "Error in saving Elastic file library.", Toast.LENGTH_SHORT).show();
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

    public void addSymbol(char code) {
        if (objSymbolLib == null) {
            return;
        }

        int unicode = ((int)code);
        if (code != 0) {
            try {
                Symbol sbl = new Symbol(SymbolLib.getHexToChar(unicode));
                objSymbolLib.addSymbol(sbl);
                jList1.clear();
                jList1 = objSymbolLib.getSymbols();
                //jList1.setSelectedIndex(objSymbolLib.getSymbols().size() - 1);
                //invalidate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
