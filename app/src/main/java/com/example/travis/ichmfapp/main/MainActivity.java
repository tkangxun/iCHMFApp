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
import com.example.travis.ichmfapp.preprocessor.PreprocessorSVM;
import com.example.travis.ichmfapp.preprocessor.Recognizer;
import com.example.travis.ichmfapp.symbollib.*;



import symbolFeature.SVM_predict;
import symbolFeature.SymbolFeature;

public class MainActivity extends AppCompatActivity implements WriteViewListener{

    private WriteView writeView;
    private static Context context;
    static Recognizer objreg;
    private StrokeList currentstrokes;
    private Boolean training = Boolean.FALSE;
    private Button  trainButton;
    //private EditText result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);


        writeView = (WriteView) findViewById(R.id.writeView);
        writeView.addWriteViewListener(this);




        final Switch simpleswitch = (Switch) findViewById(R.id.simpleswitch);


        //Training button
        trainButton = (Button) findViewById(R.id.button1);
        trainButton.setVisibility(View.GONE);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeView.getStrokes();
                Toast.makeText(MainActivity.getAppContext(), "Training symbol", Toast.LENGTH_SHORT).show();
                simpleswitch.setChecked(false);


            }
        });
        //training mode

        //result = (EditText) findViewById(R.id.editTextResult);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);
        builder.setTitle("Symbol Trainer");
        builder.setMessage("Please insert  symbol to train");
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

                                String toTrain = userInput.getText().toString();

                                //do not know why but toast is not working, might be something to do with the view or final
                                Toast.makeText(MainActivity.getAppContext(), "training " + toTrain, Toast.LENGTH_SHORT).show();
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

        /**try {
            StrokeList preProcessedStrokeList = PreprocessorSVM.preProcessing(writeView.getStrokes());
            //compare storkelist with each symbol in symbol library _quxi
            String featureString = SymbolFeature.getFeature(0, preProcessedStrokeList);
            SVM_predict sp = new SVM_predict();
            sp.run(featureString,1);



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
                Snackbar.make(view, "undo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                /** place icon action here! */
                writeView.undoLastStroke();
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
    public void StrokeEnd(WriteViewEvent evt) {
        Toast.makeText(context, ("maybe can get my strokes"), Toast.LENGTH_SHORT).show();
    }
}
