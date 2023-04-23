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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordPlayButtons extends FrameLayout implements Looper.ProgressListener {
    private Button armRecordingBtn;
    private Button playStopButton;
    private boolean isRecordingArmed = false;
    public boolean isRecording = false;
    private boolean isPlaying = false;
    public boolean isMetronomePlaying = false;
    private boolean shouldUpdateCursorPosition = false;
    private Context mContext;
    
    private static RecordPlayButtons instance;
    public static synchronized RecordPlayButtons getInstance() {
        if (instance == null) {
            throw new RuntimeException("RecordPlayButtons not initialized");
        }
        return instance;
    }
    
    
    private PlaybackHandler playback;

    private Metronome metronome;

    private Button metronomeToggle;

    private Looper looper;

    private LoopTimelineView loopTimelineView;

    public RecordPlayButtons(@NonNull Context context) {
        super(context);
        init(context);
        instance = this;
    }

    public RecordPlayButtons(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        instance = this;
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

        // !!!!!!!!!!! TEST !!!!!!!!!!!
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
            looper.startLoop(false);
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

        looper.stopLoop(); // !!!!!!!!! TEST !!!!!!!!!
        //looper.stopLoop();
        loopTimelineView.updateCursorPosition(0);
        // save
        new Thread(() -> {
            PlaybackHandler.getInstance().saveAll();
        }).start();
    }

    private void startRecording() {
        // Countdown
        int countdownBeats = 4;
        playStopButton.setEnabled(false); // Disable the button during countdown

        Timer countdownTimer = new Timer();
        TimerTask countdownTask = new TimerTask() {
            int remainingBeats = countdownBeats;

            @Override
            public void run() {
                boolean isDownbeat = remainingBeats == countdownBeats; // Set to true on the first beat of the countdown

                if (remainingBeats > 0) {
                    // Play metronome sound on each beat of the countdown
                    metronome.playSound(isDownbeat);
                    remainingBeats--;
                } else {
                    // Start Recording on the 5th beat
                    shouldUpdateCursorPosition = true;
                    post(() -> {
                        playStopButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_stop_active));
                        playStopButton.setEnabled(true); // Enable the button after countdown
                    });
                    isRecording = true;

                    looper.startLoop(true);

                    // Cancel the Timer when done
                    countdownTimer.cancel();
                }
            }
        };

        countdownTimer.schedule(countdownTask, (long) metronome.getBeatInterval(), (long) metronome.getBeatInterval());
    }


    private void stopRecording() {
        isRecording = false;
        playback.addRecording();
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
