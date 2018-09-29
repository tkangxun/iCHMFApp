package com.example.travis.ichmfapp.preprocessor;

import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.symbollib.*;
import java.util.*;

import symbolFeature.*;

//* Created by Travis on 23/8/2018.

public class SymbolRecognizer_SVM {

    public ArrayList recognizing(StrokeList _strokeListMemory) throws Exception {
        ArrayList result = new ArrayList();
        SVM_predict sp = new SVM_predict();
        char symbolChar;
        long startTime = System.currentTimeMillis();
        boolean[] validStroke = oneSymbol(_strokeListMemory);
        //Toast.makeText(MainActivity.getAppContext(), "created SVM", Toast.LENGTH_SHORT).show();

        for (int count = _strokeListMemory.size(); count > 0; count--) {
            if (validStroke[count - 1] == false) {
                continue;
            }
            StrokeList _strokeListLocal = new StrokeList();
            for (int k = 0; k < count; k++) {
                _strokeListLocal.add(_strokeListMemory.get(
                        _strokeListMemory.size() - (count - k)).clone());
            }

            StrokeList preProcessedStrokeList = PreprocessorSVM.preProcessing(_strokeListLocal);
            //compare storkelist with each symbol in symbol library _quxi
            String featureString = SymbolFeature.getFeature(0, preProcessedStrokeList);
            Toast.makeText(MainActivity.getAppContext(), "processed and get featured", Toast.LENGTH_SHORT).show();

            List<SVMResult> svmResult = sp.run(featureString, 1);
            //System.out.println("For round" + count + " Time after SVM is" + (System.currentTimeMillis() - startTime));
            for (int j = 0; j < svmResult.size(); j++) {
                if (checkStrokeNO(svmResult.get(j).getIndex(), preProcessedStrokeList.size())) {
                    symbolChar = (char) svmResult.get(j).getIndex();
                    RecognizedSymbol tempResult = new RecognizedSymbol(symbolChar, _strokeListLocal, svmResult.get(j).getProb());
                    result.add(tempResult);
                }
            }
        }
        return result;
    }

    private boolean[] oneSymbol(StrokeList stlist) {
        boolean[] validStroke = new boolean[4]; //validStroke 0 -- 1 stroke, 1 -- 2 strokes, 2 -- 3 strokes, 3 -- 4 strokes
        Stroke[] strokeNo = new Stroke[4]; // stores stroke information as exact sequence of stlist
        for (int i = 0; i < 4; i++) {
            validStroke[i] = false;
            if (stlist.size() - 1 < i) {
                strokeNo[i] = null;
            } else {
                strokeNo[i] = stlist.get(i);
            }
        }

        switch (stlist.size()) {
            case 1:
                validStroke[0] = true;
                break;
            case 2:
                if (!checkStrokeClose(copyArray(strokeNo, 0, 0), copyArray(strokeNo, 1, 1))) {
                    validStroke[0] = true;
                } else {
                    if (!intersect(strokeNo[0], strokeNo[1])) {
                        validStroke[0] = true;
                    }
                    validStroke[1] = true;
                }
                break;
            case 3:
                if (!checkStrokeClose(copyArray(strokeNo, 0, 1), copyArray(strokeNo, 2, 2))) {
                    validStroke[0] = true;
                } else {
                    if (!intersect(copyArray(strokeNo, 0, 1), copyArray(strokeNo, 2, 2))) {
                        validStroke[0] = true;
                    }
                    if (!checkStrokeClose(copyArray(strokeNo, 0, 0), copyArray(strokeNo, 1, 2))) {
                        validStroke[1] = true;
                    } else {
                        if (!intersect(copyArray(strokeNo, 0, 0), copyArray(strokeNo, 1, 2))) {
                            validStroke[1] = true;
                        }
                        validStroke[2] = true;
                    }
                }
                break;

            case 4:
                //E, M, W, Sum all
                boolean check4Stroke = true; //4 stroke symbols formed by all connected strokes
                if (!checkStrokeClose(copyArray(strokeNo, 0, 2), copyArray(strokeNo, 3, 3))) {
                    validStroke[0] = true;
                } else {
                    if (!intersect(copyArray(strokeNo, 0, 2), copyArray(strokeNo, 3, 3))) {
                        check4Stroke = false;
                        validStroke[0] = true;
                    }
                    if (!checkStrokeClose(copyArray(strokeNo, 0, 1), copyArray(strokeNo, 2, 3))) {
                        validStroke[1] = true;
                    } else {
                        if (!intersect(copyArray(strokeNo, 0, 1), copyArray(strokeNo, 2, 3))) {
                            check4Stroke = false;
                            validStroke[1] = true;
                        }
                        if (!checkStrokeClose(copyArray(strokeNo, 0, 0), copyArray(strokeNo, 1, 3))) {
                            validStroke[2] = true;
                        } else {
                            if (!intersect(copyArray(strokeNo, 0, 0), copyArray(strokeNo, 1, 3))) {
                                check4Stroke = false;
                                validStroke[2] = true;
                            }
                        }
                    }
                }
                if (check4Stroke) {
                    validStroke[3] = true;
                }
                break;
        }
        return validStroke;
    }

    private Stroke[] copyArray(Stroke[] list, int start, int end) {
        Stroke[] newList = new Stroke[end - start + 1];
        for (int i = 0; i <= end - start; i++) {
            newList[i] = list[start + i];
        }
        return newList;
    }

    private boolean checkStrokeClose(Stroke[] slist1, Stroke[] slist2) {
        double start1 = 1000;
        double start2 = 1000;
        double end1 = 0;
        double end2 = 0;
        double start, end;
        for (int i = 0; i < slist1.length; i++) {
            start = slist1[i].CalculateBoundingBox().x;
            end = slist1[i].CalculateBoundingBox().width + start;
            if (start1 > start) {
                start1 = start;
            }
            if (end1 < end) {
                end1 = end;
            }
        }
        for (int i = 0; i < slist2.length; i++) {
            start = slist2[i].CalculateBoundingBox().x;
            end = slist2[i].CalculateBoundingBox().width + start;
            if (start2 > start) {
                start2 = start;
            }
            if (end2 < end) {
                end2 = end;
            }
        }
        double symbolDistance = Math.max(end2-start2, end1-start1)*1/5;
        if (start2 - end1 > symbolDistance || start1 - end2 > symbolDistance) {  // at left or right
            return false;
        }
        return true;
    }

    private boolean intersect(Stroke[] slist1, Stroke[] slist2) {
        Stroke s1, s2;
        for (int i = 0; i < slist1.length; i++) {
            s1 = slist1[i];
            for (int j = 0; j < slist2.length; j++) {
                s2 = slist2[j];
                if (intersect(s1, s2) == true) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean intersect(Stroke s1, Stroke s2) {
        double xA, yA, xB, yB, xC, yC, xD, yD, xI, yI;
        double aAB, bAB, aCD, bCD;
        double closestDistance = Double.MAX_VALUE; //the smallest distance between 2 strokes
        double tempDistance;

        for (int j = 0; j < s1.getTotalStrokePoints() - 1; j++) {
            xA = s1.getStrokePoint(j).X;
            yA = s1.getStrokePoint(j).Y;
            xB = s1.getStrokePoint(j + 1).X;
            yB = s1.getStrokePoint(j + 1).Y;
            for (int k = 0; k < s2.getTotalStrokePoints() - 1; k++) {
                xC = s2.getStrokePoint(k).X;
                yC = s2.getStrokePoint(k).Y;
                xD = s2.getStrokePoint(k + 1).X;
                yD = s2.getStrokePoint(k + 1).Y;
                tempDistance = SymbolRecognizer.distance(s1.getStrokePoint(j), s2.getStrokePoint(k));
                if (tempDistance < closestDistance) {
                    closestDistance = tempDistance;
                }
                try {
                    //y = kx+b; bAB = k and aAB = b
                    bAB = (yB - yA) / (xB - xA); //PoErr:AB is vertical
                    bCD = (yD - yC) / (xD - xC); //PoErr:CD is vertical
                    if (notInfi(bAB) && notInfi(bCD)) {
                        aAB = yA - (bAB * xA);
                        aCD = yC - (bCD * xC);
                        xI = -(aAB - aCD) / (bAB - bCD);//PoErr:AB || CD
                        yI = aAB + (bAB * xI);
                    } else if (notInfi(bAB)) {
                        xI = xD;
                        aAB = yA - (bAB * xA);
                        yI = aAB + (bAB * xI);
                    } else if (notInfi(bCD)) {
                        xI = xA;
                        aCD = yC - (bCD * xC);
                        yI = aCD + (bCD * xI);
                    } else {
                        continue;
                    }
                    if ((xA - xI) * (xI - xB) >= 0 && (xC - xI) * (xI - xD) >= 0 && (yA - yI) * (yI - yB) >= 0 && (yC - yI) * (yI - yD) >= 0) {
                        //Intersection found
                        closestDistance = 0;
                        break;
                    }
                } catch (Exception ex) {
                    j++;
                }
            }
        }
        return (closestDistance < 8);
    }

    private boolean notInfi(double num) {
        if (num == Double.POSITIVE_INFINITY || num == Double.NEGATIVE_INFINITY) {
            return false;
        }
        return true;
    }

    public static boolean checkStrokeNO(int index, int strokeCount) {
        switch (strokeCount) {
            case 1:
                if (inArray(index, SymbolFeature.oneStroke)) {
                    return true;
                } else {
                    return false;
                }
            case 2:
                if (inArray(index, SymbolFeature.twoStroke)) {
                    return true;
                } else {
                    return false;
                }
            case 3:
                if (inArray(index, SymbolFeature.threeStroke)) {
                    return true;
                } else {
                    return false;
                }
            case 4:
                if (inArray(index, SymbolFeature.fourStroke)) {
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }

    private static Boolean inArray(int c, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (c == arr[i]) {
                return true;
            }
        }
        return false;
    }
}
