package com.example.travis.ichmfapp.symbollib;

/**
 * Created by Travis on 23/8/2018.
 */


import android.Manifest;
import android.content.Context;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import com.example.travis.ichmfapp.main.MainActivity;

import java.io.*;



/**
 *
 * @author Qu Xi
 */

public class ConstantData {

    private static AssetManager am;
    private static Context context = MainActivity.getAppContext();


    public ConstantData() {

    }
    //To save files in internal storage
    static String root = Environment.getExternalStorageDirectory().toString();
    public static File mydir = new File (root + "/iCHMF/");

    //These strings are use to read files from the assets folder
    public static String trainFile = "sample.dat";
    public static String modelFile = "model.dat";

    //created files are read from internal storage
    public static String ElasticFileString = "elastic.dat";
    public static String ElasticFileDefaultString = "elasticDefault.dat";


    public static File ElasticFile = new File(mydir + ElasticFileString);
    public static File ElasticFileDefault = new File(mydir + ElasticFileDefaultString);

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


    public static void savetophone(){
        am = MainActivity.getAppContext().getAssets();
        String[] files = null;
        try {
            files = am.list("file");
        } catch (IOException e) {
            Toast.makeText(context, "fail to retrieve file string", Toast.LENGTH_SHORT).show();
        }

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

        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = am.open("file/"+ filename);
                File outFile = new File(mydir, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Toast.makeText(context, "fail to copy file", Toast.LENGTH_SHORT).show();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}