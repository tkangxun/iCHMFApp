package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */
import java.io.File;

/**
 *
 * @author Qu Xi
 */
public class ConstantData {

    static File dir = new File(System.getProperty("user.dir"));
    static String parentpath = dir.getParent();
    public static String trainFile = parentpath + "\\sample.dat";
    public static String modelFile = parentpath + "\\model.dat";
    public static String ElasticFileString = parentpath + "\\elastic.dat";
    public static String ElasticFileDefaultString = parentpath + "\\elasticDefault.dat";
    public static File ElasticFile = new File(ElasticFileString);
    public static File ElasticFileDefault = new File(ElasticFileDefaultString);
    public static String exeDir = parentpath + "\\php\\mathml.exe ";
    public static boolean doTest = false;
}