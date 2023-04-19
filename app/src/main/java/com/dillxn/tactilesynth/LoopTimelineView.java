package com.dillxn.tactilesynth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LoopTimelineView extends RelativeLayout {
    private ImageView cursor;
    private ViewGroup rootView;

    public LoopTimelineView(Context context) {
        super(context);
        init(context);
    }

    public LoopTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.loop_timeline_view, this, true);
        cursor = rootView.findViewById(R.id.cursor);
    }

    public void updateCursorPosition(float progress) {
        int cursorX = (int) (rootView.getWidth() * progress);
        cursor.setX(cursorX);
    }
}
