package com.example.travis.ichmfapp.preprocessor;

import android.content.Context;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;
import com.example.travis.ichmfapp.symbollib.*;

import org.w3c.dom.*;

import java.util.*;

import javax.xml.parsers.*;

/**
 * Created by Travis on 31/8/2018.
 */

public class Recognizer {

    private Context context = MainActivity.getAppContext();

    SymbolLib _symbolLib;
    /// The collection of strokes drawn by user for recognition.
    /// Upon completion of drawing A new stroke, that last drawn
    /// stroke is added into this collection for processing.
    /**
     * The collection of strokes drawn by user for recognition.
     * Upon completion of drawing A new stroke, that last drawn
     * stroke is added into this collection for processing.
     */
    StrokeList _strokeListMemory;
    /**
     * This generic list stored the list RecognizedSymbol objects,which
     * are the symbols identified after recognition. As for more info,
     * A RecognizedSymbol stores a StrokeList related to it for furture processing.
     */
    List<RecognizedSymbol> _aryLMemoryRecognizedString;
    /**
     * XML Tree to store the Math Expression Tree as the result of Structural
     *  Analysis. This tree is ammended as necessary upon symbol recognition results.
     */
    Document _rawExpressionTree;
    /**
     * XML Tree to store Math ML as result of recognition.
     */
    Document _mathMLDocTree;
    /**
     * The object which is resposible for recognizing the "symbols",
     * (NOT MATH EXPRESSION) based on the trained symbol library.
     */
    SymbolRecognizer _manualRecognizer;
    //added by quxi 2009.12.22
    SymbolRecognizer_SVM _svmRecognizer;
    /**
     * The object which is responbislbe for analyzing the symbol relations
     * and to meaningful mathematic expressions.
     */
    StructuralAnalyser _structuralAnalyser;
    /**
     * This collection is the collection of symbols from the trained library,
     * each symbol tagged with recognition distance from identified symbol,
     * upon the event of last stroke drawing.
     * Since this this list if changed upon receiving each stroke. Every moment,
     * this list will contain symbols as the recognition result arranging in
     * Less Similar order.
     *
     * So the primary purpose of this list is to store all the symbol in order
     * of decreasing similarity after (symbol) recognition process.
     */
    ArrayList<RecognizedSymbol> _recognitionList = new ArrayList();
    /**
     * Object to create Document Builder Object for
     * XML document creation.
     */
    DocumentBuilderFactory objDocumentBuilderFactory;
    /**
     * Document builder object to create XML document.
     */
    DocumentBuilder xmlDocBuilder;
    //added by quxi 2009.09.01
    List<Baseline> baseLineList;
    //MapleConnection mapleConn;
    // </editor-fold>
    /**
     * Default constructor of Recognizer class.
     * @param theLibrary SymbolLib object with loaded symbol library.
     */
    public Recognizer(SymbolLib theLibrary) {
        //added by quxi 2009.09.01
        baseLineList = new ArrayList();
        _symbolLib = theLibrary;


        objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        //mapleConn = new MapleConnection();
        try {
            xmlDocBuilder = objDocumentBuilderFactory.newDocumentBuilder();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        _structuralAnalyser = new StructuralAnalyser(baseLineList);

    }

    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Retrieve the candidate list of symbols arrange
     * in ascending order of recognition error rate.
     * @return List of RecognizedSymbol objects.
     */
    public ArrayList getOptionalRecognitionList() {
        return _recognitionList;
    }

    /**
     * Retrieve the candidate list of symbols arrange
     * in ascending order of recognition error rate.
     * @return List of RecognizedSymbol objects.
     */
    //added by quxi 2009.08.27
    public List<RecognizedSymbol> getRecognizedSymboList() {
        return _aryLMemoryRecognizedString;
    }

    /**
     * Public method to set the SymbolLib for
     * Recognizer object.
     * @param SymbolLib The new symbol library to set.
     * @see SymbolLib
     */
    public void setSymbolLib(SymbolLib SymbolLib) {
        this._symbolLib = SymbolLib;
    }

    /**
     * Main method for Online Mathematic Expression Recognition.
     * @param newStroke The last stroke written on Drawing Pad.
     * @return MathML string of recognized expression.
     * @throws java.lang.Exception
     * @see Stroke
     */
    public String Recognize(Stroke newStroke) throws Exception {

        /// Initialize the _strokeListMemory if not yet been done so.
        if (_strokeListMemory == null) {
            _strokeListMemory = new StrokeList();
        }
        //added by quxi 2009.09.08
        if (baseLineList == null) {
            baseLineList = new ArrayList();
          this._structuralAnalyser.setBaseLine(baseLineList);
        }

        /// Initialize recognized String list memory if not yet been done so
        /// and create a Method wide local reference object.
        List<RecognizedSymbol> recognizedStringList = _aryLMemoryRecognizedString;
        if (recognizedStringList == null) {
            recognizedStringList = new ArrayList();
        }

        /// Initialize expression tree, if not yet been done so
        /// and create a Method wide local reference object.


        if (_rawExpressionTree == null) {
            _rawExpressionTree = xmlDocBuilder.newDocument();
            _rawExpressionTree.appendChild(_rawExpressionTree.createElement("root"));
        }


        /// Initialize manual recognizer object, providing the loaded
        /// trained symbol library, if not yet been done so.
        SymbolRecognizer manualRecognizer = _manualRecognizer;
        if (manualRecognizer == null) {
            manualRecognizer = new SymbolRecognizer(_symbolLib);
        }

        //added by quxi 2009.12.22
        SymbolRecognizer_SVM svmRecognizer = _svmRecognizer;
        if (svmRecognizer == null) {
            svmRecognizer = new SymbolRecognizer_SVM();
        }


        StructuralAnalyser structuralAnalyser = _structuralAnalyser;
        if (structuralAnalyser == null) {
            structuralAnalyser = new StructuralAnalyser(baseLineList);
        }

        /// Add the latest drawn stroke into stroke list memory collection.
        _strokeListMemory.add(newStroke);

        /// Call the recognition method (which is in Recognizer.cs) providing
        /// 1. Previously recognized String list,
        /// 2. Previously recognized expression tree, and
        /// 3. Manual Recoginzer object.
        /// And recieved the list of symbols (all from symbol lib) tagged with
        /// similarity distance.
        long startTime = System.currentTimeMillis();

        //More than one symbol
        ArrayList recognitionList = doRecognition(recognizedStringList,
                manualRecognizer, svmRecognizer);

        //System.out.println("Time after recognition is" + (System.currentTimeMillis() - startTime));
        Toast.makeText(MainActivity.getAppContext(), "Time after Symbol recognition is" + (System.currentTimeMillis() - startTime), Toast.LENGTH_SHORT).show();
        //FOR TESTING
        //for (int c = 0; c < recognizedStringList.size(); c++)
        //{
        //    RecognizedSymbol rrc = (RecognizedSymbol)recognizedStringList[c];
        //    for (int b = 0; b < rrc.Strokes.size(); b++)
        //    {
        //        for (int a = 0; a < rrc.Strokes[b].TotalStrokePoints; a++)
        //        {
        //            Console.WriteLine(rrc.Strokes[b].StrokePoints[a].ToString());
        //        }
        //    }
        //}
        String result = "";//MathML String

        result = doAnalysis(recognizedStringList, true);
        Toast.makeText(MainActivity.getAppContext(), "Time after analyser is" + (System.currentTimeMillis() - startTime), Toast.LENGTH_SHORT).show();

        _recognitionList = recognitionList;
        _aryLMemoryRecognizedString = recognizedStringList;
        _manualRecognizer = manualRecognizer;
        _svmRecognizer = svmRecognizer;
        _structuralAnalyser = structuralAnalyser;


        //String result = Character.toString(_recognitionList.get(0).getSymbolChar());


        return result;
    }

    /**
     * To make the correction of the recognition result
     * by selecting the index of recognized results.
     * Developer is consider to know OptionalRecogntionList
     * @param position Index of item in OptionalRecognitionList
     * @return MathML string of corrected recognized expression.
     * @throws java.lang.Exception
     */
    public String MakeCorrection(int position) throws Exception {
        String result = "";
        RecognizedSymbol symbolForReplacement = (RecognizedSymbol) _recognitionList.get(position);

        //add to sample file
        if (SymbolRecognizer_SVM.checkStrokeNO(symbolForReplacement.getSymbolCharDecimal(), symbolForReplacement.getStrokes().size())) {
            symbolFeature.SymbolFeature.writeFeatures(symbolFeature.SymbolFeature.getFeature(symbolForReplacement.getSymbolCharDecimal(), PreprocessorSVM.preProcessing(symbolForReplacement.getStrokes())));
        }

        int affectedSymbolCount = 0, count = 0;
        //added by quxi 2009.12.27
        baseLineList.removeAll(baseLineList);
        _structuralAnalyser.resetFlags();

        while (count < symbolForReplacement.getNumberOfStrokes()) {
            RecognizedSymbol symbolToBeReplaced = (RecognizedSymbol) (_aryLMemoryRecognizedString.get(_aryLMemoryRecognizedString.size() - 1));
            _aryLMemoryRecognizedString.remove(symbolToBeReplaced);
            affectedSymbolCount++;
            count += symbolToBeReplaced.getNumberOfStrokes();
        }
        count = count - symbolForReplacement.getNumberOfStrokes();
        for (int i = 0; i < _aryLMemoryRecognizedString.size(); i++) {
            if (i == _aryLMemoryRecognizedString.size() - 1) {
                result = doAnalysis(_aryLMemoryRecognizedString.subList(0, i + 1), true);
            } else {
                doAnalysis(_aryLMemoryRecognizedString.subList(0, i + 1), false);
            }
        }
        while (count > 0) {
            List<Stroke> tempStrokeList1 = _strokeListMemory.subList(0,
                    _strokeListMemory.size() - symbolForReplacement.getNumberOfStrokes() - count + 1);
            StrokeList st = new StrokeList();
            for (int i = 0; i < tempStrokeList1.size(); i++) {
                st.add(tempStrokeList1.get(i));
            }
            doRecognition(st, _aryLMemoryRecognizedString, _manualRecognizer, _svmRecognizer);
            doAnalysis(_aryLMemoryRecognizedString, true);
            count--;
        }
        _aryLMemoryRecognizedString.add(symbolForReplacement);
        //result = doAnalysis(_aryLMemoryRecognizedString, true);
        return result;
    }

    /**
     * To undo the last stroke of recognition result
     * by removing the last stroke from recognition input
     * re-evaluate the expression.
     * @return MathML string of undone recognized expression.
     * @throws java.lang.Exception
     */
    public String UndoLastStroke() throws Exception {
        String result = " ";
        _rawExpressionTree = xmlDocBuilder.newDocument();

        RecognizedSymbol rc = (RecognizedSymbol) (_aryLMemoryRecognizedString.get(_aryLMemoryRecognizedString.size() - 1));
        _aryLMemoryRecognizedString.remove(_aryLMemoryRecognizedString.size() - 1);

        //added by quxi 2009.12.27
        baseLineList.removeAll(baseLineList);
        _structuralAnalyser.resetFlags();

        for (int i = 0; i < _aryLMemoryRecognizedString.size(); i++) {
            if (i == _aryLMemoryRecognizedString.size() - 1) {
                result = doAnalysis(_aryLMemoryRecognizedString.subList(0, i + 1), true);
            } else {
                doAnalysis(_aryLMemoryRecognizedString.subList(0, i + 1), false);
            }
        }

        int pos = rc.getNumberOfStrokes();
        while (pos > 1) {
            _recognitionList = doRecognition(
                    specialTypeCast(_strokeListMemory.subList(0, _strokeListMemory.size() - pos + 1)),
                    _aryLMemoryRecognizedString, _manualRecognizer, _svmRecognizer);
            result = doAnalysis(_aryLMemoryRecognizedString, true);
            pos--;
        }
        _strokeListMemory.remove(_strokeListMemory.size() - 1);

        return result;
    }

    /**
     * Clear the internal memory of Recognizer object.
     * This method clear all recognized results, temporary processing data
     * and reset Structural Analyzer object, but doesn't effect
     * on Symbol Recognizer and Symbol Libray loaded for
     * recognition.
     */
    public void ClearRecognitionMemory() {
        _strokeListMemory = null;
        //added by quxi 2009.09.08
        baseLineList = null;
        _aryLMemoryRecognizedString = null;
        _rawExpressionTree = null;
        _mathMLDocTree = null;
        _recognitionList = null;
        _structuralAnalyser = new StructuralAnalyser(baseLineList);
    }

    /**
     * Retrive internal XML Document object of recognized
     * expression.
     * @return XML Document object.
     * @see Document
     *
     */

    public Document getMathMLDocTree() {
        return _mathMLDocTree;
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Private Methods">
    /**
     * Method to cast Generic list of Stroke Objects to
     * StrokeList object.
     * @param target Generic list object to convert.
     * @return StrokeList object of converted Strokes.
     * @see Stroke
     * @see StrokeList
     */
    private StrokeList specialTypeCast(List<Stroke> target) {
        StrokeList result = new StrokeList();
        for (int c = 0; c < target.size(); c++) {
            result.add(target.get(c));
        }
        return result;
    }

    /**
     * Internal data management method.
     * @param recognizedChar The lastly recognized symbol.
     * @param list The arraylist of RecognizedSymbosl which is previously recognized
     *        just before this last symbol is recognized.
     * @return True for successful addition, False otherwise.
     */
    private boolean addToList(RecognizedSymbol recognizedChar, List<RecognizedSymbol> candidate, List<RecognizedSymbol> list) throws Exception {
        //NOTE THAT: at this initial stage recognizedcChar is not in the list.

        //get the stroke count of last recognized symbol
        int strokes = recognizedChar.getNumberOfStrokes();

        ///this part is actually checking if the last character
        ///in the recognized list is minus sign (-)
        ///If yes, modify the XML of that recognized char.
        ///And DO NOT add the new (recognized char) to list (just return false);

        //When the stroke count of last recognized is more than 1
        //keep looping
        while (strokes > 1) {
            RecognizedSymbol lastRecSymbolInList = (RecognizedSymbol) list.get(list.size() - 1);
            strokes -= lastRecSymbolInList.getNumberOfStrokes();

            //if previously last recognized symbol from the list is - or dash
            if (lastRecSymbolInList.getSymbolCharString().equals(String.valueOf('\u2212'))) {
                //if the child node position at 1 and 5 has children return FALSE
                //no adding of last symbol to the recognized string list
                if (lastRecSymbolInList.getNode().getChildNodes().item(1).getChildNodes().getLength() > 0 || lastRecSymbolInList.getNode().getChildNodes().item(5).getChildNodes().getLength() > 0) {
                    return false;
                }
            }
        }
        //re-init stroke count
        //Keep removing the number of strokes and previously recognized symbol
        //from the list (take example we write a - first to write a +, in that case
        //the last recognized - will be removed to replace it with +
        strokes = recognizedChar.getNumberOfStrokes();
        while (strokes > 1) {
            RecognizedSymbol lastRecSymbolInList = (RecognizedSymbol) list.get(list.size() - 1);
            strokes -= lastRecSymbolInList.getNumberOfStrokes();
            list.remove(lastRecSymbolInList);
            Node node = lastRecSymbolInList.getNode();
            if (node.getParentNode() != null) {
                node.getParentNode().removeChild(node);

            }
            //added by quxi 2009.09.01
            //update the baseLine symbols
            for (int i = 0; i < baseLineList.size(); i++) {
                List<RecognizedSymbol> symbolList = baseLineList.get(i).getSymbolList();
                for (int j = 0; j < symbolList.size(); j++) {
                    if (!list.contains(symbolList.get(j))) {
                        if (baseLineList.get(i).getSymbolList().size() == 1) {
                            baseLineList.remove(baseLineList.get(i));
                        } else {
                            baseLineList.get(i).removeSymbol(symbolList.get(j));
                        }
                    }
                }
            }
        }
        //added by quxi 2009.12.29
        while (strokes < 1) {
            List<Stroke> tempStrokeList1 = _strokeListMemory.subList(0,
                    _strokeListMemory.size() - recognizedChar.getNumberOfStrokes() + strokes);
            StrokeList st = new StrokeList();
            for (int i = 0; i < tempStrokeList1.size(); i++) {
                st.add(tempStrokeList1.get(i));
            }
            doRecognition(st, list, _manualRecognizer, _svmRecognizer);
            doAnalysis(list, true);
            strokes++;
        }

        //modified by quxi 2009.12.29

        if (list.size() == 0 || verifyContext(recognizedChar, (RecognizedSymbol) list.get(list.size() - 1))) {
            RecognizedSymbol pre = new RecognizedSymbol('0');
            if (list.size() != 0) {
                pre = (RecognizedSymbol) list.get(list.size() - 1);
            }
            recognizedChar = checkSimilarSymbols(recognizedChar, pre, candidate);
            list.add(recognizedChar);
            return true;
        } else {
            if (list.size() != 0 && ConstantData.doTest) {
                Toast.makeText(context, "Blocked by checkContext!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

    }


    //added by quxi 2010.1.15
    //check if the symbol is similar symbol and choose the correct one
    private RecognizedSymbol checkSimilarSymbols(RecognizedSymbol recognizedChar, RecognizedSymbol previous, List<RecognizedSymbol> candidate) {
        char lastChar = recognizedChar.getSymbolChar();
        char preChar = previous.getSymbolChar();
        if (lastChar == 'S' || lastChar == 'C' || lastChar == 'P' || lastChar == 'V' || lastChar == 'Z') {
            if (65 > (int) preChar || (int) preChar > 90) {
                recognizedChar.setSymbolChar(Character.toLowerCase(lastChar));
                switchChar(candidate, lastChar, recognizedChar);
            }
        } else if (lastChar == 'O' || lastChar == 'o' || lastChar == '0') {
            if (65 <= (int) preChar && (int) preChar <= 90) {
                recognizedChar.setSymbolChar('O');
                switchChar(candidate, lastChar, recognizedChar);
            } else if (97 <= (int) preChar && (int) preChar <= 122) {
                recognizedChar.setSymbolChar('o');
                switchChar(candidate, lastChar, recognizedChar);
            } else {
                recognizedChar.setSymbolChar('0');
                switchChar(candidate, lastChar, recognizedChar);
            }
        } else if (lastChar == 'X' || (int) lastChar == 215) {
            if (65 <= (int) preChar && (int) preChar <= 90) {
                recognizedChar.setSymbolChar('X');
                switchChar(candidate, lastChar, recognizedChar);
            } else {
                recognizedChar.setSymbolChar((char) 215);
                switchChar(candidate, lastChar, recognizedChar);
            }
        }
        return recognizedChar;
    }

    private void switchChar(List<RecognizedSymbol> list, char lastChar, RecognizedSymbol recognizedChar) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSymbolChar() == recognizedChar.getSymbolChar() && !list.get(i).equals(recognizedChar)) {
                list.get(i).setSymbolChar(lastChar);
            }
        }
    }

    /**
     * Internal method to perform symbol Recognition.
     * @param recognizedStringList Previously recognized symbols. alr exist
     * @param mRecognizer Symbol recognizer object.
     * @return List of newly recognized symbol.
     * @throws java.lang.Exception
     *
     */
    private ArrayList doRecognition(
            List<RecognizedSymbol> recognizedStringList,
            SymbolRecognizer mRecognizer, SymbolRecognizer_SVM svmRecognizer) throws Exception {

        //TODO: cannot reg ":"

        long SVMStartTime = System.currentTimeMillis();
        //using svm to recognise, get and recognise all 4
        ArrayList mResult = svmRecognizer.recognizing(_strokeListMemory.GetLast4Strokes());
        long elasticStartTime = System.currentTimeMillis() - SVMStartTime;
        Toast.makeText(context, "Time after SVM recognition is : " + elasticStartTime , Toast.LENGTH_SHORT).show();
        Toast.makeText(context, "Result from SVM: " + mResult, Toast.LENGTH_LONG).show();
        //using elastic match
        mResult = mRecognizer.recognizing(mResult);
        Toast.makeText(context, "Time after elastic is" + (System.currentTimeMillis() - elasticStartTime), Toast.LENGTH_SHORT).show();
        //mResult = verifyContext(mResult);

        /// Take the first (with closet similarity distance) character
        /// as recognized symbol.
        RecognizedSymbol recognizedChar = (RecognizedSymbol) mResult.get(0);

        int i = 0;
        while (i < mResult.size() && !addToList(recognizedChar, mResult, recognizedStringList)) {
            recognizedChar = (RecognizedSymbol) mResult.get(++i);
        }
        System.out.println("Time after addToList is" + (System.currentTimeMillis() - elasticStartTime));

        return mResult;
    }

    /**
     * Overloaded version of internal method to perform symbol Recognition.
     * @param inputStrokeList StrokeList of all written strokes.
     * @param recognizedStringList Previously recognized symbols.
     * @param mRecognizer Symbol recognizer object.
     * @return List of newly recognized symbol.
     * @throws java.lang.Exception
     */
    private ArrayList doRecognition(
            StrokeList inputStrokeList,
            List<RecognizedSymbol> recognizedStringList,
            SymbolRecognizer mRecognizer, SymbolRecognizer_SVM svmRecognizer) throws Exception {

        /// Call the manual recognizer's process
        /// by passing (Last 4 Strokes) from memory stroke list collection.
        /// And keep the result. The return result the list of all
        /// symbols (same as in trained symbol list) tagged with similarity distance
        /// in increasing order.

        ArrayList mResult = svmRecognizer.recognizing(inputStrokeList.GetLast4Strokes());
        mResult = mRecognizer.recognizing(mResult);
        /// Take the first (with closet similarity distance) character
        /// as recognized symbol.
        RecognizedSymbol recognizedChar = (RecognizedSymbol) mResult.get(0);

        int i = 0;
        //do we have to do the add to list here?
        while (i < mResult.size() && !addToList(recognizedChar, mResult, recognizedStringList)) {
            recognizedChar = (RecognizedSymbol) mResult.get(++i);
        }
        return mResult;
    }


    //added by quxi 2009.12.29
    private boolean verifyContext(RecognizedSymbol result, RecognizedSymbol last) {
        int pos = _structuralAnalyser.boundingBoxDetermination(last, result);
        return SymbolClassifier.checkContext(pos, last, result);

     }


    /**
     * Internal method to perform structural analysis of
     * recognized symbols.
     * @param recognizedStringList List of recognized symbols.
     * @param convertFlag Whether to convert Expression Tree to MathML String. Usually it's always TRUE.
     * @return MathML String of recognized expression.
     *
     */



    private String doAnalysis(List<RecognizedSymbol> recognizedStringList, boolean convertFlag) {

        _structuralAnalyser.analyse(recognizedStringList, _rawExpressionTree);

        if (convertFlag) {
            try {
                String asciimath = treeToascii(_rawExpressionTree.getFirstChild().cloneNode(true));
                //return asciiToMathMl(asciimath);
                Toast.makeText(context, asciimath, Toast.LENGTH_SHORT).show();
                return asciimath;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
     }



    /**private String asciiToMathMl(String formula) {
        Process p = null;
        String ls_str = "";
        String result = "";
        try {
            String s = ConstantData.exeDir;
            s += formula;
            p = Runtime.getRuntime().exec(s);

            DataInputStream ls_in = new DataInputStream(
                    p.getInputStream());
            try {
                while ((ls_str = ls_in.readLine()) != null) {
                    result += ls_str + "\n";
                }
            } catch (IOException e) {
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("error===" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }*/

    private int getPosition(Node child, Node parent) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).equals(child)) {
                if (i == 2) { //now the sequence is 2 subscript 3 superscript 4 row
                    return 4;
                } else if (i == 3) {
                    return 2;
                } else if (i == 4) {
                    return 3;
                }
                return i;
            }
        }
        return -1;
    }
    //added by quxi 2010.

    private String treeToascii(Node rootEquationNode) {
        String asciiString = "";
        String lastOperator = "";
        Stack stack = new Stack();
        boolean handlingMatrix = false;
        // ignore root -- root acts as a container
        Node node = rootEquationNode.getFirstChild();
        while (node != null) {
            // print node information
            String nodeType = node.getNodeName();
            String symbol = ((Element) (node)).getAttribute("identity");
            if (nodeType.equals("symbolnode")) {
                if (symbol.equals(String.valueOf('\u221a'))) { //root
                    asciiString += "root()";
                    lastOperator = "root";
                } else if (symbol.equals(String.valueOf('\u2212'))) {
                    if (node.getChildNodes().item(5).hasChildNodes() || node.getChildNodes().item(1).hasChildNodes()) {
                        //asciiString += "frac";
                    } else {
                        asciiString += "-";
                    }
                } else if (symbol.equals(String.valueOf('\u2192'))) {
                    asciiString += "vec(?)";
                } else if (symbol.equals(String.valueOf('\u002f'))) {
                    asciiString += "//";
                } else if (symbol.equals(String.valueOf('\u00d7'))) {
                    asciiString += "xx";
                } else if (symbol.equals(String.valueOf('\u00f7'))) {
                    asciiString += "-:";
                } else if (symbol.equals(String.valueOf('\u00b1'))) {
                    asciiString += "+-";
                } else if (symbol.equals(String.valueOf('\u222b'))) {
                    asciiString += "int";
                } else if (symbol.equals(String.valueOf('\u2211'))) {
                    asciiString += "sum";
                } else if (symbol.equals(String.valueOf('\u220f'))) {
                    asciiString += "prod";
                } else if (symbol.equals("^")) { // havent handled
                    asciiString += "hat";
                } else if (symbol.equals(String.valueOf('\u2260'))) { //
                    asciiString += "!=";
                } else if (symbol.equals(String.valueOf('\u2264'))) { //
                    asciiString += "<=";
                } else if (symbol.equals(String.valueOf('\u2265'))) { //
                    asciiString += ">=";
                } else if (symbol.equals(String.valueOf('\u2248'))) { //
                    asciiString += "~~";
                } else if (symbol.equals(String.valueOf('\u221d'))) { //
                    asciiString += "prop";
                } else if (symbol.equals(String.valueOf('\u221e'))) { //
                    asciiString += "oo";
                } else if (symbol.equals(String.valueOf('\u03b1'))) { //
                    asciiString += "alpha";
                } else if (symbol.equals(String.valueOf('\u03b2'))) { //
                    asciiString += "beta";
                } else if (symbol.equals(String.valueOf('\u03b5'))) { //
                    asciiString += "epsilon";
                } else if (symbol.equals(String.valueOf('\u03b8'))) { //
                    asciiString += "Theta";
                } else if (symbol.equals(String.valueOf('\u03bb'))) { //
                    asciiString += "lambda";
                } else if (symbol.equals(String.valueOf('\u03bc'))) { //
                    asciiString += "mu";
                } else if (symbol.equals(String.valueOf('\u03c1'))) { //
                    asciiString += "rho";
                } else if (symbol.equals(String.valueOf('\u03c3'))) { //
                    asciiString += "sigma";
                } else if (symbol.equals(String.valueOf('\u03c6'))) { //
                    asciiString += "phi";
                } else {
                    asciiString += symbol;
                }
            } else if (nodeType.equals("equation") && node.hasChildNodes()) {
                if (handlingMatrix && (node.getParentNode().getNodeName().equals("matrixrow") || node.getParentNode().getNodeName().equals("matrix"))) {
                    stack.push(node);
                } else {
                    int pos = getPosition(node, node.getParentNode());
                    switch (pos) {
                        case StructuralAnalyser.PRE_SUPER_SCRIPT:
                            if (lastOperator.equals("root")) {
                                asciiString = asciiString.substring(0, asciiString.length() - 2);
                                asciiString += "(";
                                stack.push(node);
                            }
                            break;
                        case StructuralAnalyser.ABOVE:
                            asciiString += "(";
                            stack.push(node);
                            break;
                        case StructuralAnalyser.SUPER_SCRIPT:
                            asciiString += "^(";
                            stack.push(node);
                            break;
                        case StructuralAnalyser.ROW:
                            break;
                        case StructuralAnalyser.SUB_SCRIPT:
                            asciiString += "_(";
                            stack.push(node);
                            break;
                        case StructuralAnalyser.BELOW:
                            Node temp = node.getParentNode();
                            String tempValue = ((Element) (temp)).getAttribute("identity");
                            if (tempValue.equals(String.valueOf('\u2212')) && temp.getChildNodes().item(5).hasChildNodes() && temp.getChildNodes().item(1).hasChildNodes()) {
                                asciiString += "/(";
                            } else if (tempValue.equals(String.valueOf('\u2192'))) {
                                asciiString = asciiString.substring(0, asciiString.length() - 3);
                                asciiString += "(";
                            } else {
                                asciiString += "(";
                            }
                            stack.push(node);
                            break;
                        case StructuralAnalyser.PRE_SUB_SCRIPT:
                            break;
                        case 7:
                            break;
                        case StructuralAnalyser.INSIDE:
                            asciiString += "(";
                            stack.push(node);
                            break;
                    }
                }
            } else if (nodeType.equals("matrixOB")) {
                handlingMatrix = true;
            } else if (nodeType.equals("matrixCB")) {
                handlingMatrix = false;
            } else if (nodeType.equals("matrixrow")) {
                asciiString += "(";
                stack.push(node);
            }
            if (node.hasChildNodes()) {
                if (node.getChildNodes().getLength() == 9 && node.getNodeName().equals("symbolnode")) { //force it to handle subscript first
                    Node subNode = node.removeChild(node.getChildNodes().item(4));
                    node.insertBefore(subNode, node.getChildNodes().item(2));
                }
                node = node.getFirstChild();
            } else {
                // find the parent level
                while (node.getNextSibling() == null && node != rootEquationNode) // use child-parent link to get to the parent level
                {
                    node = node.getParentNode();
                    //do correction after return back
                    if (!stack.empty() && stack.peek().equals(node)) {
                        String parentNodeName = node.getParentNode().getNodeName();
                        if (parentNodeName.equals("matrixrow")) {
                            if (node.getNextSibling() != null) {
                                asciiString += ",";
                            }
                        } else if (parentNodeName.equals("matrix")) {
                            asciiString += ")";
                            if (node.getNextSibling() != null && node.getNextSibling().getNodeName().equals("matrixrow")) {
                                asciiString += ",";
                            }
                        } else {
                            asciiString += ")";
                        }
                        stack.pop();
                    }
                }
                node = node.getNextSibling();
            }
        }
/**
        //added by quxi 2010.1.10 --computation engine
        if (asciiString.length() > 1 && asciiString.charAt(asciiString.length() - 1) == '=') {
            String mapleCmd = mapleConn.convertToMapleInstruction(asciiString.substring(0, asciiString.length() - 1));
            String result = mapleConn.compute(mapleCmd, mapleConn.getEngine());
            if (result != null) {
                try {
                    double resu = Double.parseDouble(result);
                    DecimalFormat formatter = new DecimalFormat("0.00");
                    asciiString = asciiString.concat(formatter.format(resu));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse maple result");
                    asciiString = asciiString.concat(result);
                }
            }
        }
        System.out.println(asciiString);*/
        return asciiString;
    }

    /**
     * Utility method to dump a branch of XML tree,
     * starting from given node, to debugger console.
     * @param startNode The xml node to start exploration.
     * @param padding String padding to indent the children nodes.
     */
    private void visualizeTree(Element startNode, String padding) {
        System.out.println(padding + startNode.getAttribute("id") + " " + startNode.getNodeName() + "( " + startNode.getAttribute("identity") + " )" + "[ " + startNode.getAttribute("type") + " ]");

        if (startNode.hasChildNodes()) {
            for (int i = 0; i < startNode.getChildNodes().getLength(); i++) {
                visualizeTree((Element) startNode.getChildNodes().item(i), padding + "-");
            }
        }
        return;
    }
    // </editor-fold>*/
}