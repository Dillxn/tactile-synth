package com.dillxn.tactilesynth;

import static java.lang.Math.min;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.util.Random;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

public class Synth {

    // bridge events
    static {
        System.loadLibrary("tactilesynth");
    }
    native void toggleOsc(int oscId, boolean toggle);
    native boolean isOscDown(int oscId);
    private native void setOscFrequency(int oscId, double frequency);
    private native void setOscPhase(int oscId, double offset);
    private native void setOscVoices(int oscId, int voices);
    private native void setOscVoicesVolume(int oscId, double volume);
    private native void setOscSpread(int oscId, double spread);
    native void setReverb(double reverb);
    private native void setBitCrush(double amount);
    native void setFilter(double amount);
    private native void setDelay(double amount);
    native void setTremolo(double amount);
    private native void setOscVolume(int oscId, double volume);
    private native void setOscAttack(int oscId, double amount);
    public native void startRecord();   
    public native void stopRecord();
    public native float[] getRecordedAudioData();

    public native int getSampleRate();
    public native int getBufferSize();

    Database db;
    public static Synth instance;
    public static synchronized Synth getInstance() {
        if (instance == null) {
            throw new RuntimeException("Synth not initialized");
        }
        return instance;
    }

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


    // Intializes the synth
    public Synth(int xres, int yres) {
        this.xres = xres;
        this.yres = yres;
        this.db = Database.getInstance();
        Random phaseGen = new Random();
        for (int i = 0; i < MAX_POINTERS; i++) {
            //setOscPhase(i, phaseGen.nextDouble());
        }
        
        instance = this;
    }

    // Handles touch events
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
        // check which action occurred
        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // down
                toggleOsc(pointerId, true);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                // up
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

    //used for effects that map to axis.
    public void rotation(float x, float y, float z) {
        //filter = ((1 - ((x % 2) + 2) % 2) + 1) / 2;
        //^old comment
        x = (float) Math.round(x * 100) / 100;
        y = (float) Math.round(y * 100) / 100;
        z = (float) Math.round(z * 100) / 100;
        //loads the sensorEffects from JSON and loads their effects into arrays.
        JSONObject sensorEffects = db.getPreset().optJSONObject("sensorEffects");
        JSONObject gyroscopeEffects = sensorEffects.optJSONObject("gyroscope");
        JSONArray xEffect = gyroscopeEffects.optJSONArray("x");
        JSONArray yEffect = gyroscopeEffects.optJSONArray("y");
        JSONArray zEffect = gyroscopeEffects.optJSONArray("z");
        //for each axis map the effects to the that axis
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
    //handles the map
    private void setEffect(String effectName, float effectValue) {
        switch (effectName) {
            case "reverb": {
                setReverb(effectValue);
                break;
            }
            case "voices": {
                for (int i = 0; i < MAX_POINTERS; i++) {
                    setOscVoicesVolume(i, effectValue);
                }
                break;
            }
            case "filter": {
                setFilter(effectValue);
                break;
            }
            case "delay": {
                // absolute value
                setDelay(effectValue);
                break;
            }
            case "tremolo": {
                setTremolo(effectValue);
                break;
            }
        }
    }

    int[] getNoteFromXY(float x, float y) {
        int xSegmentRes = xres / xSegments;
        int xSegment = (int) Math.floor(x / xSegmentRes);

        int ySegmentRes = yres / ySegments;
        int ySegment = (int) Math.floor(y / ySegmentRes);

        int noteIndex = Math.max(0, ySegment % scaleLength);
        int octaveBoost = ySegment >= scaleLength ? 1 : 0;
        int octave = (xSegment + 1) + octaveBoost;

        return new int[]{noteIndex, octave};
    }

    double getNoteFrequency(int[] note) {
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
        setDelay(0);
        setTremolo(0);

        for (int i = 0; i < MAX_POINTERS; i++) {
            setOscVoicesVolume(i, 0);
        }
    }
}
