package com.example.travis.ichmfapp.preprocessor;

import android.inputmethodservice.Keyboard;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.symbollib.*;

import java.util.*;

import org.w3c.dom.*;


/**
 * Created by Travis on 8/10/2018.
 */

public class StructuralAnalyser {
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
    private static int maxGroupingLength = 6;
    public static final byte PRE_SUPER_SCRIPT = 0;
    public static final byte ABOVE = 1;
    public static final byte SUPER_SCRIPT = 2;
    public static final byte ROW = 3;
    public static final byte ROW_BEFORE = -3;
    public static final byte SUB_SCRIPT = 4;
    public static final byte BELOW = 5;
    public static final byte PRE_SUB_SCRIPT = 6;//
    public static final byte ROW2 = 7;
    public static final byte INSIDE = 8;
    private Document _tempExpressionTree;

    List<Baseline> baseLine;

    boolean fracHandling;
    List<Integer> sqrtHandling = new ArrayList<Integer>();
    static boolean matrixHandling;

    //tell if the formula is a matrix or an normal equation
    boolean matrixFound;
    static double elementDistance = 0;

    /**
     * Default constructor for Structural Analyzer.
     */
    public StructuralAnalyser(List<Baseline> baseLine) {

        this.baseLine = baseLine;
        fracHandling = false;
        sqrtHandling.clear();
        matrixFound = false;
        matrixHandling = false;
    }

    /**
     * The main method to perform the structural analysis.
     * @param recognizedSymbolList The list of recognized symbols.
     * @param rawExpressionTree Expression Tree for data storage.
     * @see RecognizedSymbol
     */
    public void analyse(List<RecognizedSymbol> recognizedSymbolList,
                        Document rawExpressionTree) {

        //this is just for internal reference
        _tempExpressionTree = rawExpressionTree;

        /**
         * For each item (RecognizedSymbol) in given list,
         * tag them with their specific index.
         * The sequence of th index is the writing sequence of
         * symbols (i.e the recognized sequence of symbols).
         */
        for (int i = 0; i < recognizedSymbolList.size(); i++) {
            ((RecognizedSymbol) recognizedSymbolList.get(i)).setId(i);
        }

        RecognizedSymbol lastRecSymbol = (RecognizedSymbol) recognizedSymbolList.get(recognizedSymbolList.size() - 1);

        // When RecognizedSymbol list contains only ONE item
        // (i.e upon recognition of 1st symbol character),
        // [1] Create a Root node and add 1st recognized symbol node
        // under Root node.
        // [2] Remove any existing nodes in given tree, to rebuild new one.
        // [3] Add newly created Root to the tree.
        // In case RecognizedSymbol list contains more than ONE item dobaseline

        if (recognizedSymbolList.size() == 1) {


            baseLine.add(new Baseline(recognizedSymbolList.get(0), center(recognizedSymbolList.get(0))));
            //--------------------
            Element root = rawExpressionTree.createElement("rootequation");
            Element node = createSymbolNode((RecognizedSymbol) recognizedSymbolList.get(0), rawExpressionTree);
            root.appendChild(node);

            if (lastRecSymbol.getSymbolChar() =='\u221a' ) {
                sqrtHandling.add(lastRecSymbol.getId());
                if (node.getChildNodes().item(INSIDE).getChildNodes().getLength() == 0) {
                    Element child = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                    node.getChildNodes().item(INSIDE).appendChild(child);
                }
            }



            while (rawExpressionTree.getChildNodes().getLength() != 0) {
                rawExpressionTree.removeChild(rawExpressionTree.getLastChild());
            }
            rawExpressionTree.appendChild(root);
        } else {
            RecognizedSymbol secondLastRecSymbol = (RecognizedSymbol) recognizedSymbolList.get(recognizedSymbolList.size() - 2);

            if (ConstantData.doTest) {
                System.out.println("        -BEFORE TREE");
                visualizeTree((Element) rawExpressionTree.getFirstChild(), "");
                System.out.println("        -BEFORE TREE");
            }

            boolean success = true;
            //THIS IS TEMPORARY GATE
            doBaseLine(recognizedSymbolList, lastRecSymbol, rawExpressionTree);
            //success = doAnalyse(secondLastRecSymbol, lastRecSymbol, recognizedSymbolList, rawExpressionTree);


            if (ConstantData.doTest) {
                System.out.println("        -AFTER TREE");
                visualizeTree((Element) rawExpressionTree.getFirstChild(), "");
                System.out.println("        -AFTER TREE");
            }

             if (!success) {// Do correction
                int pos = boundingBoxDetermination(secondLastRecSymbol, lastRecSymbol);
                int newPos = doCorrection(secondLastRecSymbol, pos);
                if (newPos == 3) {
                    Node equation = secondLastRecSymbol.getNode().getParentNode();
                    Element node = createSymbolNode(lastRecSymbol, rawExpressionTree);
                    equation.appendChild(node);
                } else {
                    Element node = createSymbolNode(lastRecSymbol, rawExpressionTree);
                    Node equation = secondLastRecSymbol.getNode().getChildNodes().item(newPos);
                    equation.appendChild(node);
                }
//                matrixDetectStep(lastRecSymbol, mdt, rawExpressionTree, recognizedSymbolList);
            }
        }
    }

    private void visualizeTree(Element startNode, String padding) {
        System.out.println(padding + startNode.getAttribute("id") + " " + startNode.getNodeName() + "( " + startNode.getAttribute("identity") + " )" + "[ " + startNode.getAttribute("type") + " ]");

        if (startNode.hasChildNodes()) {
            for (int i = 0; i < startNode.getChildNodes().getLength(); i++) {
                visualizeTree((Element) startNode.getChildNodes().item(i), padding + "-");
            }
        }
        return;
    }

    /**
     * Create XML node namely "symbolnode" for given recognized symbol using given
     * XML document object. This method set "identity" and "id" atrribute
     * for newly created node.
     * @param recSymbol RecognizedSymbol for node creation.
     * @param xmlDoc XML document to create the node from.
     * @return XML Element object.
     * @see Document
     * @see RecognizedSymbol
     */
    private Element createSymbolNode(RecognizedSymbol recSymbol, Document xmlDoc) {
        Element node = xmlDoc.createElement("symbolnode");
        node.setAttribute("identity", recSymbol.getSymbolCharString()); //set symbol char
        node.setAttribute("id", String.valueOf(recSymbol.getId()));   //set id

        String[] positions = {"presuperscript",
                "above", "superscript", "row", "subscript", "below",
                "presubscript", "row", "inside"};
        for (int i = 0; i < 9; i++) {
            Node temp = xmlDoc.createElement("equation");
            ((Element) temp).setAttribute("type", positions[i]);
            node.appendChild(temp);
        }
        recSymbol.setNode(node);
        return node;
    }


    //set baseLine after clear
    public void setBaseLine(List<Baseline> baseLineList) {
        this.baseLine = baseLineList;
    }
    //detect baseline for recognized sybmbols

    public void resetFlags() {
        fracHandling = false;
        sqrtHandling.clear();
        matrixFound = false;
        matrixHandling = false;
    }

    private void doBaseLine(List<RecognizedSymbol> recognizedSymbolList, RecognizedSymbol lastRecSymbol, Document rawExpressionTree) {
        boolean testRow = false;
        boolean symbolHandled = false;
        Element newLastRecSymbolNode = createSymbolNode(lastRecSymbol, rawExpressionTree);

        for (int i = 0; i < baseLine.size(); i++) {
            List<RecognizedSymbol> baseLineNodeList = baseLine.get(i).getSymbolList();
            RecognizedSymbol closestSymbol = (RecognizedSymbol) (this.findClosest(baseLineNodeList, lastRecSymbol));
            int pos = boundingBoxDetermination(closestSymbol, lastRecSymbol);
            //RecognizedSymbol relatedSymbol = findRelatedSymbol(recognizedSymbolList, lastRecSymbol);
            //Node equation = relatedSymbol.getNode().getChildNodes().item(pos);

            //remove ?
            //TODO:fraction have abit of problem the minus sign and fraction
            if (fracHandling){
                Node equation = closestSymbol.getNode().getChildNodes().item(BELOW);
                if (equation.hasChildNodes() && ((Element) (equation.getFirstChild())).getAttribute("identity").equals("?")){
                    equation.removeChild(equation.getFirstChild());
                    equation.appendChild(newLastRecSymbolNode);
                    baseLine.get(i).addSymbol(lastRecSymbol);
                    baseLine.add(new Baseline(lastRecSymbol, center(lastRecSymbol)));
                    if (lastRecSymbol.getSymbolChar() =='\u221a' ) {
                        sqrtHandling.add(lastRecSymbol.getId());
                        Element sqr = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                        newLastRecSymbolNode.getChildNodes().item(INSIDE).appendChild(sqr);
                    }
                    return;
                }
            }
            if (!sqrtHandling.isEmpty()) {
                /**
                 * check if the new symbol is out side of sqrt box.
                 * if true, off sqrt handing and continue
                 * if new symbol still inside, add inside the sqrt
                 */
                Box sqrtBox = getBoxByID(recognizedSymbolList, sqrtHandling.get(sqrtHandling.size()-1));
                if (!checkOutside(sqrtBox, lastRecSymbol)) {
                    RecognizedSymbol relatedSymbol = getSymbolByID(recognizedSymbolList, sqrtHandling.get(sqrtHandling.size() - 1));
                    Node equation = relatedSymbol.getNode().getChildNodes().item(INSIDE);
                    //TODO: error when square  root is not the first in nested`also in nested
                    if (equation.hasChildNodes() && ((Element) (equation.getFirstChild())).getAttribute("identity").equals("?")) {
                        equation.removeChild(equation.getFirstChild());
                        equation.appendChild(newLastRecSymbolNode);
                        baseLine.add(new Baseline(lastRecSymbol, center(lastRecSymbol)));
                        baseLine.get(i).addSymbol(lastRecSymbol);
                        if (lastRecSymbol.getSymbolChar() =='\u221a' ) {
                            sqrtHandling.add(lastRecSymbol.getId());
                            Element sqr = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                            newLastRecSymbolNode.getChildNodes().item(INSIDE).appendChild(sqr);
                        }
                        return;
                    }
                } else{  // add as row
                    closestSymbol = getSymbolByID(recognizedSymbolList, sqrtHandling.get(sqrtHandling.size() - 1));
                    pos = boundingBoxDetermination(closestSymbol, lastRecSymbol);
                    sqrtHandling.remove(sqrtHandling.size()-1);} //end of squareroot relation
            }//fall thru as symbol not added


            if (Math.abs(pos) == ROW && !symbolHandled) {
                if (checkSameLine(recognizedSymbolList, lastRecSymbol, closestSymbol)) {
                    Node symbolParentNode = baseLine.get(i).getSymbol().getNode().getParentNode(); //equation node
                    if (symbolParentNode.getNodeName().equals("matrixOB")) {
                        //need to write a method to solve this
                        symbolParentNode = symbolParentNode.getParentNode().getParentNode();
                    }

                    symbolHandled = true;
                    testRow = true;
                    baseLine.get(i).addSymbol(lastRecSymbol);

                    //handle matrix element added in existing row
                    if (matrixHandling == true && (closestSymbol.getNode().getParentNode().getParentNode().getNodeName().equals("matrixrow") || closestSymbol.getNode().getParentNode().getNodeName().equals("matrixOB"))) {
                        if (SymbolClassifier.oneExpression(pos, closestSymbol, lastRecSymbol)) { //existing element entry
                            Node parentNodeOfClosestSymbol = closestSymbol.getNode().getParentNode();
                            parentNodeOfClosestSymbol.appendChild(newLastRecSymbolNode);
                        } //matrix close bracket
                        else if (closeFences.indexOf(lastRecSymbol.getSymbolCharString()) > -1) {
                            matrixHandling = false;
                            Node refMatrixRoot = closestSymbol.getNode().getParentNode().getParentNode();
                            Node newCloseBraceNode = rawExpressionTree.createElement("matrixCB");
                            refMatrixRoot.appendChild(newCloseBraceNode);
                            newCloseBraceNode.appendChild(newLastRecSymbolNode);

                            //if the equation is not a matrix
                            if (!matrixFound) {
                                Node parentNodeOfMatrix = newCloseBraceNode.getParentNode().getParentNode();
                                Node matrixRoot = newCloseBraceNode.getParentNode();
                                NodeList sonOfMatrix = matrixRoot.getChildNodes();

                                Node openBracket = sonOfMatrix.item(0).getFirstChild();
                                Node equationNode = sonOfMatrix.item(1).getFirstChild().getFirstChild(); //the only 1 entry
                                Node closeBracket = sonOfMatrix.item(2).getFirstChild();

                                parentNodeOfMatrix.removeChild(matrixRoot);
                                parentNodeOfMatrix.appendChild(openBracket);
                                parentNodeOfMatrix.appendChild(equationNode);
                                parentNodeOfMatrix.appendChild(closeBracket);
                            }
                        } else {
                            //new matrix element input
                            matrixFound = true;
                            Node parentNodeOfClosestSymbol = closestSymbol.getNode().getParentNode().getParentNode();
                            Node newEquationNode = rawExpressionTree.createElement("equation");
                            setMatrixElementDistance(this.getDistance(closestSymbol, lastRecSymbol));
                            parentNodeOfClosestSymbol.appendChild(newEquationNode);
                            newEquationNode.appendChild(newLastRecSymbolNode);
                            //check for empty column
                            NodeList temp = parentNodeOfClosestSymbol.getChildNodes();
                            for (int j = 0; j < temp.getLength(); j++) {
                                if (!temp.item(j).hasChildNodes()) {
                                    parentNodeOfClosestSymbol.removeChild(temp.item(j));
                                }
                            }
                        }
                    } //handle normal row alignment case
                    else if (checkRelativePos(closestSymbol, lastRecSymbol) == -1) { //newSymbol at left
                        symbolParentNode.insertBefore(newLastRecSymbolNode, closestSymbol.getNode());
                    } else {          //newSymbol at right
                        //Node rightHandNode = findRightNode(baseLineNodeList, closestSymbol, lastRecSymbol);
                        //if (rightHandNode != null) {
                        //    //error here
                        //    symbolParentNode.insertBefore(newLastRecSymbolNode, rightHandNode);
                        //} else {
                            symbolParentNode.appendChild(newLastRecSymbolNode);
                        //}
                        if (lastRecSymbol.getSymbolChar() =='\u221a' ) {
                            sqrtHandling.add(lastRecSymbol.getId());
                            Element sqr = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                            newLastRecSymbolNode.getChildNodes().item(INSIDE).appendChild(sqr);
                        }
                    }

                    //handle fraction case e.g. 2^3 and lastRecSymbol is -
                    if (lastRecSymbol.getSymbolChar() == '\u2212' && closestSymbol.getNode().getChildNodes().item(SUPER_SCRIPT).hasChildNodes()) {
                        Node equationNodeOf2ndLastSymbol = closestSymbol.getNode().getChildNodes().item(SUPER_SCRIPT);
                        List<RecognizedSymbol> rc = findBaseLineSymbols(equationNodeOf2ndLastSymbol.getFirstChild());
                        for (int k = 0; k < rc.size(); k++) {
                            if (boundingBoxDetermination(rc.get(k), lastRecSymbol) == BELOW) {
                                Node node = rc.get(k).getNode();
                                equationNodeOf2ndLastSymbol.removeChild(node);
                                newLastRecSymbolNode.getChildNodes().item(ABOVE).
                                        insertBefore(node, newLastRecSymbolNode.getChildNodes().item(ABOVE).getFirstChild());
                                //handle the empty denominator
                                if (newLastRecSymbolNode.getChildNodes().item(BELOW).getChildNodes().getLength() == 0) {
                                    Element denominator = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                                    newLastRecSymbolNode.getChildNodes().item(BELOW).appendChild(denominator);
                                }
                            }
                        }
                    }
                }
                //------------------------------------ 2009.09.08
            }
        } //end for

        if (testRow == false) {
            //find it's closest and MST symbol and determine the relationship
            RecognizedSymbol relatedSymbol = findRelatedSymbol(recognizedSymbolList, lastRecSymbol);
            int pos = boundingBoxDetermination(relatedSymbol, lastRecSymbol);
            //if (SymbolClassifier.oneExpression(pos, relatedSymbol, lastRecSymbol) || matrixHandling) {
            //if (rightPos(relatedSymbol, lastRecSymbol, recognizedSymbolList)) {

            //handle fraction
            //fraction to handle inside
            if ((pos == 5) && lastRecSymbol.getSymbolChar() == '\u2212') {
                fracHandling = true;

                //handle complex numerator, e.g. a2
                Node parentOfRelatedSymbol = relatedSymbol.getNode().getParentNode();
                while (!parentOfRelatedSymbol.getNodeName().equals("rootequation")) {
                    parentOfRelatedSymbol = parentOfRelatedSymbol.getParentNode();
                    if (parentOfRelatedSymbol.getNodeName().equals("symbolnode")) {
                        RecognizedSymbol parentSymbol = (RecognizedSymbol) recognizedSymbolList.get(Integer.parseInt(((Element) parentOfRelatedSymbol).getAttribute("id")));
                        if (boundingBoxDetermination(parentSymbol, lastRecSymbol) == pos) {
                            if (parentSymbol.getSymbolChar() == '\u2212' && parentSymbol.getBox().getWidth() > lastRecSymbol.getBox().getWidth()) {
                                break;
                            }
                            relatedSymbol = parentSymbol;
                        } else {
                            break;
                        }
                    }
                }
                //-------------------------
                Node equationNodeOf2ndLastSymbol = relatedSymbol.getNode().getParentNode();
                Element frac = newLastRecSymbolNode;
                NodeList nodeList = relatedSymbol.getNode().getParentNode().getChildNodes();
                RecognizedSymbol rc = (RecognizedSymbol) recognizedSymbolList.get(Integer.parseInt(((Element) nodeList.item(nodeList.getLength() - 1)).getAttribute("id")));
                while (rc != null && boundingBoxDetermination(rc, lastRecSymbol) == pos) {
                    Node node = rc.getNode();
                    Element preSibling = (Element) (node.getPreviousSibling());
                    equationNodeOf2ndLastSymbol.removeChild(node);

                    frac.getChildNodes().item(ABOVE).
                            insertBefore(node, frac.getChildNodes().item(ABOVE).getFirstChild());
                    //handle the empty denominator
                    if (frac.getChildNodes().item(BELOW).getChildNodes().getLength() == 0) {
                        Element denominator = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                        frac.getChildNodes().item(BELOW).appendChild(denominator);
                    }
                    if (preSibling == null) {
                        rc = null;
                    } else {
                        rc = (RecognizedSymbol) recognizedSymbolList.get(Integer.parseInt(preSibling.getAttribute("id")));
                    }
                }
                equationNodeOf2ndLastSymbol.appendChild(frac);
            }

            //handle matrix found 2009.09.08
            else if (openFences.indexOf(relatedSymbol.getSymbolCharString()) > -1 && pos == SUPER_SCRIPT) {
                matrixHandling = true;
                Node matrixRoot = rawExpressionTree.createElement("matrix");
                Node openBraceRootNode = rawExpressionTree.createElement("matrixOB");
                Node matrixRow = rawExpressionTree.createElement("matrixrow");

                //remove [ from existing tree
                Node eqNodeOf2ndLastSymbol = relatedSymbol.getNode().getParentNode();
                eqNodeOf2ndLastSymbol.removeChild(relatedSymbol.getNode());
                //add Openbrace in Matrix tree
                matrixRoot.appendChild(openBraceRootNode);
                openBraceRootNode.appendChild(relatedSymbol.getNode());
                //add New Row and New cell
                matrixRoot.appendChild(matrixRow);
                matrixRow.appendChild(rawExpressionTree.createElement("equation"));
                matrixRow.getFirstChild().appendChild(newLastRecSymbolNode);

                //append to expression tree
                eqNodeOf2ndLastSymbol.appendChild(matrixRoot);
            }
            //handle new matrix row
            else if ((pos == BELOW || pos == PRE_SUB_SCRIPT) && matrixHandling && !SymbolClassifier.oneExpression(pos, relatedSymbol, lastRecSymbol)) {
                Node refMatrixRoot = relatedSymbol.getNode().getParentNode().getParentNode().getParentNode();
                while (!refMatrixRoot.getNodeName().equals("matrix")) {
                    refMatrixRoot = refMatrixRoot.getParentNode();
                }
                //create new row
                Node newMatrixRow = rawExpressionTree.createElement("matrixrow");
                Node newEqRow = rawExpressionTree.createElement("equation");
                //add cell symbol
                newEqRow.appendChild(newLastRecSymbolNode);
                newMatrixRow.appendChild(newEqRow);
                refMatrixRoot.appendChild(newMatrixRow);
            } //normal case
            else {
                //remove "?"

                Node equation = relatedSymbol.getNode().getChildNodes().item(pos);
                if (equation.hasChildNodes() && ((Element) (equation.getFirstChild())).getAttribute("attribute").equals("")) {
                    equation.removeChild(equation.getFirstChild());
                }
                equation.appendChild(newLastRecSymbolNode);
                if (lastRecSymbol.getSymbolChar() =='\u221a' ) {
                    sqrtHandling.add(lastRecSymbol.getId());
                    Element sqr = createSymbolNode(new RecognizedSymbol('?'), rawExpressionTree);
                    newLastRecSymbolNode.getChildNodes().item(INSIDE).appendChild(sqr);
                }
            }
            //add a new baseLine
            baseLine.add(new Baseline(lastRecSymbol, center(lastRecSymbol)));

            //matrixDetectStep(lastRecSymbol, mdt, rawExpressionTree, recognizedSymbolList);
        }

    }
    // ArrayList ancestorList = getAncestor(secondLastRecSymbol.getNode());


    //return relatedSymbol of lastRecSymbol
    private RecognizedSymbol findRelatedSymbol(List<RecognizedSymbol> recognizedSymbolList, RecognizedSymbol lastRecSymbol) {
        RecognizedSymbol closestSymbol = (RecognizedSymbol) (findClosest(recognizedSymbolList, lastRecSymbol));
        List<RecognizedSymbol> templist = findMSTSymbols(recognizedSymbolList, lastRecSymbol);
        List<RecognizedSymbol> mstSymbols = new ArrayList();
        for (int i = 0; i < templist.size(); i++) {
            if (!checkIntersect(recognizedSymbolList, lastRecSymbol, templist.get(i))) {
                mstSymbols.add(templist.get(i));
            }
        }
        List<RecognizedSymbol> tempList = new ArrayList();

//        double distance = getDistance(closestSymbol, lastRecSymbol);
//        double d2;
//        for (int i = 0; i < mstSymbols.size(); i++) {
//            System.out.println("------------------------" + mstSymbols.get(i).getSymbolCharString());
//            System.out.println(getDistance(mstSymbols.get(i), lastRecSymbol));
//            d2 = getDistance(mstSymbols.get(i), lastRecSymbol);
//            if (d2>Math.sqrt(Math.pow(lastRecSymbol.getBox().getHeight(),2)+Math.pow(lastRecSymbol.getBox().getWidth(),2)) ){
//                mstSymbols.remove(mstSymbols.get(i));
//            }
//        }

        //handle new column condition in matrix
        for (int j = 0; j < mstSymbols.size(); j++) {
            if (openFences.indexOf(mstSymbols.get(j).getSymbolCharString()) > -1 && matrixHandling == true) {
                RecognizedSymbol matrixElement = get1stElement(recognizedSymbolList, mstSymbols.get(j));
                if (!mstSymbols.contains(matrixElement)) {
                    mstSymbols.add(matrixElement);
                }
                //remove irrelavent symbol such as 2 in a2
                for (int i = 0; i < mstSymbols.size(); i++) {
                    if (!mstSymbols.get(i).equals(matrixElement) && matrixElement.getNode().equals(findParentNode(mstSymbols.get(i)))) {
                        mstSymbols.remove(mstSymbols.get(i));
                    }
                }
            }
        }
        //put closestSymbol at first
        if (!mstSymbols.get(0).equals(closestSymbol)) {
            mstSymbols.remove(closestSymbol);
            mstSymbols.add(0, closestSymbol);
        }

        if (mstSymbols.size() > 1) {
            //compare with all the mst symbols
            //get the related symbol which is the lowest symbol in expression tree
            int largest = 0;
            for (int j = 0; j < mstSymbols.size(); j++) {
                int count = 0;
                Node m = mstSymbols.get(j).getNode();
                while (!m.getParentNode().getNodeName().equals("rootequation")) {
                    m = m.getParentNode();
                    count++;
                }
                if (largest < count) {
                    RecognizedSymbol temp = mstSymbols.get(j);
                    mstSymbols.remove(mstSymbols.get(j));
                    mstSymbols.add(0, temp);
                    largest = count;
                }
            }
            for (int j = 0; j < mstSymbols.size(); j++) {
                tempList.add(mstSymbols.get(j));
            }
            for (int i = 0; i < tempList.size(); i++) {
                int pos = boundingBoxDetermination(tempList.get(i), lastRecSymbol);
                //handle exception: new matrix column
                if (pos == BELOW && matrixHandling == true) {
                    for (int j = 0; j < tempList.size(); j++) {
                        if (openFences.indexOf(tempList.get(j).getSymbolCharString()) > -1) {
                            return tempList.get(i);
                        }
                    }
                }
                if (!SymbolClassifier.oneExpression(pos, tempList.get(i), lastRecSymbol)) {
                    mstSymbols.remove(tempList.get(i));
                    mstSymbols.add(tempList.get(i)); //put it at the end
                }
            }
        }
        return mstSymbols.get(0);
    }

    //get set the distance between 2 matrix element

    protected static double getMatrixElementDistance() {
        return elementDistance;
    }

    protected void setMatrixElementDistance(double distance) {
        elementDistance = distance;
    }

    protected static boolean getMatrixHandling() {
        return matrixHandling;
    }


    //return 1st matrix element of the row
    private RecognizedSymbol get1stElement(List<RecognizedSymbol> list, RecognizedSymbol symbol) {
        Node parentNode = symbol.getNode().getParentNode().getParentNode();
        NodeList tempList = parentNode.getChildNodes();
        //remove empty column
        for (int j = 0; j < tempList.getLength(); j++) {
            if (!tempList.item(j).getFirstChild().hasChildNodes()) {
                parentNode.removeChild(tempList.item(j));
            }
        }
        Node temp = tempList.item(tempList.getLength() - 1);
        if (temp.getNodeName().equals("matrixrow")) {
            temp = temp.getFirstChild().getFirstChild();
        }
        if (temp.getNodeName().equals("symbolnode")) {
            return getSymbolFromNode(list, temp);
        }
        return null;
    }


    //return parent symbol node
    private Node findParentNode(RecognizedSymbol symbol) {
        Node temp = symbol.getNode().getParentNode();
        while (temp != null) {
            if (temp.getNodeName().equals("matrixrow")) {
                return temp.getFirstChild().getFirstChild();
            }
            temp = temp.getParentNode();
        }
        return null;
    }


    //verify if the 2 symbols that in row relations are in the same baseLine
    private boolean checkSameLine(List<RecognizedSymbol> symbolList, RecognizedSymbol lastRecSymbol, RecognizedSymbol baseLineSymbol) {
        Node tempNode = baseLineSymbol.getNode();
        if (tempNode.getParentNode().getNodeName().equals("rootequation")) {
            return true; //1 level expression
        } else if (tempNode.getParentNode().getNodeName().equals("matrixCB")) {
            return true; //matrix finished
        } else if (tempNode.getParentNode().getParentNode().getNodeName().equals("matrixrow") && !checkIntersect(symbolList, lastRecSymbol, baseLineSymbol)) {
            return true; //matrix element
        } else if (tempNode.getParentNode().getNodeName().equals("matrixOB") && matrixHandling && closeFences.indexOf(lastRecSymbol.getSymbolCharString()) > -1) {
            return true; //matrix close bracket and open bracket
        } else if (tempNode.getParentNode().getParentNode().getNodeName().equals("symbolnode")) {
            String id = ((Element) (tempNode.getParentNode().getParentNode())).getAttribute("id");
            RecognizedSymbol selectedAncestorSymbol = (RecognizedSymbol) symbolList.get(Integer.parseInt(id));
            if (boundingBoxDetermination(selectedAncestorSymbol, lastRecSymbol) == boundingBoxDetermination(selectedAncestorSymbol, baseLineSymbol)) {
                double parentCenterX = 0;
                double lastCenterX = ((StrokePoint) center(lastRecSymbol)).X;
                double baseCenterX = ((StrokePoint) center(baseLineSymbol)).X;
                List<RecognizedSymbol> parentBaseLineSymbols = findBaseLineSymbols(baseLineSymbol.getNode().getParentNode().getParentNode());
                if (parentBaseLineSymbols.size() == 1 && isFracOrVector(parentBaseLineSymbols.get(0))) {
                    return true;
                }
                for (int i = 0; i < parentBaseLineSymbols.size(); i++) {
                    parentCenterX = ((StrokePoint) center(parentBaseLineSymbols.get(i))).X;
                    if ((parentCenterX - lastCenterX) * (parentCenterX - baseCenterX) < 0) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isFracOrVector(RecognizedSymbol symbol) {
        if (symbol.getSymbolChar() == '\u2212') {
            return true;
        } else if (symbol.getSymbolChar() == '\u2192') {
            return true;
        } else {
            return false;
        }
    }

    //check if the last symbol and closest symbol intersect with other symbols

    private boolean checkIntersect(List<RecognizedSymbol> symbolList, RecognizedSymbol lastRecSymbol, RecognizedSymbol baseLineSymbol) {
        List<RecognizedSymbol> temp = new ArrayList(symbolList);
        temp.remove(lastRecSymbol);
        temp.remove(baseLineSymbol);
        StrokePoint center1 = center(lastRecSymbol);
        StrokePoint center2 = center(baseLineSymbol);
        for (int i = 0; i < temp.size(); i++) {
            StrokeList sl = ((RecognizedSymbol) (temp.get(i))).getStrokes();
            if (intersect(center1, center2, sl)) {
                return true;
            }
        }
        return false;
    }


    //find the next node that in the same baseLine
    private Node findRightNode(List<RecognizedSymbol> symbolList, RecognizedSymbol closeSymbol, RecognizedSymbol last) {
        int count = 0;
        for (int i = 0; i < symbolList.size(); i++) {
            if (closeSymbol.equals(symbolList.get(i))) {
                count = i;
                break;
            }
        }
        if ((count + 1) < symbolList.size() && !symbolList.get(count + 1).equals(last)) {
            return symbolList.get(count + 1).getNode();
        } else {
            return null;
        }
    }


    //find the closest symbol of the last recognized symbol
    private RecognizedSymbol findClosest(List<RecognizedSymbol> symbolList, RecognizedSymbol Last) {
        RecognizedSymbol closestSymbol = symbolList.get(0);

        double distance = Integer.MAX_VALUE;
        double d, dx, dy;
        for (int i = 0; i < symbolList.size(); i++) {
            /**
            if (!sqrtHandling.isEmpty() && symbolList.get(i).getSymbolChar() == '\u221a'){
                return symbolList.get(i);
            }*/
            dx = ((StrokePoint) center(Last)).X - ((StrokePoint) center(symbolList.get(i))).X;
            dy = ((StrokePoint) center(Last)).Y - ((StrokePoint) center(symbolList.get(i))).Y;
            d = Math.sqrt(dx * dx + dy * dy);
            if (d < distance && d != 0) {
                closestSymbol = symbolList.get(i);
                distance = d;
            }
        }
        return closestSymbol;
    }


//return the distance between 2 symbols based on symbol border
    private double getDistance(RecognizedSymbol symbolA, RecognizedSymbol lastSymbol) {
        Box aR = symbolA.getBox();
        Box lastR = lastSymbol.getBox();

        StrokePoint aPt = (StrokePoint) center(symbolA);
        StrokePoint lastPt = (StrokePoint) center(lastSymbol);

        double xIndi, yIndi;
        double aBorderX, aBorderY, lastBorderX, lastBorderY;
        if (lastR.getCenterX() > aR.getCenterX()) {
            xIndi = 0.5;
        } else {
            xIndi = -0.5;
        }

        if (lastR.getCenterY() > aR.getCenterY()) {
            yIndi = 0.5;
        } else {
            yIndi = -0.5;
        }

        StrokePoint aCorner = new StrokePoint(((int) (aR.getCenterX() + xIndi * aR.width)), ((int) (aR.getCenterY() + yIndi * aR.height)));
        StrokePoint lastCorner = new StrokePoint(((int) (lastR.getCenterX() - xIndi * lastR.width)), ((int) (lastR.getCenterY() - yIndi * lastR.height)));

        double y;
        if (lastPt.X == aPt.X) {
            aBorderX = lastBorderX = aPt.X;
            aBorderY = aR.y + aR.height * yIndi * 2;
            lastBorderY = lastR.y - lastR.height * yIndi * 2;
        } else if (lastPt.Y == aPt.Y) {
            aBorderY = lastBorderY = aPt.Y;
            aBorderX = aR.x + aR.width * xIndi * 2;
            lastBorderX = lastR.x - lastR.width * xIndi * 2;
        } else {
            y = (aCorner.X - aPt.X) * 1.0 / (lastPt.X - aPt.X) * (lastPt.Y - aPt.Y) + aPt.Y;
            if ((y <= aR.y + aR.height) && (y >= aR.y)) {
                aBorderX = aCorner.X;
                aBorderY = y;
            } else {
                aBorderY = aCorner.Y;
                aBorderX = (aCorner.Y - aPt.Y) * 1.0 / (lastPt.Y - aPt.Y) * (lastPt.X - aPt.X) + aPt.X;
            }
            y = (lastCorner.X - aPt.X) * 1.0 / (lastPt.X - aPt.X) * (lastPt.Y - aPt.Y) + aPt.Y;
            if ((y <= lastR.y + lastR.height) && (y >= lastR.y)) {
                lastBorderX = lastCorner.X;
                lastBorderY = y;
            } else {
                lastBorderY = lastCorner.Y;
                lastBorderX = (lastCorner.Y - aPt.Y) * 1.0 / (lastPt.Y - aPt.Y) * (lastPt.X - aPt.X) + aPt.X;
            }
        }
        return Math.sqrt(Math.pow(lastBorderX - aBorderX, 2) + Math.pow(lastBorderY - aBorderY, 2));
//        double dx = lastPt.X - aPt.X - (symbolA.getBox().width + lastSymbol.getBox().width) / 2;
//        double dy = lastPt.Y - aPt.Y - (symbolA.getBox().height + lastSymbol.getBox().height) / 2;
//        return Math.sqrt(dx * dx + dy * dy);
    }

    private int checkRelativePos(RecognizedSymbol oldSymbol, RecognizedSymbol lastSymbol) {
        if (boundingBoxDetermination(oldSymbol, lastSymbol) == ROW) {
            return 1; //newSymbol at right
        } else if (boundingBoxDetermination(oldSymbol, lastSymbol) == ROW_BEFORE) {
            return -1; //newSymbol at left
        } else {
            return 0; //error
        }
    }


    private int boundingBoxDeterminationForShortSymbol(RecognizedSymbol secondLastRecSymbol, RecognizedSymbol lastRecSymbol) {
        Box lastBBox = lastRecSymbol.getBox();
        StrokePoint lastCenter = center(lastRecSymbol);
        Box secondLastBBox = secondLastRecSymbol.getBox();
        StrokePoint secondLastCenter = center(secondLastRecSymbol);
        //Find angle degree between two Center points.
        if (secondLastRecSymbol.getSymbolChar() == '.'){
            return ROW;
        }

        if (secondLastBBox.x <= lastBBox.x + 0.4 * lastBBox.width && secondLastBBox.x + secondLastBBox.width >= lastBBox.x + lastBBox.width * 0.6) {
            if (lastCenter.Y >= secondLastCenter.Y) {
                return BELOW; // above 5
            } else {
                return ABOVE;//1
            }	//below
        } else if (lastBBox.y - 0.2 * lastBBox.height >= secondLastBBox.y + secondLastBBox.height) {
            if (lastRecSymbol.getSymbolChar() == '.'){
                return ROW;
            }
            return SUB_SCRIPT; // above 5
        } else if (lastBBox.y + 0.8 * lastBBox.height <= secondLastBBox.y) {
            return SUPER_SCRIPT;//1
        } //below
        else if (lastCenter.X >= secondLastCenter.X) {
            return ROW; //3
        } else {
            return ROW_BEFORE;
        }
    }

    private int boundingBoxDeterminations(Box secondLastBBox, StrokePoint secondLastCenter,
                                         RecognizedSymbol lastRecSymbol) {

        Box lastBBox = lastRecSymbol.getBox();
        StrokePoint lastCenter = center(lastRecSymbol);
        //Find angle degree between two Center points.
        double angle = getAngle(secondLastCenter, lastCenter);

        //assuming second last symbol is sqrt
        //the 1.1 width of the symbol doesn't exceed the sqrt width
        //and the height doesn't exceed more than 1.1 times of the sqrt
        if (secondLastBBox.getX()+secondLastBBox.getWidth() >= lastBBox.getX() + lastBBox.getWidth()*1.1 &&
                secondLastBBox.getY()+ 1.1 * secondLastBBox.getHeight() >= lastBBox.getY() + lastBBox.getHeight()){
            return INSIDE;
        }

        if (angle >= -Math.PI / 8 && angle <= Math.PI / 8) { //22.5
            //When absolute distance between tow centers is lesser than
            //1/4 of possible maximum 2nd last symbol height/width or
            //last symbol height/width. It is determined as SAME ROW.
            if (Math.abs(lastCenter.Y - secondLastCenter.Y) <= Math.max(Math.max((double) secondLastBBox.getHeight() / 4,
                    (double) lastBBox.getHeight() / 4),
                    Math.max((double) secondLastBBox.getWidth() / 4,
                            (double) lastBBox.getWidth() / 4))) {
                if (lastCenter.X >= secondLastCenter.X) {
                    return ROW; //3
                } else {
                    return ROW_BEFORE;
                }
            }
        }
        // When [last box height 80% is outside in above of 2nd last box's Y OR
        // 2nd last box height 80% is outside in above of last box Y] AND
        // {[20% of last box is outside in left side of 2nd last box AND
        // 20% of last box is outside in right side of 2nd last box] OR
        // [20% of second last box is outside in left side of last box AND
        //  20% of second last box is outside in right side of last box]}
        // It is to decide ABOVE or BELOW.
        // When last box's center Y is lower (Y value greater) than
        // 2nd last box's center. Second Last Symbol is ABOVE the Last Symbol.
        // other wise. Second Last Symbol is BELOW the Last Symbol.
        if ((lastBBox.getY() + 0.8 * lastBBox.getHeight() <= secondLastBBox.getY() || secondLastBBox.getY() + 0.8 * secondLastBBox.getHeight() <= lastBBox.getY()) && ((lastBBox.getX() - 0.2 * lastBBox.getWidth() <= secondLastBBox.getX() && lastBBox.getX() + 1.2 * lastBBox.getWidth() >= secondLastBBox.getX() + secondLastBBox.getWidth()) || (secondLastBBox.getX() - 0.2 * secondLastBBox.getWidth() <= lastBBox.getX() && secondLastBBox.getX() + 1.2 * secondLastBBox.getWidth() >= lastBBox.getX() + lastBBox.getWidth()))) {
            if (lastCenter.Y >= secondLastCenter.Y) {
                return BELOW; // above 5
            } else {
                return ABOVE;//1
            }	//below
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
                        System.out.println("bb:5");
                        if (lastRecSymbol.getSymbolChar() == '.'){
                            return ROW;
                        }
                        return SUB_SCRIPT;//sub script 4
                    } else {
                        System.out.println("bb:6");
                        return SUPER_SCRIPT;//super script 2
                    }
                } else if (angle > Math.PI / 8 && angle < 3 * Math.PI / 8) {
                    System.out.println("bb:7");
                    if (lastRecSymbol.getSymbolChar() == '.'){
                        return ROW;
                    }
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


    //get all the recognizedSymbols on the same baseLine
    private List<RecognizedSymbol> findBaseLineSymbols(Node nodeL) {
        for (int i = 0; i < baseLine.size(); i++) {
            List<RecognizedSymbol> symbolList = baseLine.get(i).getSymbolList();
            for (int j = 0; j < symbolList.size(); j++) {
                if (symbolList.get(j).getNode().equals(nodeL)) {
                    return symbolList;
                }
            }
        }
        return null;
    }


    //get RecognizedSymbol based on the node
    private RecognizedSymbol getSymbolFromNode(List<RecognizedSymbol> symbolList, Node node) {
        String id = ((Element) (node)).getAttribute("id");
        return (RecognizedSymbol) symbolList.get(Integer.parseInt(id));
    }


    //find out the MST for the recognizedSymbol
    //1. Directed connected symbols.
    //2. Symbols that next to connected Symbols and angle between them and lastSymbol is less than 90
    public List<RecognizedSymbol> findMSTSymbols(List<RecognizedSymbol> symbolList, RecognizedSymbol last) {
        int symbolNO = symbolList.size();
        double d;
        double minimumm;
        int numberinst = 1;
        List<StrokePoint> sp = new ArrayList();
        boolean visited[] = new boolean[symbolNO];
        int current[] = new int[symbolNO];
        double D[][] = new double[symbolNO][symbolNO];
        int selstart = 0;
        int selend = 0;
        RecognizedSymbol[][] pointLists = new RecognizedSymbol[2][symbolNO - 1];
        List<RecognizedSymbol> mstSymbols = new ArrayList(); //symbols that directly connected to lastSymbol
        List<RecognizedSymbol> connectedSymbols = new ArrayList(); //all symbols that qualified
        int count = 0;
        RecognizedSymbol tempSymbol;

        for (int i = 0; i < symbolNO; i++) {
            for (int j = 0; j < symbolNO; j++) {
                D[i][j] = 0;
            }
        }
        for (int i = 0; i < symbolNO; i++) {
            visited[i] = false;
            sp.add(center(symbolList.get(i)));
        }

        current[0] = 0;
        visited[0] = true;

        for (int i = 0; i < symbolNO; i++) {
            for (int j = 0; j < symbolNO; j++) {
                if (j != i) {
                    d = (double) (Math.pow(sp.get(i).X - sp.get(j).X, 2) + Math.pow((sp.get(i).Y - sp.get(j).Y), 2));
                    D[i][j] = Math.sqrt(d);
                }
            }
        }

        for (int k = 0; k < symbolNO - 1; k++) {
            minimumm = Integer.MAX_VALUE;
            for (int l = 0; l < numberinst; l++) {
                for (int j = 0; j < symbolNO; j++) {
                    if (visited[j] != true) {
                        if (minimumm > D[current[l]][j]) {
                            minimumm = D[current[l]][j];
                            selend = j;
                            selstart = current[l];
                        }
                    }
                }
            }
            visited[selend] = true;
            pointLists[0][k] = getSymbolsFromCenter(symbolList, sp.get(selstart));
            pointLists[1][k] = getSymbolsFromCenter(symbolList, sp.get(selend));
            //distance += (int) D[selstart][selend];
            current[numberinst++] = selend;
        }

        for (int i = 0; i < pointLists[0].length; i++) {
            boolean newFound = false;
            if (last.equals(pointLists[0][i])) {
                mstSymbols.add(pointLists[1][i]);
                connectedSymbols.add(pointLists[1][i]);
                newFound = true;
                count++;

            } else if (last.equals(pointLists[1][i])) {
                mstSymbols.add(pointLists[0][i]);
                connectedSymbols.add(pointLists[0][i]);
                newFound = true;
                count++;
            }

            if (newFound) {
                for (int j = 0; j < pointLists[0].length; j++) {
                    if (mstSymbols.get(count - 1).equals(pointLists[0][j]) && (!last.equals(pointLists[1][j]))) {
                        tempSymbol = pointLists[1][j];
                        if (calcDegree(tempSymbol, mstSymbols.get(count - 1), last)) {
                            connectedSymbols.add(tempSymbol);
                        }
                    }
                    if (mstSymbols.get(count - 1).equals(pointLists[1][j]) && (!last.equals(pointLists[0][j]))) {
                        tempSymbol = pointLists[0][j];
                        if (calcDegree(tempSymbol, mstSymbols.get(count - 1), last)) {
                            connectedSymbols.add(tempSymbol);
                        }
                    }
                }
            }
        }
        return connectedSymbols;
    }


    //find out the RecognizedSymbol based on its center
    private boolean calcDegree(RecognizedSymbol temp, RecognizedSymbol center, RecognizedSymbol last) {
        StrokePoint tempP = center(temp);
        StrokePoint centerP = center(center);
        StrokePoint lastP = center(last);
        double vectorLastX = lastP.X - centerP.X;
        double vectorLastY = lastP.Y - centerP.Y;
        double vectorTempX = tempP.X - centerP.X;
        double vectorTempY = tempP.Y - centerP.Y;

        double dotProduct = (vectorTempX * vectorLastX + vectorTempY * vectorLastY) / (Math.sqrt(Math.pow(vectorLastX, 2) + Math.pow(vectorLastY, 2)) * Math.sqrt(Math.pow(vectorTempX, 2) + Math.pow(vectorTempY, 2)));
        if (Math.acos(dotProduct) > Math.PI / 2) {
            return false;
        } else {
            return true;
        }
    }


    //find out the RecognizedSymbol based on its center
    private RecognizedSymbol getSymbolsFromCenter(List<RecognizedSymbol> symbolList, StrokePoint sp) {
        int i = 0;
        for (; i <
                symbolList.size(); i++) {
            StrokePoint s = (StrokePoint) center(symbolList.get(i));
            if (Math.abs(s.X - sp.X) < 1 && Math.abs(s.Y - sp.Y) < 1) {
                break;
            }

        }
        return symbolList.get(i);
    }
//------------------------------

    /**
     * Determine the realtional position of two recognized symbol
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
        if (secondLastRecSymbol.getSymbolChar() == '.'){
            return ROW;
        }
        //- ,= , nearly equal
//        if ((isShortSymbol(secondChar)||isShortSymbol(lastChar))&&!(isShortSymbol(secondChar)&&isShortSymbol(lastChar))) {
//            return boundingBoxDeterminationForShortSymbol(secondLastRecSymbol, lastRecSymbol);
//        }
        return boundingBoxDeterminations(secondLastBBox, secondLastCenter, lastRecSymbol);
    }

    private boolean isShortSymbol(String c){
        if(c.equals(String.valueOf('\u2212')) || c.equals(String.valueOf('\u003d')) || c.equals(String.valueOf('\u2248'))){
            return true;
        }
        return false;
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
     * This method find angle between two given points.
     * @param center1 First point for angle discovery.
     * @param center2 Second point for angle discovery.
     * @return The angle degree between two points.
     */
    private double getAngle(StrokePoint center1, StrokePoint center2) {
        double angle = Math.atan((double) (center2.Y - center1.Y) / (center2.X - center1.X));
        return angle;
    }

    /**
     * This method collects the list of ancestor Nodes
     * of a given Node. The algorithm loops and retrieves
     * parent nodes starting from given Node to the Root node which is
     * named "rootequation". The ancestor Nodes list is in
     * bottom up order but Exclusive of root node.
     * @param node The xml node to find ancestors for.
     * @return The nodes list in ArrayList object.
     * @see Node
     * @see ArrayList
     */
    private ArrayList getAncestor(Node node) {

        ArrayList result = new ArrayList();

        Node tempNode = node;
        while (tempNode != null && !tempNode.getNodeName().equals("rootequation")) {
            if (tempNode.getNodeName().equals("symbolnode")) {
                result.add(tempNode);
            }
            tempNode = tempNode.getParentNode();
        }//end while
        return result;
    }

    /**
     * This recursive methods use predefined relational positition
     * of spcific symbol to find the right position of given symbol
     * and given position.
     * @param c RecognizedSymbol for position correction.
     * @param p The original position of the RecognizedSymbol.
     * @return The new position of the symbol.
     * @see RecognizedSymbol
     */
    private int doCorrection(RecognizedSymbol c, int p) {
        if (p >= 3) {
            if (!rightPos(c, p)) {
                return doCorrection(c, p - 1);
            } else {
                return p;
            }
        } else {
            if (!rightPos(c, p)) {
                return doCorrection(c, p + 1);
            } else {
                return p;
            }
        }
    }


    //overload the rightPos function, a modified version
    private Boolean rightPos(RecognizedSymbol c, RecognizedSymbol last, int p) {
        if (p == 3) {
            return true;
        } else if (p == 0 || p == 1 || p == 5 || p == 2 || p == 4 || p == 8) {
            if (SymbolClassifier.checkPosClass(c.getSymbolCharString(), p)) {
                return true;
            } else if (SymbolClassifier.checkPosClass(last.getSymbolCharString(), p)) {
                return true;
            } //-- to handle matrix
            else if (matrixHandling && p == 5) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the Recognized symbol is at proper relational position.
     * @param c Recognized Symbol to check.
     * @param p Relational Position constant.
     * @return True if recognized symbol is at correct position, False otherwise.
     */
    private Boolean rightPos(RecognizedSymbol c, int p) {
        if (p == 3) {
            return true;
        } else if (p == 0 || p == 1 || p == 5 || p == 2 || p == 4 || p == 8) {
            if (SymbolClassifier.checkPosClass(c.getSymbolCharString(), p)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Wrapper method to check of the two given Recognized Symbols
     * are at their correct relational positions.
     * @param c2 First recognized symbol to check.
     * @param c1 Second recognized symbol to check.
     * @param list List of all recognized sybmol.
     * @return True if recognized symbol is at correct position, False otherwise.
     */
    private Boolean rightPos(RecognizedSymbol c2, RecognizedSymbol c1,
                             List<RecognizedSymbol> list) {

        int pos = boundingBoxDetermination(c2, c1);
        ArrayList<RecognizedSymbol> temp = new ArrayList(list);
        temp.remove(c2);
        temp.remove(c1);

        if (rightPos(c2, c1, pos)) {
            if (c2.getSymbolChar() == '\u221A') {
                return true;
            }
//Point center1 = center(c1);
//Point center2 = center(c2);

            StrokePoint center1 = center(c1);
            StrokePoint center2 = center(c2);
            for (int i = 0; i < temp.size(); i++) {
                StrokeList sl = ((RecognizedSymbol) (temp.get(i))).getStrokes();
                if (intersect(center1, center2, sl)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method check if there is any interaction among the strokes of
     * given strokelist.
     * @param c1 First Stroke Point to calculate.
     * @param c2 Second Stroke Point to calculate.
     * @param sl StrokeList for checking intersection.
     * @return True if there is at least 1 intersection, False otherwise.
     */
    private Boolean intersect(StrokePoint c1, StrokePoint c2, StrokeList sl) {
        ///ref http://www.geog.ubc.ca/courses/klink/gis.notes/ncgia/u32.html
        int numberOfStrokeIntersect = 0;
        double xA, yA, xB, yB, xC, yC, xD, yD, xI, yI;
        double aAB, bAB, aCD, bCD;
        xA = c1.X;
        yA = c1.Y;
        xB = c2.X;
        yB = c2.Y;
        for (int i = 0; i < sl.size(); i++) {
            for (int j = 0; j < sl.get(i).getTotalStrokePoints() - 1; j++) {
                xC = sl.get(i).getStrokePoint(j).X;
                yC = sl.get(i).getStrokePoint(j).Y;
                xD = sl.get(i).getStrokePoint(j + 1).X;
                yD = sl.get(i).getStrokePoint(j + 1).Y;
                try {
                    bAB = (yB - yA) / (xB - xA); //PoErr:AB is vertical
                    bCD = (yD - yC) / (xD - xC); //PoErr:CD is vertical
                    aAB = yA - (bAB * xA);
                    aCD = yC - (bCD * xC);

                    //call intersection point
                    xI = -(aAB - aCD) / (bAB - bCD);//PoErr:AB || CD
                    yI = aAB + (bAB * xI);
                    if ((xA - xI) * (xI - xB) >= 0 && (xC - xI) * (xI - xD) >= 0 && (yA - yI) * (yI - yB) >= 0 && (yC - yI) * (yI - yD) >= 0) {
                        //Intersection found
                        numberOfStrokeIntersect++;
                        break;//from inner loop of points
                    } else {
                    }
                } catch (Exception ex) {
                    j++;
                }
            }
        }
        return (numberOfStrokeIntersect > 0);
    }

    /**
     * This method explore the tree to identify if any of
     * the recognized symbol sequence (letter sequence) is the
     * special meaning based on defined list "groupingList".
     * @param rootEquation Root node of the tree to explore.
     */
    private void doGrouping(Node rootEquation) {
        //Looping the symbol nodes of root node
        //looping backward taking pair basis, starting from
        //last 2 symbol node pair
        for (int k = rootEquation.getChildNodes().getLength() - 2; k >= 0; k--) {

            Element lastChild = (Element) rootEquation.getChildNodes().item(k + 1);
            Element fixedLastChild = lastChild;
            String cur = "";
            int count = 0;
            boolean currentIsNumber = isNumber(cur);
            while (lastChild != null && (lastChild.equals(fixedLastChild) || !hasChild(lastChild)) && cur.length() <= maxGroupingLength) {
                String lastChar = lastChild.getAttribute("identity");
                cur = lastChar + cur;
                currentIsNumber = isNumber(cur);
                boolean found = false;

                if (groupingList.contains((Object) cur.toLowerCase())) {
                    fixedLastChild.setAttribute("identity", cur.toLowerCase());
                    if (!lastChild.equals(fixedLastChild)) {
                        Element nextChild = lastChild;
                        while (nextChild.getNextSibling() != null && !nextChild.getNextSibling().equals(fixedLastChild)) {
                            rootEquation.removeChild(nextChild.getNextSibling());
                        }
                        rootEquation.removeChild(nextChild);
                    }
                }
                boolean nextIsNumber = false;
                Element temp = (Element) lastChild.getPreviousSibling();
                if (temp != null) {
                    String tempChar = temp.getAttribute("identity");
                    if (isNumber(tempChar + cur)) {
                        nextIsNumber = true;
                    }
                }
                if (!nextIsNumber && currentIsNumber) {
                    fixedLastChild.setAttribute("identity", cur);
                    if (!lastChild.equals(fixedLastChild)) {
                        Element nextChild = lastChild;
                        while (nextChild.getNextSibling() != null && !nextChild.getNextSibling().equals(fixedLastChild)) {
                            rootEquation.removeChild(nextChild.getNextSibling());
                        }
                        rootEquation.removeChild(nextChild);
                    }
                    break;
                }
                if (found) {
                    break;
                }
                lastChild = (Element) lastChild.getPreviousSibling();
                count++;
            }//end while
        }
    }

    /**
     * Check if given string contains only numbers.
     * @param cur String to check
     * @return True if all are numbers, False otherwise.
     */
    public static boolean isNumber(String cur) {
        if (cur == null) {
            return false;
        } else {
            try {
                Double.parseDouble(cur.replaceAll("\\+", "a").replaceAll("\\-", "a").trim());
            } catch (Exception ex) {
                return false;
            }
            return true;
        }
    }

    /**
     * To check if given expression Node has children.
     * Note that this method is checking all equation node of
     * the given node if they have children.
     * @param node Node to examine.
     * @return True if node has children, False for otherwise.
     */
    private boolean hasChild(Element node) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).hasChildNodes()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the respective openbrace of given close brace.
     * @param s Close brace to search open brace.
     * @return Open brace respective to Close brace.
     */
    private String getOpen(String s) {
        if (s.equals(")")) {
            return "(";
        }
        if (s.equals("}")) {
            return "{";
        }
        if (s.equals("]")) {
            return "[";
        }
        return "";
    }

    private Box getBoxByID (List<RecognizedSymbol> list ,int index){
        Box box =new Box(0,0,0,0);
        for (int i =0; i < list.size();i++){
            if (list.get(i).getId() == index){
                box = list.get(i).getBox();
            }
        }
        return box;

    }

    private RecognizedSymbol getSymbolByID (List<RecognizedSymbol> list ,int index) {


        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == index) {
                return list.get(i);
            }
        }
        return null;
    }




    public Boolean checkOutside(Box sqrt, RecognizedSymbol lastSymbol){
        double line =  sqrt.getX() + sqrt.getWidth();
        if (line < lastSymbol.getBox().getX()){
            return true;
        }
        return false;
    }

}