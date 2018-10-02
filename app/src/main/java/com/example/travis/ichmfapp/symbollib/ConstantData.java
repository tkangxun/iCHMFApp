package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */

import android.Manifest;
import android.content.Context;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


import com.example.travis.ichmfapp.main.MainActivity;

import java.io.*;



/**
 *
 * @author Qu Xi
 */

public class ConstantData {

    private static AssetManager am;
    //private ArrayList<File> allFiles = new ArrayList<>();
    private static Context context = MainActivity.getAppContext();


    public ConstantData() {

    }


    //To save files in external storage
    static String root = Environment.getExternalStorageDirectory().toString();
    static File mydir = new File (root + "/iCHMF/");



    //These strings are use to read files from the assets folder
    public static String trainFile = "sample.dat";
    public static String modelFile = "model.dat";


    public static String ElasticFileString = "/elastic.dat";
    public static String ElasticFileDefaultString = "/elasticDefault.dat";


    public static File ElasticFile = new File(mydir + ElasticFileString);
    public static File ElasticFileDefault = new File(mydir + ElasticFileDefaultString);
    //public static String exeDir = parentpath + "\\php\\mathml.exe ";
    public static boolean doTest = false;

    public static String getAssest(String filename) {

        File f = new File(context.getCacheDir() + "/" + filename);
        if (!f.exists()) try {
            // if file does not exist, create cache file
            am = MainActivity.getAppContext().getAssets();
            InputStream is = am.open("file/" + filename);
            System.out.print("file: " + filename + "loaded");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            System.out.print("file: " + filename + "ERROR!!!");
            throw new RuntimeException(e);
        }
        return f.getPath();
    }







    /**public void saveFile (File savefile, String fname){

        if (!mydir.exists()){
            mydir.mkdirs();
        }
        fname = fname + ".dat";
        File file = new File(mydir, fname);
        if (file.exists()){
            file.delete();
        }
        try{
            FileOutputStream out = new FileOutputStream(file);

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}