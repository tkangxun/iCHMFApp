package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */
import android.content.Context;
import android.content.res.AssetManager;

import java.io.*;
import java.util.ArrayList;


/**
 *
 * @author Qu Xi
 */

public class ConstantData {

    private AssetManager am;
    private ArrayList<File> allFiles = new ArrayList<>();


    public ConstantData(Context context){

        am = context.getAssets();
        String[] filenames = {"elastic.dat", "elasticDefault.dat", "model.dat","sample.dat","sample_original.dat"};

        for (int i = 0; i<filenames.length; i++) {
            try {
                String file = "file/" + filenames[i];
                InputStream inputStream = am.open(file);
                allFiles.add(new File(filenames[i]));
                /** OutputStream outputStream = new FileOutputStream(f);
                 byte buffer[] = new byte[1024];
                 int length = 0;

                 while((length=inputStream.read(buffer)) > 0) {
                 outputStream.write(buffer, 0, length);
                 }
                 outputStream.close();
                 inputStream.close();
                 */
            } catch (Exception e) {
                System.out.print(filenames[i] + "not changed to file.");
                e.printStackTrace();
            }
        }}

    static File dir = new File(("user.dir"));
    static String parentpath = dir.getParent();
    public static String trainFile = parentpath + "\\sample.dat";

    //model file past to svm_predict, sp.run then to svm file to create buffer
    public  File modelFile = new File(allFiles[2]);
    public static String ElasticFileString = parentpath + "\\elastic.dat";
    public static String ElasticFileDefaultString = parentpath + "\\elasticDefault.dat";
    public static File ElasticFile = new File(ElasticFileString);
    public static File ElasticFileDefault = new File(ElasticFileDefaultString);
    public static String exeDir = parentpath + "\\php\\mathml.exe ";
    public static boolean doTest = false;



}