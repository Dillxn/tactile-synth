package com.dillxn.tactilesynth;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class Metronome {
    private int sampleRate;
    private int beat;
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    
    public static Metronome instance;
    public static synchronized Metronome getInstance() {
        if (instance == null) {
            instance = new Metronome();
        }
        return instance;
    }

    public Metronome() {
        
        sampleRate = Synth.getInstance().getSampleRate();
        beat = Database.getInstance().getPreset().optInt("bpm");

        createAudioTrack();
        
        instance = this;
    }

    // getBeatInterval() returns the time in milliseconds between each beat
    public double getBeatInterval() {
        //60,000 milliseconds in a minute
        return 60000.0 / beat;
    }

    private void createAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);

        float volume = 0.5f; // 50% volume reduction
        audioTrack.setVolume(volume);
    }

    public void playMetronome() {
        if (!isPlaying) {
            isPlaying = true;
            audioTrack.play();
            playLoop();
        }

        Log.d("Metronome", "Playing metronome");
    }

    public void stopMetronome() {
        if (isPlaying) {
            isPlaying = false;
            audioTrack.pause(); // Add this line to pause the playback before stopping
            audioTrack.stop();
            audioTrack.flush(); // Add this line to clear the audio buffer
            audioTrack.setPlaybackHeadPosition(0);
        }
        beatCounter = 0;
    }

    private void playLoop() {
        double interval = getBeatInterval();
        Handler handler = new Handler();
    
        Runnable metronomeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    playSound();
                    handler.postDelayed(this, (long) interval);
                }
            }
        };
        handler.postDelayed(metronomeRunnable, 0); // Change the delay to 0 for the first beat
    }

    private int beatCounter = 0;

    private void playSound() {
        boolean isDownBeat = beatCounter % 4 == 0; // Assume 4 beats per bar
        short[] soundBuffer = createMetronomeSound(isDownBeat);
        audioTrack.write(soundBuffer, 0, soundBuffer.length);

        beatCounter++; // Increment the beat counter
    }


    private short[] createMetronomeSound(boolean isDownBeat) {
        double frequency = 1000.0;
        double duration = 0.1; // 100ms sound
        int numSamples = (int) (duration * sampleRate);
        double[] soundBuffer = new double[numSamples];
        short[] outputBuffer = new short[numSamples];
        float volume = 0.1f; // 50% volume reduction
        float downBeatVolume = 0.6f; // Adjust downbeat volume separately

        for (int i = 0; i < numSamples; ++i) {
            if (isDownBeat) {
                // Square wave for downbeat
                soundBuffer[i] = (i % (sampleRate / frequency) < (sampleRate / (2 * frequency))) ? 1.0 : -1.0;
                outputBuffer[i] = (short) (soundBuffer[i] * (Short.MAX_VALUE / 2) * volume * downBeatVolume);
            } else {
                // Sine wave for other beats
                soundBuffer[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency));
                outputBuffer[i] = (short) (soundBuffer[i] * Short.MAX_VALUE * volume);
            }
        }

        return outputBuffer;
    }



}
