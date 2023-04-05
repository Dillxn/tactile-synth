package com.dillxn.tactilesynth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordingsAdapter extends ArrayAdapter<float[]> {

    private Context mContext;
    private ArrayList<float[]> mRecordings;

    public RecordingsAdapter(Context context, ArrayList<float[]> recordings) {
        super(context, 0, recordings);
        mContext = context;
        mRecordings = recordings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recording_item, parent, false);
        }

        TextView recordingNameTextView = convertView.findViewById(R.id.recording_name_text_view);
        recordingNameTextView.setText("Recording " + (position + 1));

        Button playButton = convertView.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method in PlaybackHandler to play recording
                ((MainActivity) mContext).playback.play(mRecordings.get(position));
            }
        });

        return convertView;
    }
}
