package com.dillxn.tactilesynth;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

public class Metronome {
    private int sampleRate;
    private int bpm;
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
        bpm = Database.getInstance().getPreset().optInt("bpm");

        createAudioTrack();

    }

    // getBeatInterval() returns the time in milliseconds between each beat
    public double getBeatInterval() {
        //60,000 milliseconds in a minute
        return 60000.0 / bpm;
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

    public void playSound(boolean isDownBeat) {
        System.out.println("Playsound called");

        if (!isPlaying) {
            isPlaying = true;
            audioTrack.play();
        }

        short[] soundBuffer = createMetronomeSound(isDownBeat);
        audioTrack.write(soundBuffer, 0, soundBuffer.length);

        // Stop the AudioTrack after a small delay to let the sound play fully
        new Handler().postDelayed(() -> {
            if (isPlaying) {
                isPlaying = false;
                audioTrack.pause();
                audioTrack.setPlaybackHeadPosition(0);
            }
        }, 150); // Adjust the delay as needed
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
