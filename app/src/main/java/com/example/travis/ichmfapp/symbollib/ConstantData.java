package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */
import android.content.Context;
import android.content.res.AssetManager;

import java.io.*;
import java.util.*;


/**
 *
 * @author Qu Xi
 */

public class ConstantData {

    private AssetManager am;


    public ConstantData(Context context){

        am = context.getAssets();
        String[] filenames = {"elastic.dat", "elasticDefault.dat", "model.dat","sample.dat","sample_original.dat"};

        try {


            for (int i = 0; i<filenames.length; i++) {
                String file = "file/" + filenames[i];
                InputStream inputStream = am.open(file);
                File f = new File(filenames[i]);
                OutputStream outputStream = new FileOutputStream(f);
                byte buffer[] = new byte[1024];
                int length = 0;

                while((length=inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            }


        } catch (Exception e){
            e.printStackTrace();
        }

    }
    public void CreateData() {
        return;
    }

    static File dir = new File(("user.dir"));
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