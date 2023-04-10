package com.dillxn.tactilesynth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class RecordingsMenuView extends LinearLayout {
    private RecordingsAdapter adapter;

    public RecordingsMenuView(Context context) {
        super(context);
        init(context);
    }

    public RecordingsMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.recordings_menu_view, this, true);
        ListView recordingList = findViewById(R.id.recording_list);

        ArrayList<float[]> recordings = ((MainActivity) context).playback.getRecordings();
        adapter = new RecordingsAdapter(context, recordings);
        recordingList.setAdapter(adapter);
    }
}
