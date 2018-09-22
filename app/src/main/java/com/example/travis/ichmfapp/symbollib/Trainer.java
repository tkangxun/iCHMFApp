package com.example.travis.ichmfapp.symbollib;


import java.io.File;


/**
 * Created by Travis on 13/9/2018.
 */

public class Trainer{

    private SymbolLib objSymbolLib;
    private File fileSymbolLib = null;
    private SymbolList jList1 = null;

    public Trainer (StrokeList s1){
        this.generateDefaultSetSVM();



    }

    private void generateDefaultSetSVM() {
        try {
            this.objSymbolLib = SymbolLib.GenerateDefaultSetSVM(SymbolLib.LibraryTypes.Binary);
            jList1 = objSymbolLib.getSymbols();
            //jList1.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
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
            System.out.print("Error in loading Elastic file library.");
            ex.printStackTrace();
        }
    }


    private void saveSymbolLib() {
        if (fileSymbolLib != null && objSymbolLib != null) {
            try {
                objSymbolLib.setTitle(objSymbolLib.getTitle());
                objSymbolLib.Save(ConstantData.ElasticFileString, SymbolLib.LibraryTypes.Binary);
                System.out.print("Elastic file Library is saved!.");
            } catch (Exception ex) {
                System.out.print("Error in saving Elastic file library.");
            }
        }
    }


}
