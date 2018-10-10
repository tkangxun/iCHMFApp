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



public class MainActivity extends AppCompatActivity {


    private WriteView writeView;
    private static Context context;

    private char toTrain;
    private Boolean training = Boolean.FALSE;
    private Boolean saved = Boolean.TRUE;
    private Button  trainButton;
    private Button  saveButton;
    private Button addSymbolButton;
    private Button removeButton;


    private String recognizedSymbol;
    private Stroke currentstroke;



    static Recognizer objreg;
    private SymbolRecognizer_SVM _svmRecognizer;


    //private EditText result;
    private Trainer trainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        trainer = new Trainer();
        final TextView txtcontent = (TextView)findViewById(R.id.tv1);


        try{
            trainer.openSymbolLib();
            objreg = new Recognizer(
                SymbolLib.Load(ConstantData.ElasticFileString,
                SymbolLib.LibraryTypes.Binary));





         } catch (Exception e) {
            trainer.generateDefaultSetElastic();


            Toast.makeText(context, "Error! Elastic file not found!", Toast.LENGTH_SHORT).show();
         }

     writeView = (WriteView) findViewById(R.id.writeView);
        writeView.addWriteViewListener(new WriteViewListener() {
            @Override
            public void StrokeEnd() {

                //Toast.makeText(MainActivity.this, "Getting strokes: " + writeView.getStrokes().size(), Toast.LENGTH_SHORT).show();
                currentstroke = writeView.getLastStroke();

                if (training == false) {
                    try {
                        //objreg might not be initialise
                        recognizedSymbol = objreg.Recognize(currentstroke);
                        //Toast.makeText(MainActivity.this, recognizedSymbol, Toast.LENGTH_SHORT).show();
                        txtcontent.setText("Expression: " + recognizedSymbol);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //maybe use SVM only
                    }
                }

            }
        });

        //for SVM, Sampling is collected, preprocessed , feature extraction, then train and model created
        //input symbol only feature extraction then SVM classification








        final Switch simpleswitch = (Switch) findViewById(R.id.simpleswitch);



        //train elastic symbol add strokes to the current list of symbol
        trainButton = (Button) findViewById(R.id.button1);
        //save current library
        saveButton = (Button) findViewById(R.id.button2);
        //add symbol add symbols on top of the default list
        addSymbolButton = (Button) findViewById(R.id.button3);
        //remove current symbol
        removeButton = (Button) findViewById(R.id.button4);

        trainButton.setVisibility(View.GONE);
        addSymbolButton.setVisibility(View.GONE);
        if (saved) {
            saveButton.setVisibility(View.GONE);
        }else{saveButton.setVisibility(View.VISIBLE);}

        //add symbol add symbols on top of the default list






        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //for SVM, Sampling is collected, preprocessed , feature extraction, then train and model created
                //input symbol only feature extraction then SVM classification

                training = true;
                Toast.makeText(MainActivity.getAppContext(), "Training symbol, strokes: " + writeView.getStrokes().size(), Toast.LENGTH_SHORT).show();


                //Training for elastic matching
                saved = Boolean.FALSE;
                trainer.addElasticSymbol(toTrain, writeView.getStrokes());

                simpleswitch.setChecked(false);
                training = false;


                Toast.makeText(MainActivity.getAppContext(), "to be passed :" + toTrain, Toast.LENGTH_SHORT).show();
                simpleswitch.setChecked(false);


            }
        });

        addSymbolButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        trainer.addSymbol(toTrain);

                    }
                });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                trainer.saveSymbolLib();
                saved = Boolean.TRUE;
                saveButton.setVisibility(view.GONE);

                try{
                    objreg = new Recognizer(
                            SymbolLib.Load(ConstantData.ElasticFileString,
                                    SymbolLib.LibraryTypes.Binary));
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: need create another dialog box

                trainer.removeSymbol(toTrain);


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
                                simpleswitch.setChecked(false);
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = builder.create();


        simpleswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    training = Boolean.TRUE;
                    trainButton.setVisibility(View.VISIBLE);
                    addSymbolButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);
                    removeButton.setVisibility(View.GONE);

                    //TODO: tidy up diff btw train and add symbol, remove symbol


                    alertDialog.show();

                }else {
                    trainButton.setVisibility(View.GONE);
                    addSymbolButton.setVisibility(View.GONE);
                    if (saved) {
                        saveButton.setVisibility(View.GONE);
                    }else{saveButton.setVisibility(View.VISIBLE);}
                    removeButton.setVisibility(View.VISIBLE);
                }
            }
        });





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
                //objreg.ClearRecognitionMemory();

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

    private static String getOptionalResult() {
        String result = "";
        ArrayList arr = objreg.getOptionalRecognitionList();
        for (int i = 1; i < arr.size(); i++) {
            if (i == arr.size() - 1) {
                result += arr.get(i);
            } else {
                result += arr.get(i) + "-";
            }
        }
        return result;
    }


}
