package com.dillxn.tactilesynth;

import static java.lang.Math.min;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class Synth {

    // bridge events
    static {
        System.loadLibrary("tactilesynth");
    }
    private native void toggleOsc(int oscId, boolean toggle);
    private native boolean isOscDown(int oscId);
    private native void setOscFrequency(int oscId, double frequency);
    private native void setOscPhase(int oscId, double offset);
    private native void setOscVoices(int oscId, int voices);
    private native void setOscVoicesVolume(int oscId, double volume);
    private native void setOscSpread(int oscId, double spread);
    private native void setReverb(double reverb);
    private native void setBitCrush(double amount);
    private native void setFilter(double amount);
    private native void setOscVolume(int oscId, double volume);
    private native void setOscAttack(int oscId, double amount);
    public native void startRecord();
    public native void stopRecord();
    public native float[] getRecordedAudioData();

    public native int getSampleRate();
    public native int getBufferSize();


    Database db;

    // class variables
    int MAX_POINTERS = 5;
    double MAX_SPREAD = .4;
    int pointers = 0;
    int voicesCount = 0;
    double spread = .07;
    double voices = 0;
    double reverb = 0;
    double filter = 0;
    double bitCrush = 0;
    double[][] pointerStates = new double[MAX_POINTERS][2];

    int xres, yres;
    int xSegments = 4;
    int ySegments = 11;
    int scaleLength = 7;



    public Synth(int xres, int yres, Database db) {
        this.xres = xres;
        this.yres = yres;
        this.db = db;
        Random phaseGen = new Random();
        for (int i = 0; i < MAX_POINTERS; i++) {
            //setOscPhase(i, phaseGen.nextDouble());
        }
    }

    public void touchEvent(MotionEvent event) {

        pointers = event.getPointerCount();

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        // set frequencies
        for (int i = 0; i < pointers; i++) {
            int localPID = event.getPointerId(i);
            int[] note = getNoteFromXY(event.getX(i), event.getY(i));
            double frequency = getNoteFrequency(note);
            setOscFrequency(localPID, frequency);
        }

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                toggleOsc(pointerId, true);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                toggleOsc(pointerId, false);
                break;
            }

            case MotionEvent.ACTION_HOVER_ENTER: {
                break;
            }

            case MotionEvent.ACTION_HOVER_MOVE: {
                break;
            }

            case MotionEvent.ACTION_HOVER_EXIT: {
                break;
            }
        }
    }

    public void rotation(float x, float y, float z) {
        //filter = ((1 - ((x % 2) + 2) % 2) + 1) / 2;
        voices = Math.abs(x);
        filter = z;
        reverb = Math.abs(min(y, 0));
        bitCrush = Math.max(y, 0);

        x = (float) Math.round(x * 100) / 100;
        y = (float) Math.round(y * 100) / 100;
        z = (float) Math.round(z * 100) / 100;



        JSONObject sensorEffects = db.getPreset().optJSONObject("sensorEffects");


        JSONObject gyroscopeEffects = sensorEffects.optJSONObject("gyroscope");

        //gets all arrays of effects
        JSONArray xEffect = gyroscopeEffects.optJSONArray("x");
        JSONArray yEffect = gyroscopeEffects.optJSONArray("y");
        JSONArray zEffect = gyroscopeEffects.optJSONArray("z");
        //for each axis we map the effects to the that axis
        for(int i = 0; i < xEffect.length(); i++){
            setEffect((String) xEffect.opt(i), x);
        }
        for(int i = 0; i < yEffect.length(); i++){
            setEffect((String) yEffect.opt(i), y);
        }
        for(int i = 0; i < zEffect.length(); i++){
            setEffect((String) zEffect.opt(i), z);
        }
    }

    private void setEffect(String effectName, float effectValue) {
        switch (effectName) {
            case "reverb": {
                setReverb(effectValue);
                break;
            }
            case "-reverb": {
                setReverb(-1 * effectValue);
                break;
            }
            case "bitcrush": {
                setBitCrush(effectValue);
            }
            case "-bitcrush": {
                setBitCrush(-1*effectValue);
            }
            case "voices": {
                for (int i = 0; i < MAX_POINTERS; i++) {
                    setOscVoicesVolume(i, effectValue);
                }
            }
            case "-voices": {
                for (int i = 0; i < MAX_POINTERS; i++) {
                    setOscVoicesVolume(i, -1*effectValue);
                }
            }
            case "filter": {
                setFilter(effectValue);
            }
            case "-filter": {
                setFilter(-1*effectValue);
            }

        }
    }

    private int[] getNoteFromXY(float x, float y) {
        int xSegmentRes = xres / xSegments;
        int xSegment = (int) Math.floor(x / xSegmentRes);

        int ySegmentRes = yres / ySegments;
        int ySegment = (int) Math.floor(y / ySegmentRes);

        int noteIndex = Math.max(0, ySegment % scaleLength);
        int octaveBoost = ySegment >= scaleLength ? 1 : 0;
        int octave = (xSegment + 1) + octaveBoost;

        return new int[]{noteIndex, octave};
    }

    private double getNoteFrequency(int[] note) {
        int noteIndex = note[0];
        int octave = note[1];

        JSONArray frequencies = db.getPreset().optJSONArray("frequencies");
        return frequencies.optDouble(noteIndex) * Math.pow(2, octave);
    }

    /* JOSH - THIS IS ONLY USED TO POPULATE THE FREQUENCY UI ELEMENTS.
     *  IT CAN BE MOVED TO THE MAIN ACTIVITY INSTEAD OF BEING A SYNTH METHOD. */
    public Double getNoteFrequency(int note) {
        int noteIndex = note;

        JSONArray frequencies = db.getPreset().optJSONArray("frequencies");
        return frequencies.optDouble(noteIndex);
    }


    public void resetEffects(){
        setFilter(0);
        setBitCrush(0);
        setReverb(0);

        for (int i = 0; i < MAX_POINTERS; i++) {
            setOscVoicesVolume(i, 0);
        }
    }


    // play() - takes in recorded audio data and plays it in a separate thread
    public void play(float[] data) {
        final int sampleRate = getSampleRate();

        final int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
        final int bufferSize = getBufferSize();
        final AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            new Thread(new Runnable() {
                public void run() {
                    audioTrack.play();
                    audioTrack.write(data, 0, data.length, AudioTrack.WRITE_BLOCKING);
                    audioTrack.stop();
                    audioTrack.release();
                }
            }).start();
        } else {
            Log.e("AudioTrack", "Failed to initialize AudioTrack");
        }
    }
}
