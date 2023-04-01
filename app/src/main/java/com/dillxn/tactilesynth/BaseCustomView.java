package com.dillxn.tactilesynth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class BaseCustomView extends LinearLayout {

    public BaseCustomView(Context context) {
        super(context);
        init(context);
    }

    public BaseCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutResId(), this, true);
        onInit();
    }

    protected abstract int getLayoutResId();
    protected abstract void onInit();


}
