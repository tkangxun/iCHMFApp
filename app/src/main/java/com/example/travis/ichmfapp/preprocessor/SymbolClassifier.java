package com.example.travis.ichmfapp.preprocessor;

import com.example.travis.ichmfapp.symbollib.*;

import java.util.*;

/**
 * Created by Travis on 8/10/2018.
 */

public class SymbolClassifier {
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private static String[] operators = {".", "+", "-", "*", "/", "=", String.valueOf('\u2200'), String.valueOf('\u2203'),
            String.valueOf('\u2204'), String.valueOf('\u221E'),
            String.valueOf('\u221A'), String.valueOf('\u222E'),
            String.valueOf('\u2211'), String.valueOf('\u220f'),
            String.valueOf('\u222b'),
            //added by quxi 2009.09.07
            String.valueOf('\u2212')};
    private static String[] groupingListArr = {"sin", "cos", "tan", "sec", "cot",
            "arcsin", "arccos", "arctan", "arccot", "lim", "log", "ln"};
    private static ArrayList groupingList =
            new ArrayList(Arrays.asList(groupingListArr));
    private static String[] pre_superscript_list = {String.valueOf('\u221A')};

    //Pai, Sum, f, fraction
    private static String[] belowListArr = {String.valueOf('\u220f'), String.valueOf('\u2211'), String.valueOf('\u222b'),
            String.valueOf('\u2212')};
    //Pai, Sum, f, fraction, right arrow
    private static String[] aboveListArr = {String.valueOf('\u220f'), String.valueOf('\u2211'), String.valueOf('\u222b'),
            String.valueOf('\u2212'), String.valueOf('\u2192')};
    private static String[] insideListArr = {String.valueOf('\u221a'), "(", ")", "[", "]", "{", "}", String.valueOf('\u222b'),};
    private static int[] operatorSet1 = {
            37, //%
            47, ///
            60, //<
            61, //=
            62, //>
            126,//~
            94, //^
            215, //multiplication
            247, //division
            0x2192,// right arrow
            0x221D,// proportion to
            0x221E,// infinity
            0x2248,// almost equal to
            0x2260,// not equal to
            0x2264,// less or equal
            0x2265,// larger or equal
    };
    private static int[] SymbolSet = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, //number
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,//letter
            81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
            97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
            111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122,
            0x03B1, 0x03B2, 0x03B5, 0x03B8, 0x03BB, //greek
            0x03BC, 0x03C1, 0x03C3, 0x03C6};
    private static int[] operatorSet2 = { //start operator
            40, //(
            91, //[
            123,//{
            43, //+
            177, //+ or -
            0x220F,// multiply all
            0x2211,// sum all
            0x2212,// minus sign
            0x221A,// sqrt
            0x222B,// integral
    };
    private static int[] operatorSet3 = { //ending operator
            41, //)
            93, //]
            125,//}
    };
    private static int[] operatorSet4 = { //subscript operator
            42, //* Asterisk
            46, //.
            58, //:
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public SymbolClassifier() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * This method checks the a validity of the combination of
     * given symbol (string) and grouping position, based on
     * predefined possible logical relation.
     * @param s String of the recognized symbol.
     * @param group Integer value telling the relational position.
     * @return True for valid and false for otherwise.
     * @see StructuralAnalyser
     */
    public static Boolean checkPosClass(String s, int group) {
        if (group == StructuralAnalyser.ROW) {
            return true;
        } else if (group == StructuralAnalyser.SUPER_SCRIPT) {
            if (inArray(s, operators)) {
                return false;
            } else {
                return true;
            }
        } else if (group == StructuralAnalyser.SUB_SCRIPT) {
            if (inArray(s, operators) || StructuralAnalyser.isNumber(s)) {
                return false;
            } else {
                return true;
            }
        } else if (group == StructuralAnalyser.PRE_SUPER_SCRIPT || group == StructuralAnalyser.INSIDE) {
            if (inArray(s, pre_superscript_list)) {
                return true;
            } else {
                return false;
            }
        } else if (s.equals("m") && (group == StructuralAnalyser.PRE_SUB_SCRIPT || group == StructuralAnalyser.BELOW)) {
            return true;
        } else if (group == StructuralAnalyser.ABOVE || group == StructuralAnalyser.BELOW) {
            if (inArray(s, belowListArr)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //added by quxi 2009.12.30
    //check if 2 consecutive symbols could form valid expression
    public static boolean checkContext(int pos, RecognizedSymbol previous, RecognizedSymbol lastRecSymbol) {
        String previousString = previous.getSymbolCharString();
        String lastString = lastRecSymbol.getSymbolCharString();
        //the distance between 2 symbols is larger than the width of 2nd symbol

        switch (pos) {
            case StructuralAnalyser.ROW:
                return true;
            case StructuralAnalyser.BELOW: //neigbour symbol
                if (!checkNext(lastRecSymbol, previous)) {
                    return false;
                } else {
                    return true;
                }
            case StructuralAnalyser.ABOVE: //neigbour symbol
                if (!checkNext(lastRecSymbol, previous)) {
                    return false;
                } else {
                    return true;
                }
            case StructuralAnalyser.SUPER_SCRIPT:
//                if (inArray(relateString, SymbolSet) || inArray(relateString, operatorSet3)) {
//                    if (inArray(lastString, operatorSet2) || inArray(lastString, SymbolSet)) {
//                        return true;
//                    }
//                }
//                return false;
                return true;
            case StructuralAnalyser.SUB_SCRIPT:
//                if (inArray(relateString, SymbolSet)) {
//                    if (inArray(lastString, SymbolSet) || inArray(lastString, operatorSet4)) {
//                        return true;
//                    }
//                }
//                return false;
                return true;
            case StructuralAnalyser.INSIDE:
                if (inArray(previousString, insideListArr)) {
                    return true;
                } else {
                    return false;
                }
            case StructuralAnalyser.PRE_SUPER_SCRIPT:
//                if (inArray(relateString, pre_superscript_list) && inArray(lastString, SymbolSet)) {
//                    return true;
//                } else {
//                    return false;
//                }
                return true;
            case StructuralAnalyser.PRE_SUB_SCRIPT:
                return true;
            default:
                return false;
        }
    }


    //handle case when last symbol is - and above/under other operator
    private static boolean checkNext(RecognizedSymbol relate, RecognizedSymbol last) {
        String lastString = last.getSymbolCharString();
        String relateString = relate.getSymbolCharString();
        if (inArray(lastString, operatorSet2) && inArray(relateString, operatorSet2)) {
            if (relate.getBox().getWidth() >= 1.5 * last.getBox().getWidth()||last.getBox().getWidth() >= 1.5 * relate.getBox().getWidth()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    //added by quxi 2009.09.08
    //check if the 2 symbols could form a valid expression
    public static boolean oneExpression(int pos, RecognizedSymbol relateSymbol, RecognizedSymbol lastRecSymbol) {
        Box relateBox = relateSymbol.getBox();
        Box lastBox = lastRecSymbol.getBox();
        String relateString = relateSymbol.getSymbolCharString();
        String lastString = lastRecSymbol.getSymbolCharString();
        //the distance between 2 symbols is larger than the width of 2nd symbol

        switch (pos) {
            case StructuralAnalyser.ROW:
                double distance = lastBox.getX() - relateBox.getX() - relateBox.getWidth();
                double checkDistance = StructuralAnalyser.getMatrixElementDistance() == 0 ? lastBox.getWidth() : 0.8 * StructuralAnalyser.getMatrixElementDistance();
                if (StructuralAnalyser.getMatrixHandling() && distance > 0.7 * checkDistance) {
                    return false;
                }else if (StructuralAnalyser.openFences.contains(relateSymbol.getSymbolChar())||StructuralAnalyser.closeFences.contains(lastRecSymbol.getSymbolChar())){
                    return false;
                } else if ((!StructuralAnalyser.getMatrixHandling()) && distance > 0.7 * lastBox.getWidth()) {
                    return false;
                } else {
                    return true;
                }
            case StructuralAnalyser.BELOW:
                if (inArray(lastString, belowListArr) || inArray(relateString, aboveListArr)) {
                    return true;
                } else {
                    return false;
                }
            case StructuralAnalyser.ABOVE:
                if (inArray(lastString, aboveListArr) || inArray(relateString, belowListArr)) {
                    return true;
                } else {
                    return false;
                }
            case StructuralAnalyser.SUPER_SCRIPT: //handle = 3/5 case
                if (inArray(relateString, SymbolSet) || inArray(relateString, operatorSet3)) {
                    if (inArray(lastString, operatorSet2) || inArray(lastString, SymbolSet)) {
                        return true;
                    }
                }
                return false;
            case StructuralAnalyser.SUB_SCRIPT:
                if (inArray(relateString, SymbolSet)) {
                    if (inArray(lastString, SymbolSet) || inArray(lastString, operatorSet4)) {
                        return true;
                    }
                }
                return false;
            case StructuralAnalyser.INSIDE:
                if (inArray(relateString, insideListArr)) {
                    return true;
                } else {
                    return false;
                }
            case StructuralAnalyser.PRE_SUPER_SCRIPT:
                if (inArray(relateString, pre_superscript_list) && inArray(lastString, SymbolSet)) {
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    public static Boolean inArray(String c, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (c.equals(arr[i])) {
                return true;
            }
        }
        return false;
    }

    public static Boolean inArray(String c, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if ((int) c.charAt(0) == (arr[i])) {
                return true;
            }
        }
        return false;
    }
    // </editor-fold>
}
