package com.example.travis.ichmfapp.main;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.travis.ichmfapp.R;
import com.example.travis.ichmfapp.preprocessor.Recognizer;
import com.example.travis.ichmfapp.symbollib.*;

import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private WriteView writeView;
    static Recognizer objreg;
    public String filenames;
    public Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        writeView = (WriteView) findViewById(R.id.writeView);

        TextView txtcontent = (TextView)findViewById(R.id.tv1);

        ConstantData constant = new ConstantData(context);




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
}
