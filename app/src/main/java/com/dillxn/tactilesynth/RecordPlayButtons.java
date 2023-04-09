package com.dillxn.tactilesynth;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class RecordPlayButtons extends FrameLayout implements Looper.ProgressListener {
    private Button armRecordingBtn;
    private Button playStopButton;
    private boolean isRecordingArmed = false;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isMetronomePlaying = false;
    private boolean shouldUpdateCursorPosition = false;
    private Context mContext;

    private PlaybackHandler playback;

    private Metronome metronome;

    private Button metronomeToggle;

    private Looper looper;

    private LoopTimelineView loopTimelineView;

    public RecordPlayButtons(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RecordPlayButtons(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.record_play_buttons, this, true);

        armRecordingBtn = findViewById(R.id.armRecording);
        playStopButton = findViewById(R.id.playRecording);

        armRecordingBtn.setOnClickListener(view -> armRecording());
        playStopButton.setOnClickListener(view -> playStop());

        playback = PlaybackHandler.getInstance();
        metronome = Metronome.getInstance();

        metronomeToggle = findViewById(R.id.metronomeToggle);
        metronomeToggle.setOnClickListener(view -> toggleMetronome());

        shouldUpdateCursorPosition = false;
        // set up looping
        loopTimelineView = findViewById(R.id.loopTimelineView);

        looper = Looper.getInstance(loopTimelineView);
        looper.addProgressListener((Looper.ProgressListener) this);
        loopTimelineView.updateCursorPosition(0);

    }

    private void armRecording() {
        isRecordingArmed = !isRecordingArmed;
        armRecordingBtn.setBackgroundDrawable(ContextCompat.getDrawable(mContext,
                isRecordingArmed ? R.drawable.btn_record_active : R.drawable.btn_record));
    }

    private void playStop() {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        isPlaying = true;
        // set background image to @drawable/btn_stop_active
        playStopButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_stop_active));

        if (isRecordingArmed) {
            shouldUpdateCursorPosition = false;
            startRecording();
        } else {
            shouldUpdateCursorPosition = true;
            looper.startLoop(isRecording, isMetronomePlaying);
        }

    }

    private void stopPlayback() {
        shouldUpdateCursorPosition = false;
        isPlaying = false;
        // set background image to @drawable/btn_play
        playStopButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_play));

        if (isRecording) {
            stopRecording();
        }

        looper.stopLoop();
        loopTimelineView.updateCursorPosition(0);

    }

    private void startRecording() {
        // Countdown
        int countdownBeats = 4;
        playStopButton.setEnabled(false); // Disable the button during countdown
        metronome.playMetronome(); // Play the metronome during countdown


        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Start Recording
            shouldUpdateCursorPosition = true;
            playStopButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_stop_active));
            playStopButton.setEnabled(true); // Enable the button after countdown
            isRecording = true;

            if (!isMetronomePlaying)
                metronome.stopMetronome();

            playback.startRecording();
            playback.playSelected();

            looper.startLoop(isRecording, isMetronomePlaying);
        }, (long) (metronome.getBeatInterval() * countdownBeats));
    }

    private void stopRecording() {
        isRecording = false;
        playback.addRecording();
        metronome.stopMetronome();
    }

    private void toggleMetronome() {
        isMetronomePlaying = !isMetronomePlaying;
        metronomeToggle.setBackgroundDrawable(ContextCompat.getDrawable(mContext,
                isMetronomePlaying ? R.drawable.btn_metronome_active : R.drawable.btn_metronome));
    }

    @Override
    public void onProgressUpdate(float progress) {
        if (shouldUpdateCursorPosition) {
            loopTimelineView.updateCursorPosition(progress);
        }
    }
}
