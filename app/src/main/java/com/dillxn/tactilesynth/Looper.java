package com.dillxn.tactilesynth;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Looper {

    private static Looper instance;
    private List<ProgressListener> progressListeners;
    public static synchronized Looper getInstance(LoopTimelineView loopTimelineView) {
        if (instance == null) {
            instance = new Looper(loopTimelineView);
        }
        return instance;
    }
    public interface ProgressListener {
        void onProgressUpdate(float progress);
    }

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    private void notifyProgressListeners(float progress) {
        for (ProgressListener listener : progressListeners) {
            listener.onProgressUpdate(progress);
        }
    }

    private int barsLength;
    private long loopInterval;
    private boolean isLooping = false;
    public boolean loopEnabled = true;
    private PlaybackHandler playback;
    private Handler loopHandler;
    private Handler cursorHandler;
    private Runnable loopRunnable;
    private Runnable cursorRunnable;

    private LoopTimelineView loopTimelineView;
    private long startTime;

    public Looper(LoopTimelineView loopTimelineView) {
        barsLength = Database.getInstance().getPreset().optInt("barsLength");
        playback = PlaybackHandler.getInstance();
        loopHandler = new Handler();
        cursorHandler = new Handler();
        calculateLoopInterval();
        this.loopTimelineView = loopTimelineView;
        progressListeners = new ArrayList<>();
    }

    private void calculateLoopInterval() {
        loopInterval = (long) (Metronome.getInstance().getBeatInterval() * 4 * barsLength);
    }

    public void setLoopEnabled(boolean enabled) {
        loopEnabled = enabled;
    }

    public void startLoop() {
        startTime = System.currentTimeMillis();
        if (loopEnabled) {
            isLooping = true;
            loopRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLooping) {
                        Log.d("Looper", "Stopping and starting playback in loop");
                        Log.d("Looper", "Loop interval: " + loopInterval);
                        playback.stopSelected();


                        if (playback.isRecording()) {
                            playback.addRecording();
                            playback.startRecording();
                        }

                        playback.playSelected();



                        loopHandler.postDelayed(this, loopInterval);
                    }
                }
            };
            loopHandler.postDelayed(loopRunnable, loopInterval);
            Log.d("Looper", "Loop started");

            cursorRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isLooping) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        float progress = ((float) elapsedTime / loopInterval) % 1;
                        notifyProgressListeners(progress);
                        cursorHandler.postDelayed(this, 50); // Update every 50 ms for smooth animation
                    }
                }
            };
            cursorHandler.postDelayed(cursorRunnable, 50);
        }
    }

    public void stopLoop() {
        isLooping = false;
        loopHandler.removeCallbacks(loopRunnable);
        cursorHandler.removeCallbacks(cursorRunnable);
        notifyProgressListeners(0);
        Log.d("Looper", "Loop stopped");
    }

    public void setLoopInterval(long interval) {
        loopInterval = interval;
    }
}
