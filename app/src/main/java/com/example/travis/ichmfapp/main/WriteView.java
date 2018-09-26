package com.example.travis.ichmfapp.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.travis.ichmfapp.symbollib.Stroke;
import com.example.travis.ichmfapp.symbollib.StrokeList;
import com.example.travis.ichmfapp.symbollib.StrokePoint;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;

/**
 * Created by Travis on 7/8/2018.
 */

public class WriteView extends View {

    private List _listeners = new ArrayList();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private Context context = MainActivity.getAppContext();
    private static WriteViewListener wvlistener;


    private static final float TOUCH_TOLERANCE = 4;
    //private double delay = 0.001;
    //private Instant starts = Instant.now();

//    public int _strokeSize;

    private StrokePoint _startPoint, _endPoint;
    private StrokeList _strokes;
    private Stroke _currentStroke = new Stroke();


    public WriteView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){

        // initialise paint display
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(50);


        mPath = new Path();
        mCanvas = new Canvas();
        //DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();

    }

    //update canvas upon invalidate()
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    private void touchStart(float x, float y) {
        //Instant ends = Instant.now();
        //Duration duration = Duration.between(starts, ends);
        // new symbol created if more than delay
        //if (duration.getSeconds() >= delay) {
        //    mPath = new Path();
         //   _strokes = new StrokeList();

        //}


        // the 4 stroke thingu might be captured in strokelist class

        //mPath = new Path();
        //_strokes = new StrokeList();




        mPath.moveTo(x, y);
        this.mX = x;
        this.mY = y;
    }
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public synchronized boolean  onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                //Toast.makeText(context, ("Coordinates x: "+ x+ "  y:" + y), Toast.LENGTH_SHORT).show();
                this._startPoint = new StrokePoint(x,y);
                this._currentStroke = new Stroke();
                this._currentStroke.addStrokePoint(this._startPoint);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                this._endPoint = new StrokePoint(x,y);
                this._currentStroke.addStrokePoint(this._endPoint);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                this._strokes.add(_currentStroke);
                _currentStroke = null;
                _startPoint = null;
                _endPoint = null;

                //fire end of stroke
                wvlistener.StrokeEnd();

                invalidate();
                break;
        }
        //starts = Instant.now();
        return true;
    }


    //widgets
    public void clear() {
        mPath.reset();
        mPath = new Path();
        invalidate();
    }

    /*public void setStrokeSize(int ss) {
        _strokeSize = ss;
    }*/

    public int getStrokeSize() {
        return _strokes.size();
    }

    public void setStrokeList(StrokeList sl) {
        _strokes = sl;
    }

    public Stroke getLastStroke() {
        return this._strokes.get(_strokes.size() - 1);
    }

    public StrokeList getStrokes() {
        return _strokes;
    }

    public void undoLastStroke() {
        if (this.getStrokeSize()<2){
            mPath.reset();
            this.clear();
        }
        this._strokes.remove(_strokes.size() - 1);
        mPath.setLastPoint(mX,mY);


        invalidate();
    }

    public void addWriteViewListener(WriteViewListener eventListener){
        this.wvlistener = eventListener;
        Toast.makeText(MainActivity.getAppContext(), "listener added", Toast.LENGTH_SHORT).show();
    }


}
