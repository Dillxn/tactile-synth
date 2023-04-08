package com.dillxn.tactilesynth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class RecordPlayButtons extends FrameLayout {
    private Button armRecording;
    private Button playRecordButton;
    private boolean isRecordingArmed = false;
    private boolean isRecording = false;
    private Context mContext;

    private RecordingsUpdateListener mListener;

    private PlaybackHandler playback;

    public RecordPlayButtons(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RecordPlayButtons(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Update the constructor to accept the listener
    public RecordPlayButtons(Context context, RecordingsUpdateListener listener) {
        mContext = context;
        mListener = listener;
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.record_play_buttons, this, true);

        armRecording = findViewById(R.id.armRecording);
        playRecordButton = findViewById(R.id.playRecording);

        armRecording.setOnClickListener(view -> armRecording());
        playRecordButton.setOnClickListener(view -> playStop());

        playback = PlaybackHandler.getInstance();
    }

    private void armRecording() {
        isRecordingArmed = !isRecordingArmed;
        armRecording.setBackgroundColor(getCompatColor(isRecordingArmed ? R.color.armed : R.color.disarmed));
    }

    private void playStop() {
        if (isRecording) {
            // stop
            isRecording = false;
            playRecordButton.setBackgroundColor(getCompatColor(R.color.readyToRecord));
            playback.addRecording();
            RecordingsAdapter.getInstance().notifyDataSetChanged();
        } else if (isRecordingArmed) {
            // start recording
            // Add logic for handling recording start
            playRecordButton.setBackgroundColor(getCompatColor(R.color.recording));
            isRecording = true;
            playback.startRecording();
            playback.playSelected();
        } else {
            // start playing
            playRecordButton.setBackgroundColor(getCompatColor(R.color.readyToRecord));
            // Add logic for playing the recording
            playback.playSelected();
        }
    }

    private int getCompatColor(int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }
}
