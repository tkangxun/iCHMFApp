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








/**
 * Created by Travis on 7/8/2018.
 */

public class WriteView extends View {


    private Bitmap mBitmap;
    private Canvas mCanvas;
    private static Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static WriteViewListener wvlistener;


    private static final float TOUCH_TOLERANCE = 4;


//    public int _strokeSize;

    private StrokePoint _startPoint, _endPoint;
    private static StrokeList _strokes;
    private Stroke _currentStroke;



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
        _strokes = new StrokeList();
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

                _startPoint = new StrokePoint(x,y);
                _currentStroke = new Stroke();
                _currentStroke.addStrokePoint(_startPoint);

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

        return true;
    }


    public void clear() {
        mPath = new Path();
        mCanvas = new Canvas();
        _strokes = new StrokeList();
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
        if (_strokes.size()<2){
            mPath.reset();
            this.clear();
            return;
        }

        _strokes.remove(_strokes.size()-1);
        mPath = new Path();
        invalidate();

        for (int i =0; i<_strokes.size();i++){
            for (int j=0; j<_strokes.get(i).getTotalStrokePoints();j++){
                if (_strokes.get(i).getTotalStrokePoints()==1){
                    touchStart((float)_strokes.get(i).getStrokePoint(j).X,
                            (float)_strokes.get(i).getStrokePoint(j).Y);
                    touchMove((float)_strokes.get(i).getStrokePoint(j).X,
                            (float)_strokes.get(i).getStrokePoint(j).Y);
                    touchUp();
                    continue;
                }
                if (j==0){
                    touchStart((float)_strokes.get(i).getStrokePoint(j).X,
                            (float)_strokes.get(i).getStrokePoint(j).Y);
                    continue;
                }
                if (j== _strokes.get(i).getTotalStrokePoints()-1){
                    touchUp();
                    continue;
                }
                touchMove((float)_strokes.get(i).getStrokePoint(j).X,
                        (float)_strokes.get(i).getStrokePoint(j).Y);
            }
        }



    }

    public void displaySymbol (StrokeList symbolStrokes){
        this.clear();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(20);

        for (int i =0; i<symbolStrokes.size();i++){
            for (int j=0; j<symbolStrokes.get(i).getTotalStrokePoints();j++){
                if (symbolStrokes.get(i).getTotalStrokePoints()==1){
                    touchStart((float)symbolStrokes.get(i).getStrokePoint(j).X +500,
                            (float)symbolStrokes.get(i).getStrokePoint(j).Y+500);
                    touchMove((float)symbolStrokes.get(i).getStrokePoint(j).X +500,
                            (float)symbolStrokes.get(i).getStrokePoint(j).Y+500);
                    touchUp();
                    continue;
                }
                if (j==0){
                    touchStart((float)symbolStrokes.get(i).getStrokePoint(j).X +500,
                            (float)symbolStrokes.get(i).getStrokePoint(j).Y+500);
                    continue;
                }
                if (j== symbolStrokes.get(i).getTotalStrokePoints()-1){
                    touchUp();
                    continue;
                }
                touchMove((float)symbolStrokes.get(i).getStrokePoint(j).X+500,
                        (float)symbolStrokes.get(i).getStrokePoint(j).Y+500);
            }
        }

    }

    public void addWriteViewListener(WriteViewListener eventListener){
        this.wvlistener = eventListener;
    }


}
