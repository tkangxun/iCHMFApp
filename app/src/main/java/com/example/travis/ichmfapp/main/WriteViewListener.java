package com.example.travis.ichmfapp.main;

import java.util.EventListener;

/**
 * Created by Travis on 24/9/2018.
 */

public interface WriteViewListener extends EventListener {

    //public void StrokeStart(WriteViewEvent evt);
    void StrokeEnd();
    //public void CanvasCleared(WriteViewEvent evt);
}
