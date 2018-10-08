package com.example.travis.ichmfapp.preprocessor;

import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.symbollib.*;

import java.util.*;

/**
 * Created by Travis on 31/8/2018.
 */

public class SymbolRecognizer {

    private SymbolLib _symbolLib;


    public SymbolRecognizer(SymbolLib objSymbolLib) {
        _symbolLib = objSymbolLib;
    }

    public ArrayList recognizing(ArrayList<RecognizedSymbol> recognizedList) throws Exception {
        ArrayList<RecognizedSymbol> resultList = new ArrayList();
        char symbolChar;
        ArrayList<Symbol> elasticSymbol;
        double distance;
        double temp = 0;
        double finalDistance;
        StrokeList sList;
        for (int i = 0; i < recognizedList.size(); i++) {
            distance = Double.MAX_VALUE;
            symbolChar = recognizedList.get(i).getSymbolChar();

            elasticSymbol = _symbolLib.getSymbolFromChar(symbolChar);

            sList = recognizedList.get(i).getStrokes();
            for (int j = 0; j < elasticSymbol.size(); j++) {

                temp = ElasticMatch(elasticSymbol.get(j), recognizedList.get(i));
                if (distance > temp) {
                    distance = temp;
                }
            }
            finalDistance = Math.pow(distance, 1 - recognizedList.get(i).getError());
            //finalDistance2 = distance * Math.pow(10, 1 - recognizedList.get(i).getError());

            resultList.add(new RecognizedSymbol(symbolChar, sList, finalDistance));
            if (ConstantData.doTest) {
                Toast.makeText(MainActivity.getAppContext(), "The symbol " + symbolChar + ". With distance :" + distance + " and Stroke number: " + sList.size() +
                        " and possibility: " + recognizedList.get(i).getError() + " and finalDistance : " + finalDistance, Toast.LENGTH_SHORT).show();
            }
        }
        Collections.sort(resultList, new ByDistance());

        if (resultList.size() > 10) {
            resultList.remove(resultList.size() - 1);
        }
        return resultList;
    }


    private double ElasticMatch(Symbol model, RecognizedSymbol symbol) {
        double finalDistance = 0.0;
        StrokePointList slModel = model.getTotalStrokePoints();
        StrokeList sSymbol = PreprocessorSVM.preProcessing(symbol.getStrokes());
        StrokePointList slSymbol = new StrokePointList();
        for (int i = 0; i < sSymbol.size(); i++) {
            for (int j = 0; j < sSymbol.get(i).getTotalStrokePoints(); j++) {
                slSymbol.add(sSymbol.get(i).getStrokePoint(j));
            }
        }//get all the stroke point of candidate symbol

        if (slModel.size() == slSymbol.size()) {
            for (int i = 0; i < slModel.size(); i++) {
                finalDistance += distance(slModel.get(i), slSymbol.get(i));
            }
        }
        return finalDistance;
    }

    protected static double distance(StrokePoint p1, StrokePoint p2) {
        return Math.sqrt((p1.X - p2.X) * (p1.X - p2.X) + (p1.Y - p2.Y) * (p1.Y - p2.Y));
    }
}

class ByDistance implements java.util.Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        RecognizedSymbol st1 = (RecognizedSymbol) o1;
        RecognizedSymbol st2 = (RecognizedSymbol) o2;
        int sdif = (st1.getError() - st2.getError()) > 0 ? 1 : -1;
        return sdif;
    }
}

