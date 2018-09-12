package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */
import android.content.Context;
import android.content.res.AssetManager;


import com.example.travis.ichmfapp.main.MainActivity;

import java.io.*;
import java.util.ArrayList;


/**
 *
 * @author Qu Xi
 */

public class ConstantData {

    private static AssetManager am;
    //private ArrayList<File> allFiles = new ArrayList<>();
    private static Context context = MainActivity.getAppContext();



    public ConstantData() {

        //context = MainActivity.getAppContext();

        /**String[] filenames = {"elastic.dat", "elasticDefault.dat", "model.dat","sample.dat","sample_original.dat"};

        for (int i = 0; i<filenames.length; i++) {
            try {
                String file = "file/" + filenames[i];
                InputStream inputStream = am.open(file);
                allFiles.add(new File(filenames[i]));
                OutputStream outputStream = new FileOutputStream(f);
         byte buffer[] = new byte[1024];
         int length = 0;

         while((length=inputStream.read(buffer)) > 0) {
         outputStream.write(buffer, 0, length);
         }
         outputStream.close();
         inputStream.close();
            } catch (Exception e) {
                System.out.print(filenames[i] + "not changed to file.");
                e.printStackTrace();
            }
        }}*/
    }

    static File dir = new File(("user.dir"));
    static String parentpath = dir.getParent();
    public static String trainFile = parentpath + "\\sample.dat";

    //model file past to svm_predict, sp.run then to svm file to create buffer
    public static String modelFile = "model.dat";
    public static String ElasticFileString = "elastic.dat";
    public static String ElasticFileDefaultString = parentpath + "\\elasticDefault.dat";
    public static File ElasticFile = new File(ElasticFileString);
    public static File ElasticFileDefault = new File(ElasticFileDefaultString);
    public static String exeDir = parentpath + "\\php\\mathml.exe ";
    public static boolean doTest = false;

    public static String getFile (String filename) {

        File f = new File(context.getCacheDir() + "/" +filename);
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
            throw new RuntimeException(e); }

        return f.getPath();
    }

    /*public  static InputStream gentFile(String fileName) {
        InputStream is = null;
        //BufferedReader br = null;

        try {
            is = am.open(fileName);
            //br = new BufferedReader(new InputStreamReader(is));
            System.out.print("file: " + fileName + "loaded");
            is.close();

        } catch (IOException e) {
            System.out.print("file: " + fileName + "ERROR!!!");
            e.printStackTrace();
        }
        return is;*/

    }



