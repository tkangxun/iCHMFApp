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

import java.util.*;

import symbolFeature.SymbolFeature;

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
    private Button checker;
    private Button correct;
    private Button correction[] = new Button[5];
    private Button undo;

    private String result;
    private Stroke currentstroke;
    private NewtonAPI api;

    static Recognizer objreg;
    private Trainer trainer;


    //TODO: erase current expression, with time control overlay input method
    //TODO: store expression string




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        trainer = new Trainer();
        //api = new NewtonAPI();
        final TextView expression = (TextView)findViewById(R.id.result);
        writeView = (WriteView) findViewById(R.id.writeView);

        try{
            trainer.openSymbolLib();
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
                        result = objreg.Recognize(currentstroke);
                        correct.setVisibility(View.VISIBLE);
                        undo.setVisibility(View.VISIBLE);
                        correctionpanal(false);
                        expression.setText("Expression: " + result);
                        //api.getAnswer();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //Correction panel

        correction[0] = (Button) findViewById(R.id.correct0);
        correction[1] = (Button) findViewById(R.id.correct1);
        correction[2] = (Button) findViewById(R.id.correct2);
        correction[3] = (Button) findViewById(R.id.correct3);
        correction[4] = (Button) findViewById(R.id.correct4);


        //Correction button onclick initialisation, index 0 is skipped since it is the current exp shown
        // <editor-fold defaultstate="collapsed">
        correction[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    result = objreg.MakeCorrection(1);
                    correctionpanal(false);
                    expression.setText("Expression: " + result);

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        correction[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    result = objreg.MakeCorrection(2);
                    correctionpanal(false);
                    expression.setText("Expression: " + result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        correction[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    result = objreg.MakeCorrection(3);
                    correctionpanal(false);
                    expression.setText("Expression: " + result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        correction[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    result = objreg.MakeCorrection(4);
                    correctionpanal(false);
                    expression.setText("Expression: " + result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        correction[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    result = objreg.MakeCorrection(5);
                    correctionpanal(false);
                    expression.setText("Expression: " + result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
// </editor-fold>

        correctionpanal(false);

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
        checker = (Button) findViewById(R.id.check);
        correct = (Button) findViewById(R.id.correct);
        undo = (Button) findViewById(R.id.undo);
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
                                    simpleswitch.setChecked(false);

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
        checker.setVisibility(View.GONE);
        correct.setVisibility(View.GONE);
        undo.setVisibility(View.GONE);
        if (saved) {
            saveButton.setVisibility(View.GONE);
        }else{saveButton.setVisibility(View.VISIBLE);}

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for SVM, Sampling is collected, preprocessed , feature extraction,
                // then train and model created
                // input symbol only feature extraction then SVM classification
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
        checker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrokeList displayStrokes = trainer.getTrainsymbol(toTrain).getStrokes();
                writeView.displaySymbol(displayStrokes);

                Toast.makeText(MainActivity.this,"Number of Strokes: " + displayStrokes.size() , Toast.LENGTH_SHORT).show();

            }
        });
        correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show buttons
                correct.setVisibility(view.GONE);
                correctionpanal(true);


            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (writeView.getStrokeSize()==1){
                    writeView.clear();
                    objreg.ClearRecognitionMemory();
                    correctionpanal(false);
                    correct.setVisibility(View.GONE);
                    undo.setVisibility(View.GONE);
                    expression.setText("");
                    return;
                }

                try{
                    //undo stroke for recogniser
                    result = objreg.UndoLastStroke();
                    writeView.undoLastStroke();
                    expression.setText("Expression: " + result);

                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Can't undo!", Toast.LENGTH_SHORT).show();
                }



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
                    checker.setVisibility(View.VISIBLE);
                    expression.setText("");
                    correct.setVisibility(View.GONE);

                    //TODO: might wanna add back for training undo
                    undo.setVisibility(View.GONE);

                    correctionpanal(false);

                }else {
                    training = false;
                    trainButton.setVisibility(View.GONE);
                    //addSymbolButton.setVisibility(View.GONE);
                    removeButton.setVisibility(View.GONE);
                    svmButton.setVisibility(View.GONE);
                    checker.setVisibility(View.GONE);
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
                correctionpanal(false);
                correct.setVisibility(View.GONE);
                undo.setVisibility(View.GONE);
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
    private void correctionpanal(Boolean show){

        for (int i =0;i< correction.length; i++){
            if (show){
                char symbol =objreg.getOptionalRecognitionList().get(i+1    ).getSymbolChar();
                if (symbol != ' ') {
                    correction[i].setText(Character.toString(symbol));
                    correction[i].setVisibility(View.VISIBLE);
                }
            }else  {
                correction[i].setVisibility(View.GONE);
            }
        }

    }



}
