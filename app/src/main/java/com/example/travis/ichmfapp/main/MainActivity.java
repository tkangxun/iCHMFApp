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
    private Button svmButton;

    private String recognizedSymbol;
    private Stroke currentstroke;

    static Recognizer objreg;
    private Trainer trainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        trainer = new Trainer();
        final TextView expression = (TextView)findViewById(R.id.result);
        writeView = (WriteView) findViewById(R.id.writeView);

        try{
            trainer.openSymbolLib();
            //char x = ':';
            //writeView.displaySymbol(trainer.getTrainsymbol(x).getStrokes());
            objreg = new Recognizer(
                SymbolLib.Load(ConstantData.ElasticFileString,
                SymbolLib.LibraryTypes.Binary));
        } catch (Exception e) {
            trainer.generateDefaultSetElastic();
            Toast.makeText(context, "Error! Elastic file not found!", Toast.LENGTH_SHORT).show();
        }

        writeView.addWriteViewListener(new WriteViewListener() {
            @Override
            public void StrokeEnd() {
                currentstroke = writeView.getLastStroke();
                if (training == false) {
                    try {
                        //objreg might not be initialise
                        recognizedSymbol = objreg.Recognize(currentstroke);
                        expression.setText("Expression: " + recognizedSymbol);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

            }
        });
        final Switch simpleswitch = (Switch) findViewById(R.id.simpleswitch);


        //train elastic symbol add strokes to the current list of symbol
        trainButton = (Button) findViewById(R.id.train);
        //save current library
        saveButton = (Button) findViewById(R.id.save);
        //add symbol add symbols on top of the default list not used
        //addSymbolButton = (Button) findViewById(R.id.add);
        //remove strokes existing symbol in library
        removeButton = (Button) findViewById(R.id.remove);
        svmButton = (Button) findViewById(R.id.svm);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        //alert dialog
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);
        builder.setTitle("Symbol Trainer");
        builder.setMessage("Please key unicode of the symbol you want to train or remove");
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
                                try {
                                    toTrain = SymbolLib.unicodeToChar(userInput.getText().toString());
                                    Toast.makeText(MainActivity.this, "Symbol: " + toTrain, Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Toast.makeText(MainActivity.this, "Please enter only unicode!", Toast.LENGTH_SHORT).show();
                                    //alertdialog.show();  symbol cant be resolved

                                }
                                Toast.makeText(MainActivity.this, "Symbol: " + toTrain, Toast.LENGTH_SHORT).show();
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




        trainButton.setVisibility(View.GONE);
        //addSymbolButton.setVisibility(View.GONE);
        removeButton.setVisibility(View.GONE);
        svmButton.setVisibility(View.GONE);
        if (saved) {
            saveButton.setVisibility(View.GONE);
        }else{saveButton.setVisibility(View.VISIBLE);}

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //for SVM, Sampling is collected, preprocessed , feature extraction, then train and model created
                //input symbol only feature extraction then SVM classification

                training = true;
                Toast.makeText(MainActivity.getAppContext(), "Training symbol: " + toTrain+ ", strokes: " + writeView.getStrokes().size(), Toast.LENGTH_SHORT).show();

                //Training for elastic matching
                trainer.addElasticSymbol(toTrain, writeView.getStrokes());
                saved = Boolean.FALSE;
                simpleswitch.setChecked(false);
                training = false;
            }
        });

        /**
        addSymbolButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.show();
                        trainer.addSymbol(toTrain);
                        simpleswitch.setChecked(false);
                        Toast.makeText(MainActivity.this, "Symbol "+ toTrain + " added", Toast.LENGTH_SHORT).show();

                    }
                });*/

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

                trainer.removeSymbol(toTrain);
                simpleswitch.setChecked(false);
            }
        });
        svmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                trainer.trainSymbolSVM(toTrain, writeView.getStrokes());
                simpleswitch.setChecked(false);
            }
        });







        simpleswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    writeView.clear();
                    alertDialog.show();
                    training = true;
                    trainButton.setVisibility(View.VISIBLE);
                    //addSymbolButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);
                    removeButton.setVisibility(View.VISIBLE);
                    svmButton.setVisibility(View.VISIBLE);
                }else {
                    training = false;
                    trainButton.setVisibility(View.GONE);
                    //addSymbolButton.setVisibility(View.GONE);
                    removeButton.setVisibility(View.GONE);
                    svmButton.setVisibility(View.GONE);
                    if (saved) {
                        saveButton.setVisibility(View.GONE);
                    }else{saveButton.setVisibility(View.VISIBLE);}

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
                objreg.ClearRecognitionMemory();
                expression.setText("");


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
