package com.dillxn.tactilesynth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class Touchpad extends View {
    Canvas canvas;
    int xres, yres;

    public Touchpad(Context context) {
        super(context);
    }

    public Touchpad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // forward touch event to main activity
        ((Activity) getContext()).onTouchEvent(event);
        return true;
    }

    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        xres = canvas.getClipBounds().width();
        yres = canvas.getClipBounds().height();

        // draw();
    }

    public void draw() {
        invalidate();
        // draw most recent tap
    }
}
