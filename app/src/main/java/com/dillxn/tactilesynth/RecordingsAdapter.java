package com.dillxn.tactilesynth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
        CheckBox checkPlay = convertView.findViewById(R.id.check_box_play);
        
        boolean checked = PlaybackHandler.getInstance().selectedRecordings.contains(mRecordings.get(position));
        checkPlay.setChecked(checked);
        
        checkPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((MainActivity) mContext).playback.selectedRecordings.add((mRecordings.get(position)));
                } else {
                    ((MainActivity) mContext).playback.selectedRecordings.remove((mRecordings.get(position)));
                }
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method in PlaybackHandler to play recording
                ((MainActivity) mContext).playback.play(mRecordings.get(position));
            }
        });

        Button deleteButton = convertView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete the recording
                ((MainActivity) mContext).playback.deleteRecording(mRecordings.get(position));

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
