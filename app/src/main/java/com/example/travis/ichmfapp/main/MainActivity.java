package com.example.travis.ichmfapp.main;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.travis.ichmfapp.R;
import com.example.travis.ichmfapp.preprocessor.*;
import com.example.travis.ichmfapp.symbollib.*;


import java.util.ArrayList;

import symbolFeature.SVM_predict;
import symbolFeature.SymbolFeature;

public class MainActivity extends AppCompatActivity
 //       implements WriteViewListener
{

    private WriteView writeView;
    private static Context context;
    static Recognizer objreg;
    private char toTrain;
    private StrokeList currentstrokes;
    private Boolean training = Boolean.FALSE;
    private Button  trainButton;
    private Button  saveButton;
    private String recognizedSymbol;

    //private SymbolRecognizer _manualRecognizer;
    private SymbolRecognizer_SVM _svmRecognizer;

    //private EditText result;

    private Trainer trainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        //SVM_predict sp = new SVM_predict();


        writeView = (WriteView) findViewById(R.id.writeView);
        writeView.addWriteViewListener(new WriteViewListener() {
            @Override
            public void StrokeEnd() {
                //Toast.makeText(MainActivity.this, "Getting strokes: " + writeView.getStrokes().size(), Toast.LENGTH_SHORT).show();
                writeView.getStrokes();
            }
        });




        trainer = new Trainer();
        //for SVM, Sampling is collected, preprocessed , feature extraction, then train and model created
        //input symbol only feature extraction then SVM classification


        /**
        saveButton = (Button) findViewById(R.id.button2);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }*/






        final Switch simpleswitch = (Switch) findViewById(R.id.simpleswitch);


        //Training button
        trainButton = (Button) findViewById(R.id.button1);
        trainButton.setVisibility(View.GONE);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //for SVM, Sampling is collected, preprocessed , feature extraction, then train and model created
                //input symbol only feature extraction then SVM classification

                training = true;
                Toast.makeText(MainActivity.getAppContext(), "Training symbol, strokes: " + writeView.getStrokes().size(), Toast.LENGTH_SHORT).show();

                /** for training
                //send for processing, should account for symbol training only 4 strokes (not added yet)
                StrokeList processedStrokes = getprocessed(writeView.getStrokes());
                Toast.makeText(MainActivity.this, "Before: "+ PreprocessorSVM.countTotalPoint(writeView.getStrokes())
                + ". After: " + PreprocessorSVM.countTotalPoint(processedStrokes), Toast.LENGTH_SHORT).show();

                //send for feature extraction
                String featureString = SymbolFeature.getFeature(0, processedStrokes);
                Toast.makeText(context, featureString, Toast.LENGTH_SHORT).show();
                 */



                //run SVM
                recognizedSymbol = recognize(writeView.getStrokes());
                Toast.makeText(MainActivity.this, recognizedSymbol, Toast.LENGTH_LONG).show();





                simpleswitch.setChecked(false);
                training = false;


            }
        });
        //training mode



        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);
        builder.setTitle("Symbol Trainer");
        builder.setMessage("Please key unicode of new symbol");
        builder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        builder
                .setCancelable(false)
                .setPositiveButton("SUBMIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text


                                toTrain = SymbolLib.unicodeToChar(userInput.getText().toString());
                                Toast.makeText(MainActivity.this, "Symbol to train: " + toTrain , Toast.LENGTH_SHORT).show();
                                //writeView.getStrokes();
                                //trainer.trainSymbolSVM(toTrain);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = builder.create();


        simpleswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    training = Boolean.TRUE;
                    trainButton.setVisibility(View.VISIBLE);

                    alertDialog.show();

                }else{
                    trainButton.setVisibility(View.GONE);
                }
            }
        });





        TextView txtcontent = (TextView)findViewById(R.id.tv1);
/**
        try {



            StrokeList preProcessedStrokeList = PreprocessorSVM.preProcessing(writeView.getStrokes());
            Toast.makeText(context, "preprocessed", Toast.LENGTH_SHORT).show();
            //compare storkelist with each symbol in symbol library
            String featureString = SymbolFeature.getFeature(0, preProcessedStrokeList);
            SVM_predict sp = new SVM_predict();
            sp.run(featureString, 1);

        }catch (Exception e){
            e.printStackTrace();
        }

*/
        /**
            Recognizer objreg = new Recognizer(
                    SymbolLib.Load(ConstantData.ElasticFileString,
                            SymbolLib.LibraryTypes.Binary));
        } catch (Exception e) {
            e.printStackTrace();
        }
         */


        ////////////////////////
        /**
        try {
            objreg = new Recognizer(
                    SymbolLib.Load(ConstantData.ElasticFileString,
                            SymbolLib.LibraryTypes.Binary));

            String pointList = "";
            Stroke receivedStroke = new Stroke();
            int pointNum = Integer.parseInt(st.nextToken());

            for (int i = 0; i < pointNum; i++) {
                StrokePoint sp = new StrokePoint(Double.parseDouble(st2.nextToken()), Double.parseDouble(st2.nextToken()));
                receivedStroke.addStrokePoint(sp);
            }
            objreg.Recognize(receivedStroke);

        } catch (Exception e) {
            e.printStackTrace();
        }



        */
        ///////////////////////


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "clear", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                /** place icon action here! */
                writeView.clear();
                //trainer.addSymbol(toTrain);
                //trainer.saveSymbolLib();
            }
        });
    }



    public static Context getAppContext(){
        return MainActivity.context;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public StrokeList getprocessed (StrokeList rawStroke){
        StrokeList processed = PreprocessorSVM.preProcessing(rawStroke);

        return processed;
    }

    private String recognize (StrokeList memory){
        SymbolRecognizer_SVM svmRecognizer = _svmRecognizer;
        if (svmRecognizer == null) {
            svmRecognizer = new SymbolRecognizer_SVM();
        }
        _svmRecognizer = svmRecognizer;
        ArrayList mResult =null;
        String result = "";

        try{
            mResult = svmRecognizer.recognizing(memory);
        }catch (Exception e){
            e.printStackTrace();
        }


        for (int i =0; i < mResult.size(); i++){
            result += mResult.get(i);
        }

        return result;
    }

}
