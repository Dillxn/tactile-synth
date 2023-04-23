package com.dillxn.tactilesynth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;

public class BlurBackground extends View {
    private Bitmap mBlurredBitmap;
    private Rect mSrcRect;
    private Rect mDstRect;

    public BlurBackground(Context context) {
        super(context);
    }

    public BlurBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlurBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBlurredBitmap(Bitmap blurredBitmap) {
        mBlurredBitmap = blurredBitmap;
        mSrcRect = new Rect(0, 0, blurredBitmap.getWidth(), blurredBitmap.getHeight());
        mDstRect = new Rect(0, 0, getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBlurredBitmap != null) {
            canvas.drawBitmap(mBlurredBitmap, mSrcRect, mDstRect, null);
        }
    }
}
