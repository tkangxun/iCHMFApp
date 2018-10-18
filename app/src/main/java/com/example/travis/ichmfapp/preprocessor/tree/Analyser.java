package com.example.travis.ichmfapp.preprocessor.tree;

import com.example.travis.ichmfapp.preprocessor.SymbolClassifier;
import com.example.travis.ichmfapp.symbollib.Box;
import com.example.travis.ichmfapp.symbollib.RecognizedSymbol;
import com.example.travis.ichmfapp.symbollib.StrokePoint;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Travis on 16/10/2018.
 */

public class Analyser {
    private static String[] groupingListArr = {"sin", "cos", "tan", "sec", "cot",
            "arcsin", "arccos", "arctan", "arccot", "lim", "log", "ln"};
    private static ArrayList groupingList = new ArrayList(
            Arrays.asList(groupingListArr));
    private static String[] closeFenceArr = {")", "}", "]"};
    private static ArrayList closeFences = new ArrayList(
            Arrays.asList(closeFenceArr));
    private static String[] openFenceArr = {"(", "{", "["};
    private static ArrayList openFences = new ArrayList(
            Arrays.asList(openFenceArr));

    public static final byte PRE_SUPER_SCRIPT = 1;
    public static final byte ABOVE = 2;
    public static final byte SUPER_SCRIPT = 3;
    public static final byte ROW = 6;

    public static final byte ROW_BEFORE = 4;
    public static final byte SUB_SCRIPT = 9;
    public static final byte BELOW = 8;
    public static final byte PRE_SUB_SCRIPT = 7;//
    //public static final byte ROW2 = 7;
    public static final byte INSIDE = 5;

    private static Tree tempTree;


    public void analyse(ArrayList<RecognizedSymbol> symbolList, Tree rawtree) {

        tempTree = rawtree;

        for (int i = 0; i < symbolList.size(); i++) {
            (symbolList.get(i)).setId(i);
        }
        //if only one symbol, add base to root and return
        if (symbolList.size()==1){
            rawtree.addRoot(new Node(symbolList.get(0),rawtree.getRoot(),"Root"));
            return;
        }
        RecognizedSymbol lastRecSymbol = symbolList.get(symbolList.size() - 1);
        RecognizedSymbol secondLastRecSymbol = symbolList.get(symbolList.size() - 2);

        doBaseLine(symbolList, lastRecSymbol, rawtree);

    }

    public void doBaseLine(ArrayList<RecognizedSymbol> recognizedList, RecognizedSymbol lastSbl, Tree rawtree){

        Boolean hasSibling = false;
        Boolean symbolHaondled = false;
        ArrayList<Node> baseLineNodes = rawtree.getBase();
        RecognizedSymbol closestSymbol = findClosest(recognizedList, lastSbl);




        //last sym is pos of closest sym
        int pos = boundingBoxDetermination(closestSymbol, lastSbl);

        if (pos == ROW && !symbolHaondled) {
            if (rawtree.symbolFindNode(closestSymbol.getId()) != null) {
                Node parent = rawtree.symbolFindNode(closestSymbol.getId()).getParent();
                if (checkSameLine(parent.getSiblingSymbols(), lastSbl)){
                    Node brother = new Node(lastSbl, parent, "row");
                    parent.addSibling(brother);
                    rawtree.appendTree(brother);




                }


                }
            }
        }





    private Boolean checkSameLine(ArrayList<RecognizedSymbol> baseLineSymbols , RecognizedSymbol lastRecSymbol) {

        char getlast = baseLineSymbols.get(baseLineSymbols.size()-1).getSymbolChar();
        return true;

    }















    private RecognizedSymbol findClosest(ArrayList<RecognizedSymbol> symbolList, RecognizedSymbol Last) {
        RecognizedSymbol closestSymbol = symbolList.get(0);
        double distance = Integer.MAX_VALUE;
        double d, dx, dy;
        for (int i = 0; i < symbolList.size(); i++) {
            dx = center(Last).X - center(symbolList.get(i)).X;
            dy = center(Last).Y - center(symbolList.get(i)).Y;
            d = Math.sqrt(dx * dx + dy * dy);
            if (d < distance && d != 0) {
                closestSymbol = symbolList.get(i);
                distance = d;
            }
        }
        return closestSymbol;
    }

    private StrokePoint center(RecognizedSymbol recgSymbol) {
        String[] array1 = {"t","f", "b", "d", "h", "k", "l"};
        String[] array2 = { "g", "j", "p", "q", "y"};
        String[] array3 = {"a","c","e","e","m","n","o","r","s","u","v","w","x","z"};

        StrokePoint center;

        Box rec = recgSymbol.getBox();

        /**
         * [1] For character t,f,b,d,h,k,l and 0-9 and capital letters
         * take x=0.5 and y=1- 0.55 location. Almost to the center
         * [2] For character g,j,p,q and y take center to be = 1- 0.65
         * [3] the rest of the small letters, take center to be = 1- 0.75
         * [4] For rest of the characters, take take x=0.5 and y=1- 0.5,
         */
        if (SymbolClassifier.inArray(recgSymbol.getSymbolCharString(), array1) || Character.isDigit(recgSymbol.getSymbolCharString().charAt(0)) || Character.isUpperCase(recgSymbol.getSymbolChar())) {
            center = new StrokePoint((int) (rec.getX() + 0.5 * rec.getWidth()),
                    (int) (rec.getY() + 0.45 * rec.getHeight()));
        } else if (SymbolClassifier.inArray(recgSymbol.getSymbolCharString(), array2)) {
            center = new StrokePoint((int) (rec.getX() + 0.5 * rec.getWidth()),
                    (int) (rec.getY() + 0.35 * rec.getHeight()));
        } else if (SymbolClassifier.inArray(recgSymbol.getSymbolCharString(), array3)) {
            center = new StrokePoint((int) (rec.getX() + 0.5 * rec.getWidth()),
                    (int) (rec.getY() + 0.25 * rec.getHeight()));
        }
        //center of sqrt should be at the left side hook part
        else if ( recgSymbol.getSymbolCharString().equals(String.valueOf('\u221A'))) {
            center = new StrokePoint((int) (rec.getX() + 0.1 * rec.getWidth()),
                    (int) (rec.getY() + 0.5 * rec.getHeight()));
        } else {
            center = new StrokePoint((int) (rec.getX() + 0.5 * rec.getWidth()),
                    (int) (rec.getY() + 0.5 * rec.getHeight()));
        }
        return center;
    }

    /**
     * Determine the relational position of two recognized symbol
     * by calculating their bounding box and typographic center.
     * @param secondLastRecSymbol Previous neighbor of last recognized symbol.
     * @param lastRecSymbol Last Recognized symbol.
     * @return <br/>
     *   0 = superscript<br/>
     *   1 = below/above<br/>
     *   2 = superscript<br/>
     *   3 = row<br/>
     *   4 = subscript<br/>
     *   5 = above/below<br/>
     *   6 = subscript<br/>
     *   7 = row<br/>
     *   8 = inside<br/>
     * @see RecognizedSymbol
     */
    protected int boundingBoxDetermination(RecognizedSymbol secondLastRecSymbol, RecognizedSymbol lastRecSymbol) {

        Box secondLastBBox = secondLastRecSymbol.getBox();
        StrokePoint secondLastCenter = center(secondLastRecSymbol);
        String secondChar = secondLastRecSymbol.getSymbolCharString();
        String lastChar = lastRecSymbol.getSymbolCharString();
        //- ,= , nearly equal
//        if ((isShortSymbol(secondChar)||isShortSymbol(lastChar))&&!(isShortSymbol(secondChar)&&isShortSymbol(lastChar))) {
//            return boundingBoxDeterminationForShortSymbol(secondLastRecSymbol, lastRecSymbol);
//        }
        return boundingBoxDetermination(secondLastBBox, secondLastCenter, lastRecSymbol);
    }

    /**
     * Determine the realtional position of two recognized symbol
     * by calculating their bounding box and typographic center.
     * @param secondLastCenter base on second last symbol or nearest symbol
     * @param lastRecSymbol Last Recognized symbol.
     * @return
     * */

    private int boundingBoxDetermination(Box secondLastBBox, StrokePoint secondLastCenter,
                                         RecognizedSymbol lastRecSymbol) {

        Box lastBBox = lastRecSymbol.getBox();
        StrokePoint lastCenter = center(lastRecSymbol);
        //Find angle degree between two Center points.
        double angle = getAngle(secondLastCenter, lastCenter);

        /**if ((lastBBox.getX() >= secondLastBBox.getX() && lastBBox.getX() + 0.8 * lastBBox.getWidth() <= secondLastBBox.getX() + secondLastBBox.getWidth()) && (lastBBox.getY() >= secondLastBBox.getY() && lastBBox.getY() + 0.8 * lastBBox.getHeight() <= secondLastBBox.getY() + secondLastBBox.getHeight())) {
            return INSIDE;
        }	//	inside*/

        //assuming second last symbol is sqrt, the center of sqrt should be larger than the start of the symbol inside
        // and the width of the symbol doesn't exceed the sqrt
        //and the height doesn't exceed more than 1.1 times of the sqrt
        if (secondLastBBox.getCenterX() >= lastBBox.getX() &&
                secondLastBBox.getX()+secondLastBBox.getWidth() >= lastBBox.getX() + lastBBox.getWidth() &&
                secondLastBBox.getY()+ 1.1 * secondLastBBox.getHeight() >= lastBBox.getY() + lastBBox.getHeight()){
            return INSIDE;
        }

        if (angle >= -Math.PI / 8 && angle <= Math.PI / 8) { //22.5
            //When absolute distance between tow centers is lesser than
            //1/4 of possible maximum 2nd last symbol height/width or
            //last symbol height/width. It is determined as SAME ROW.
            if (Math.abs(lastCenter.Y - secondLastCenter.Y) <= Math.max(Math.max( secondLastBBox.getHeight() / 4,
                     lastBBox.getHeight() / 4),
                    Math.max( secondLastBBox.getWidth() / 4,
                            lastBBox.getWidth() / 4))) {
                if (lastCenter.X >= secondLastCenter.X) {
                    return ROW;
                } else {
                    return ROW_BEFORE;
                }
            }
        }
        //for below
        // When [last box height 80% is outside and above of 2nd last box's Y OR
        // 2nd last box height 80% is outside and above of last box Y] AND
        //
        // {[20% of last box is outside in left side of 2nd last box AND
        // 20% of last box is outside in right side of 2nd last box] OR
        // [20% of second last box is outside in left side of last box AND
        //  20% of second last box is outside in right side of last box]}
        // It is to decide ABOVE or BELOW.
        // When last box's center Y is lower (Y value greater) than
        // 2nd last box's center. Second Last Symbol is ABOVE the Last Symbol.
        // other wise. Second Last Symbol is BELOW the Last Symbol.
        if ((lastBBox.getY() + 0.8 * lastBBox.getHeight() <= secondLastBBox.getY() || //last below
                secondLastBBox.getY() + 0.8 * secondLastBBox.getHeight() <= lastBBox.getY()) // last above
                &&
                /**
                ((lastBBox.getX() - 0.2 * lastBBox.getWidth() <= secondLastBBox.getX() && //before
                        lastBBox.getX() + 1.2 * lastBBox.getWidth() >= secondLastBBox.getX() + secondLastBBox.getWidth()) ||
                 (secondLastBBox.getX() - 0.2 * secondLastBBox.getWidth() <= lastBBox.getX() &&
                 secondLastBBox.getX() + 1.2 * secondLastBBox.getWidth() >= lastBBox.getX() + lastBBox.getWidth()))) {*/

                (lastBBox.getCenterX() <= secondLastBBox.getX()+0.9*secondLastBBox.getWidth())){ //last sym is not super(onthe left)
            if (lastCenter.Y >= secondLastCenter.Y) {
                return BELOW;
            } else {
                return ABOVE;
            }
        } else {
            //When identification of above/below relationship fails
            //check for other possibilities

            //When last symbol center is at the right side of
            //second last symbol center.
            if (lastCenter.X >= secondLastCenter.X) {
                if (angle >= -Math.PI / 8 && angle <= Math.PI / 8) { //22.5
                    //When absolute distance between tow centers is lesser than
                    //1/4 of possible maximum 2nd last symbol height/width or
                    //last symbol height/width. It is determined as SAME ROW.
                    if (lastCenter.Y > secondLastCenter.Y) {
                        System.out.println("bb:9");
                        return SUB_SCRIPT;//sub script 4
                    } else {
                        System.out.println("bb:3");
                        return SUPER_SCRIPT;//super script 2
                    }
                } else if (angle > Math.PI / 8 && angle < 3 * Math.PI / 8) {
                    System.out.println("bb:7");
                    return SUB_SCRIPT;//4
                } //subscript
                else if (angle < -Math.PI / 8 && angle > -3 * Math.PI / 8) {
                    System.out.println("bb:8");
                    return SUPER_SCRIPT;
                } //superscript
                else if (angle >= 3 * Math.PI / 8 && angle <= Math.PI / 2) {
                    System.out.println("bb:9");
                    return BELOW;//5
                } //above
                else if (angle <= -3 * Math.PI / 8 && angle >= -Math.PI / 2) {
                    System.out.println("bb:10");
                    return ABOVE;//5
                }//below
            } else {
                //When last symbol center is NOT at the right side of
                //second last symbol center.
                if (angle > 0 && angle < Math.PI / 2) {
                    System.out.println("bb:11");
                    return PRE_SUPER_SCRIPT;//0
                } //superscript
                else if (angle >= -Math.PI / 8 && angle <= Math.PI / 8) {
                    if (lastCenter.Y > secondLastCenter.Y) {
                        System.out.println("bb:13");
                        return PRE_SUB_SCRIPT;//6
                    } else {
                        System.out.println("bb:18");
                        return PRE_SUPER_SCRIPT;//0
                    }
                } //else if(angle>Math.PI/8 && angle<3*Math.PI/8) return 0;	//superscript
                else if (angle < -Math.PI / 8 && angle > -3 * Math.PI / 8) {
                    System.out.println("bb:14");
                    return PRE_SUB_SCRIPT;//6
                } //subscript
                else if (angle >= 3 * Math.PI / 8 && angle <= Math.PI / 2) {
                    System.out.println("bb:15");
                    return ABOVE;//1
                } //above
                else if (angle <= -3 * Math.PI / 8 && angle >= -Math.PI / 2) {
                    System.out.println("bb:16");
                    return BELOW;//5
                }	//below
            }
        }
        System.out.println("bb:17");
        return ROW;

    }

    private double getAngle(StrokePoint center1, StrokePoint center2) {
        double angle = Math.atan((double) (center2.Y - center1.Y) / (center2.X - center1.X));
        return angle;
    }
}
