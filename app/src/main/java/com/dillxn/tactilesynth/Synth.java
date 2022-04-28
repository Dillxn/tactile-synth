package com.dillxn.tactilesynth;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

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

    // class variables
    int MAX_POINTERS = 10;
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

    double[] scale = {
            38.89,
            43.65,
            49.00,
            51.91,
            58.27,
            65.41,
            73.42
    };

    public Synth(int xres, int yres) {
        this.xres = xres;
        this.yres = yres;
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
        reverb = Math.abs(Math.min(y, 0));
        bitCrush = Math.max(y, 0);

        setReverb(reverb);
        setBitCrush(bitCrush);
        setFilter(filter);

        for (int i = 0; i < MAX_POINTERS; i++) {
            setOscVoicesVolume(i, voices);
        }

    }

    private int[] getNoteFromXY(float x, float y) {
        int xSegmentRes = xres / xSegments;
        int xSegment = (int) Math.floor(x / xSegmentRes);

        int ySegmentRes = yres / ySegments;
        int ySegment = (int) Math.floor(y / ySegmentRes);

        int noteIndex = Math.max(0, ySegment % scale.length);
        int octaveBoost = ySegment >= scale.length ? 1 : 0;
        int octave = (xSegment + 1) + octaveBoost;

        return new int[]{noteIndex, octave};
    }

    private double getNoteFrequency(int[] note) {
        int noteIndex = note[0];
        int octave = note[1];
        return scale[noteIndex] * Math.pow(2, octave - 1);
    }

    private int[] getRelativeNote(int[] note, int shift) {
        int noteIndex = note[0];
        int octave = note[1];

        int suppliedNoteIndex = noteIndex + shift;
        double suppliedNoteIndexRatio = suppliedNoteIndex / scale.length;
        int octaveShift = suppliedNoteIndexRatio >= 0 ? (int) Math.floor(suppliedNoteIndexRatio) : (int) Math.ceil(suppliedNoteIndexRatio);

        int relativeNoteIndex = ((suppliedNoteIndex % scale.length) + scale.length) % scale.length;
        int relativeOctave = octave + octaveShift;

        return new int[]{relativeNoteIndex, relativeOctave};
    }

}
