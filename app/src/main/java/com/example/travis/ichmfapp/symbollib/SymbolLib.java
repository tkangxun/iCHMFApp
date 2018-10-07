package com.example.travis.ichmfapp.symbollib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.travis.ichmfapp.main.MainActivity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.travis.ichmfapp.symbollib.ConstantData.mydir;


/**
 * Created by Travis on 9/8/2018.
 */

public class SymbolLib implements Serializable {

    public enum LibraryTypes {

        Binary, Xml
    };
    /**
     * List of symbol objects being managed.
     */
    protected SymbolList _Symbols;
    /**
     * Name of the symbol library.
     */
    protected String _title = "";

    /**
     * Name of the symbol library file.
     */
    protected String _filename ="";


    /**
     * Default constructor of the SymbolLibrary
     * */
    public SymbolLib() {
        _Symbols = new SymbolList();
    }

    /**
     * Overloaded constructor.
     * @param title
     */
    public SymbolLib(String title) {
        _Symbols = new SymbolList();
        _title = title;
        _filename= new String();
    }

    public int getTotalSymbol() {
        return _Symbols.size();
    }

    public SymbolList getSymbols() {
        return _Symbols;
    }

    public void setSymbols(SymbolList value) {
        _Symbols = value;
    }

    public String getTitle() {
        return _title;
    }

    public String getfilename(){return _filename;}

    public void setTitle(String value) {
        _title = value;
    }

    public void setFilename(String value) {
        _filename = value;
    }

    public void addSymbol(Symbol s) throws Exception {
        _Symbols.add(s);
    }

    public Symbol getSymbol(int index) {
        return _Symbols.get(index);
    }


    //get a list since some char may have more than one samples
    public ArrayList getSymbolFromChar(char character) {
        ArrayList<Symbol> result = new ArrayList();
        for (int index = 0; index < _Symbols.size(); index++) {
            if (character == ((Symbol) _Symbols.get(index)).getSymbolChar()) {
                result.add(_Symbols.get(index));
            }
        }
        if (result.size() > 0 && result.size() < 4) {
            return result;
        } else {
            Toast.makeText(MainActivity.getAppContext(), "is null here", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void setSymbol(Symbol newSymbol, int index) {
        _Symbols.set(index, newSymbol);
    }

    public void removeSymbol(int index) {
        _Symbols.remove(index);
    }

    //find the index of the symbol by providing the decimal
    public List<Integer> findSymbol(int SymbolDecimalChar) {
        List<Integer> list = new ArrayList<Integer>();
        for (int index = 0; index < _Symbols.size(); index++) {
            if (SymbolDecimalChar == ((Symbol) _Symbols.get(index)).getSymbolCharDecimal()) {
                list.add(index);
            }
        }
        return list;
    }

    public static int getCharToDecimal(char _charSymbol) {
        return ((int) _charSymbol);
    }

    public static String getCharToHex(char _charSymbol) {
        return String.format("0x%x", getCharToDecimal(_charSymbol));
    }

    public static char getHexToChar(int hexCode) {
        return (char) hexCode;
    }

    public static char unicodeToChar(String unicode){
        unicode = unicode.replace("\\","");
        unicode = unicode.replace("u","");
        int hexVal = Integer.parseInt(unicode, 16);
        char _charSymbol = (char)hexVal;
        return _charSymbol;
    }

    public static char getDecimalToChar(int number) {
        return (char) number;
    }

    /**
     * Generate default symbol set for training.
     * @param filePathLibrary Location to generate library file.
     * @param type Type of symbol library file to create.
     * @return True if success, False otherwise.
     * @throws java.lang.Exception
     */
    //added by quxi 2009.10.21 :for the symbol trainer
    public static boolean GenerateDefaultSetElastic(String filePathLibrary, LibraryTypes type) throws Exception {
        SymbolLib basic = new SymbolLib("Basic Symbol Library for Elastic matching");
        basic.setFilename(filePathLibrary);
        int[] operators = {
                0x2192,// right arrow
                0x220F,// multiply all
                0x2211,// sum all 1 stroke
                0x2211,// sum all 2 stroke
                0x2211,// sum all 4 stroke
                0x2212,// minus sign
                0x221A,// sqrt
                0x221D,// proportion to top to bottom
                0x221D,// proportion to bottom to top
                0x221E,// infinity clockwise
                0x221E,// infinity anticlockwise
                0x222B,// integral
                0x2248,// almost equal to
                0x2260,// not equal to strike first
                0x2260,// not equal to equal first
                0x2264,// less or equal
                0x2265,// larger or equal
        }; //207 - 138 = 69
        int[] numberSet = {
                48, //0 clockwise
                48, //0 anticlockwise
                49, //1
                49, //1 with hook on top
                50, //2
                51, //3
                52, //4 2 strokes, line first
                52, //4 2 strokes
                52, //4 1 stroke
                53, //5 1 stroke
                53, //5 2 strokes - first
                53, //5 2 strokes - last
                54, //6 clockwise
                54, //6 anticlockwise
                55, //7 1 stroke
                55, //7 2 strokes
                56, //8 clockwise
                56, //8 anticlockwise
                57, //9 1 stroke
                57};//9 2 stroke

        int[] letterSet = {
                //capital letters
                65, 66, 66, 67, 68, 68, 69, 70, 71, 71, 72, 72, 73,
                74, 74, 75, 75, 76, 77, 77, 77, 78, 78, 79, 79, 80, 80, 81, 82, 82, 83, 84, 84, 85, 86, 86,
                87, 87, 88, 89, 89, 90, 90,

                //small letters
                97, 98, 98, 99, 100, 100, 101, 102, 102, 103, 104, 105,
                105, 106, 106, 107, 108, 109, 110, 111, 112, 112, 113, 114, 115, 116,
                116, 117, 118, 118, 119, 120, 121, 122, 122};

        int[] greekSet = {0x03B1, 0x03B2, 0x03B5, 0x03B8, 0x03BB,
                0x03BC, 0x03BC, 0x03C1, 0x03C3, 0x03C6};

        int[] operatorSet = {
                37, //%
                40, //(
                41, //)
                42, //* Asterisk
                42,
                42,
                43, //+
                43, //+
                46, //.
                47, ///
                58, //:
                60, //<
                61, //=
                62, //>
                91, //[
                91, //[
                93, //]
                93, //]
                94, //^
                94, //^ 2 strokes
                123,//{
                125,//}
                126,//~
                177, //+ or -
                215, //multiplication
                247, //division
                247, //division
        };
        for (int j = 0; j < numberSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(numberSet[j])));
        }
        for (int j = 0; j < letterSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(letterSet[j])));
        }
        for (int j = 0; j < operatorSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(operatorSet[j])));
        }
        for (int i = 0; i < greekSet.length; i++) {
            basic.addSymbol(new Symbol(SymbolLib.getHexToChar(greekSet[i])));
        }
        for (int i = 0; i < operators.length; i++) {
            basic.addSymbol(new Symbol(SymbolLib.getHexToChar(operators[i])));
        }

        if (type == LibraryTypes.Xml) {
            basic.Save(filePathLibrary, SymbolLib.LibraryTypes.Xml);
        }
        if (type == LibraryTypes.Binary) {
            basic.Save(filePathLibrary, SymbolLib.LibraryTypes.Binary);
        }
        return true;
    }

    public static SymbolLib GenerateDefaultSetSVM(LibraryTypes type) throws Exception {
        SymbolLib basic = new SymbolLib("Basic Symbol Library for SVM");
        int[] operators = {
                0x2192,// right arrow
                0x220F,// multiply all
                0x2211,// sum all
                0x2212,// minus sign
                0x221A,// sqrt
                0x221D,// proportion to
                0x221E,// infinity
                0x222B,// integral
                0x2248,// almost equal to
                0x2260,// not equal to
                0x2264,// less or equal
                0x2265,// larger or equal
        }; //207 - 138 = 69

        int[] numberSet = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
        int[] letterSet = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
                111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};

        int[] greekSet = {0x03B1, 0x03B2, 0x03B5, 0x03B8, 0x03BB,
                0x03BC, 0x03C1, 0x03C3, 0x03C6};

        int[] operatorSet = {
                37, //%
                40, //(
                41, //)
                42, //* Asterisk
                43, //+
                46, //.
                47, ///
                58, //:
                60, //<
                61, //=
                62, //>
                91, //[
                93, //]
                94, //^
                123,//{
                125,//}
                126,//~
                177, //+ or -
                215, //multiplication
                247, //division
        };
        for (int j = 0; j < numberSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(numberSet[j])));
        }
        for (int j = 0; j < letterSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(letterSet[j])));
        }
        for (int j = 0; j < operatorSet.length; j++) {
            basic.addSymbol(new Symbol(SymbolLib.getDecimalToChar(operatorSet[j])));
        }
        for (int i = 0; i < greekSet.length; i++) {
            basic.addSymbol(new Symbol(SymbolLib.getHexToChar(greekSet[i])));
        }
        for (int i = 0; i < operators.length; i++) {
            basic.addSymbol(new Symbol(SymbolLib.getHexToChar(operators[i])));
        }
        return basic;
    }

    /**
     * To save current library object into a
     * given file location, with given type.
     * @param fname file name to save the library.
     * @param libType Type of file to save.
     * @return True for successful saving/ False otherwise.
     * @throws java.lang.Exception
     */
    public boolean Save(String fname, LibraryTypes libType)
            throws Exception {
        //---- Serialize in Binary Format
        if (libType == LibraryTypes.Binary) {
            if (ContextCompat.checkSelfPermission(MainActivity.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.getAppContext(), "permission denied, please enable it", Toast.LENGTH_SHORT).show();
            } else{
                //Toast.makeText(MainActivity.getAppContext(), "permission granted", Toast.LENGTH_SHORT).show();
            }


            if (!mydir.exists()) {
                mydir.mkdirs();

                if (!mydir.mkdirs()) {
                    Toast.makeText(MainActivity.getAppContext(), "dir not made", Toast.LENGTH_SHORT).show();
                }
            }

            File file = new File(mydir, fname);
            String s = file.getPath().toString();
            //Toast.makeText(MainActivity.getAppContext(), s, Toast.LENGTH_SHORT).show();

            if (file.exists()){
                file.delete();
                Toast.makeText(MainActivity.getAppContext(), s + " is overwritten", Toast.LENGTH_SHORT).show();
            }
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
            } return true;


        }



    /**
     * Read the symbol library file from the give  location.
     * @param filePathLibrary Location of symbol library file to read.
     * @param libType The type symbol library file to read.
     * @return Newly create symbol library object from read file.
     * @throws java.lang.Exception
     */
    public static SymbolLib Load(String filePathLibrary, LibraryTypes libType)
            throws Exception {

        if (libType == LibraryTypes.Binary) {

            java.io.FileInputStream fis =
                    new java.io.FileInputStream(
                            new File(ConstantData.mydir.toString()+filePathLibrary));

            ObjectInputStream ois = new ObjectInputStream(fis);

            SymbolLib sbl = (SymbolLib) ois.readObject();
            ois.close();
            fis.close();

            Toast.makeText(MainActivity.getAppContext(), filePathLibrary+ " loaded", Toast.LENGTH_SHORT).show();
            return sbl;
        }
        return null;
    }
}
