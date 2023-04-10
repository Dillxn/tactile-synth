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
	private PlaybackHandler playback;
	private Handler loopHandler;
	private Runnable loopRunnable;

	private LoopTimelineView loopTimelineView;
	private long startTime;
	private int beatCount = 0;

	public Looper(LoopTimelineView loopTimelineView) {
		barsLength = Database.getInstance().getPreset().optInt("barsLength");
		playback = PlaybackHandler.getInstance();
		loopHandler = new Handler();
		calculateLoopInterval();
		this.loopTimelineView = loopTimelineView;
		progressListeners = new ArrayList<>();
	}

	private void calculateLoopInterval() {
		loopInterval = (long) (Metronome.getInstance().getBeatInterval() * 4 * barsLength);
	}

	public void startLoop(boolean shouldRecord, boolean metronomeEnabled) {
		beatCount = 0;
		startTime = System.currentTimeMillis();
		loopRunnable = new Runnable() {
			@Override
			public void run() {
				
					if (beatCount == 0) {
						// Start recording on first beat
						if (shouldRecord) {
							playback.startRecording();
						}
						playback.playSelected();
						
					}
					else if (beatCount % (4 * barsLength) == 0) {
						// first beat of loop n
						if (shouldRecord) {
							// Stop recording, add it to recordings
							playback.addRecording();
							// Start recording again
							playback.startRecording();
						}
						
						playback.stopSelected();
						playback.playSelected();
					}
				
					// Play metronome sound
					boolean isDownBeat = beatCount % 4 == 0;
					if (metronomeEnabled)
						Metronome.getInstance().playSound(isDownBeat);
						
					// notify progress listeners
					float progress = ((float) beatCount / (4 * barsLength)) % 1;
					notifyProgressListeners(progress);

					
					beatCount++;
					loopHandler.postDelayed(this, (long) Metronome.getInstance().getBeatInterval());
			}
		};
		loopHandler.postDelayed(loopRunnable, 0);
		Log.d("Looper", "Loop started");
	}

	public void stopLoop() {
		loopHandler.removeCallbacks(loopRunnable);
		playback.stopSelected();
		notifyProgressListeners(0);
		Log.d("Looper", "Loop stopped");
	}

	public void setLoopInterval(long interval) {
		loopInterval = interval;
	}
}
