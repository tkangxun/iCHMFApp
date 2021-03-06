package com.example.travis.ichmfapp.symbollib;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.preprocessor.PreprocessorSVM;
import com.example.travis.ichmfapp.preprocessor.SymbolRecognizer_SVM;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import symbolFeature.*;

import static com.example.travis.ichmfapp.symbollib.ConstantData.mydir;


/**
 * Created by Travis on 13/9/2018.
 */

public class Trainer{

    private SymbolLib objSymbolLib;
    private SymbolLib svmlib;
    private String fileSymbolLib = null;
    private SymbolList jList1 = null;
    private Context context = MainActivity.getAppContext();

    public Trainer (){

        ConstantData.savetophone();


    }




    private void generateDefaultSetSVM() {

        if (objSymbolLib == null) {
            return;
        }

        try {
            this.svmlib = SymbolLib.GenerateDefaultSetSVM(SymbolLib.LibraryTypes.Binary);
            //An array of all the basic symbols and create file
            jList1 = svmlib.getSymbols();
            List<Integer> indexes;
            for (int i = 0; i< jList1.size();i++){

                indexes = objSymbolLib.findSymbol(jList1.get(i).getSymbolCharDecimal());
                for (int j =0; j< indexes.size();j++){
                    symbolFeature.SymbolFeature.writeFeatures(
                            symbolFeature.SymbolFeature.getFeature(jList1.get(i).getSymbolCharDecimal(),
                            objSymbolLib.getSymbol(indexes.get(j)).getStrokes()));
                }
            }




        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Symbol getTrainsymbol(char sym){
        if (objSymbolLib == null) {
            return null;
        }

        int unicode = ((int)sym);
        List<Integer> indexes = objSymbolLib.findSymbol(unicode);
        if (indexes.size() == 0){
            Toast.makeText(context, "Symbol not found, try again", Toast.LENGTH_SHORT).show();
            return null;
        }
        return objSymbolLib.getSymbol(indexes.get(0));
    }

    public void generateDefaultSetElastic() {

        fileSymbolLib = ConstantData.ElasticFileDefaultString;
        try {
            SymbolLib.GenerateDefaultSetElastic(fileSymbolLib, SymbolLib.LibraryTypes.Binary);
            //Toast.makeText(context, " New Default Elastic File Generated!", Toast.LENGTH_SHORT).show();
            this.objSymbolLib = SymbolLib.Load(fileSymbolLib, SymbolLib.LibraryTypes.Binary);
            Toast.makeText(context, " New Default Elastic File loaded", Toast.LENGTH_SHORT).show();
            jList1=objSymbolLib.getSymbols();
            //Toast.makeText(context, "trainer for elastic size: " + jList1.size() + jList1.get(120), Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
            }

        }





    public  void openSymbolLib() throws Exception{
        //Choose Elastic file

        try {
            fileSymbolLib = ConstantData.ElasticFileString;
            objSymbolLib = SymbolLib.Load( fileSymbolLib,
                    SymbolLib.LibraryTypes.Binary);
            jList1 = objSymbolLib.getSymbols();

        } catch (Exception ex) {
            objSymbolLib = null;
            Toast.makeText(context, "Elastic file library not found.", Toast.LENGTH_SHORT).show();
            throw ex;
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

    public void removeSymbol(char sym) {
        try{openSymbolLib();}
        catch (Exception e){e.printStackTrace();}
        fileSymbolLib = ConstantData.ElasticFileString;
        if (objSymbolLib == null) {
            return;
        }

        int unicode = ((int) sym);
        List<Integer> indexes = objSymbolLib.findSymbol(unicode);
        if (indexes.size() == 0) {
            Toast.makeText(context, "Symbol not found, try again", Toast.LENGTH_SHORT).show();
            return;
        }
        StrokeList empty = new StrokeList();
        for (int i = 0; i < indexes.size(); i++) {
            objSymbolLib.getSymbol(indexes.get(i)).setStrokes(empty);
        }
        Toast.makeText(context, "symbol strokes have been removed from : " + indexes, Toast.LENGTH_SHORT).show();
    }

    public void addElasticSymbol(char sym, StrokeList strokes){
        try{openSymbolLib();}
        catch (Exception e){e.printStackTrace();}
        if (objSymbolLib == null) {
            return;
        }
        int unicode = ((int)sym);
        List<Integer> indexes = objSymbolLib.findSymbol(unicode);
        if (indexes.size() == 0){
            Toast.makeText(context, "Symbol not found, try again", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i< indexes.size(); i++){
            if (objSymbolLib.getSymbol(indexes.get(i)).getStrokes().size() == 0){
                objSymbolLib.getSymbol(indexes.get(i)).setStrokes(PreprocessorSVM.preProcessing(strokes));
                Toast.makeText(context, "Symbol added to index: " + indexes.get(i), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(context, "library for symbol already added to indexes: " + indexes, Toast.LENGTH_SHORT).show();


    }

    // adds a brand new symbol apart from the default list
    public void addSymbol(char sym) {

        int unicode = ((int)sym);
        List<Integer> indexes = objSymbolLib.findSymbol(unicode);
        if (indexes.size() == 0){
            Toast.makeText(context, "Symbol not found, try again", Toast.LENGTH_SHORT).show();
            return;
        }

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


    //input ? to train svm, secret key
    public void trainSymbolSVM(char sym, StrokeList strokes) {


        if( sym == '?'){
            //create new svm file base on new symbol input
            SVM_train svm = new SVM_train();
            svm.run();
            return;
        }

        File file = new File(mydir, ConstantData.trainFile);
        //String s = file.getPath().toString();
        if (!file.exists()){
            this.generateDefaultSetSVM();}


        int unicode = ((int)sym);

        List<Integer> indexes = objSymbolLib.findSymbol(unicode);
        if (indexes.size() == 0){
            Toast.makeText(context, "Symbol not found, try again", Toast.LENGTH_SHORT).show();
            return;
        }
        Symbol sbl = new Symbol(sym);
        sbl.setStrokes(strokes);

        if (SymbolRecognizer_SVM.checkStrokeNO(sbl.getSymbolCharDecimal(), sbl.getStrokes().size())) {
            SymbolFeature.writeFeatures(SymbolFeature.getFeature(sbl.getSymbolCharDecimal(), PreprocessorSVM.preProcessing(sbl.getStrokes())));

        } else {
            Toast.makeText(context, "invalid number of strokes", Toast.LENGTH_SHORT).show();
        }





    }




}
