package com.dillxn.tactilesynth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaRecorder;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.content.Context;
import android.app.Activity;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import androidx.core.app.ActivityCompat;

import java.io.PrintWriter;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

public class PlaybackHandler {
    static {
        System.loadLibrary("tactilesynth");
    }

    public native void startRecord();

    public native void stopRecord();

    public native float[] getRecordedAudioData();

    public native int getSampleRate();

    public native int getBufferSize();

    public static PlaybackHandler instance;

    public static synchronized PlaybackHandler getInstance() {
        if (instance == null) {
            throw new RuntimeException("PlaybackHandler not initialized");
        }
        return instance;
    }

    ArrayList<float[]> selectedRecordings = new ArrayList<>();
    ArrayList<float[]> recordings = new ArrayList<>();
    ArrayList<float[]> newRecordings = new ArrayList<>();
    List<AudioTrack> playingAudioTracks = new CopyOnWriteArrayList<>();
    File dirPath;
    File recordingsBase;
    File recordingsFolderFile;
    int count = 0;
    String defaultFileName = "recording";

    boolean isPlaying = false;
    boolean isRecording = false;

    public PlaybackHandler(File dirPath) {
        this.dirPath = dirPath;
        // if the dirPath exists
        if (this.dirPath.exists()) {
            recordingsBase = new File(dirPath, "recording");
            // if the dir path + "recording" exists.
            if (recordingsBase.exists()) {
                recordingsFolderFile = new File(recordingsBase, "recordings");
                if (!recordingsFolderFile.exists()) {
                    System.out.println("Somehow recordingsFolderFile does not exist");
                }
                loadAll();
                updateCount();

            } else {// what happens if recordings base does not exist
                Boolean isCreated = recordingsBase.mkdir();
                if (isCreated) {
                    System.out.println("recordingsBase created");
                    // since recordingsbase is good now create the recordings folder
                    recordingsFolderFile = new File(recordingsBase, "recordings");
                    Boolean isRecordingsCreated = recordingsFolderFile.mkdir();
                    if (isRecordingsCreated) {
                        System.out.println("recordingsFolderFile created");
                    } else {
                        System.out.println("failed to create recordingsFolderFile");
                    }
                } else {
                    System.out.println("failed to create recordingsBase");
                }
                updateCount();
            }
        }

        instance = this;
    }

    public ArrayList<float[]> getRecordings() {
        return recordings;
    }

    private void updateCount() {
        int largestCount = 0;
        for (File file : recordingsFolderFile.listFiles()) {
            String name = file.getName();
            int temp = getFileNumber(name);
            if (temp > largestCount) {
                largestCount = temp;
            }
        }
        count = largestCount + 1;
    }

    private int getFileNumber(String s) {
        return Integer.valueOf(s.substring(9, s.length() - 4));
    }

    // from the activity pass in the argument getApplicationContext().getFilesDir()
    // as the path
    public void save(float[] data) {
        File file = new File(recordingsFolderFile, (defaultFileName + count++ + ".bin"));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            for (float f : data) {
                dataOutputStream.writeFloat(f);
            }
            dataOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println("ERROR IN SAVING");
            System.out.println(e.getMessage());
        }
    }

    public void saveAll() {
        for (float[] recording : newRecordings) {
            save(recording);
        }
    }

    public float[] load(File file) {
        ArrayList<Float> fileArray = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            boolean notDone = true;
            while (notDone) {
                try {
                    fileArray.add(dataInputStream.readFloat());
                } catch (Exception e) {
                    notDone = false;
                }
            }
            dataInputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        float[] ret = new float[fileArray.size()];
        for (int i = 0; i < fileArray.size(); i++) {
            ret[i] = fileArray.get(i);
        }
        return ret;

    }

    public void loadAll() {
        File[] files = recordingsFolderFile.listFiles();
        for (File f : files) {
            recordings.add(load(f));
        }
    }

    public void addRecording() {
        stopRecord();
        float[] temp = getRecordedAudioData();
        
        // only add if not silent
        if (!isSilent(temp, 0.01f)) {
            recordings.add(temp);
            selectedRecordings.add(temp);
        }
    
        isRecording = false;
    }
    
    private boolean isSilent(float[] audioData, float silenceThreshold) {
        for (float sample : audioData) {
            if (Math.abs(sample) > silenceThreshold) {
                return false;
            }
        }
        return true;
    }

    public void startRecording() {
        startRecord();
        isRecording = true;
    }

    public void flushSelected() {
        selectedRecordings = new ArrayList<>();
    }

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

        playingAudioTracks.add(audioTrack);

        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            new Thread(new Runnable() {
                public void run() {
                    audioTrack.play();
                    audioTrack.write(data, 0, data.length, AudioTrack.WRITE_BLOCKING);
                    audioTrack.stop();
                    audioTrack.release();
                    playingAudioTracks.remove(audioTrack);
                }
            }).start();
        } else {
            Log.e("AudioTrack", "Failed to initialize AudioTrack");
        }
    }

    public void deleteRecording(float[] data) {
        // Find the index of the recording in the recordings list
        int index = recordings.indexOf(data);

        if (index != -1) {
            // Remove the recording from the recordings list
            recordings.remove(index);

            // Remove the recording from the selectedRecordings list, if present
            selectedRecordings.remove(data);

            // Find and delete the corresponding file for the recording
            File[] files = recordingsFolderFile.listFiles();
            for (File file : files) {
                float[] loadedData = load(file);
                if (arraysAreEqual(loadedData, data)) {
                    file.delete();
                    break;
                }
            }

            // Update the count
            updateCount();
        }
    }

    private boolean arraysAreEqual(float[] arr1, float[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }

        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }
    
    public void playSelected() {
        for (float[] recording : selectedRecordings) {
            play(recording);
        }
        isPlaying = true;
    }

    public void stopSelected() {
        for (AudioTrack audioTrack : playingAudioTracks) {
            if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
                audioTrack.flush();
                audioTrack.setPlaybackHeadPosition(0);
            }
        }
        playingAudioTracks.clear();
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying; // Assuming 'isPlaying' is a boolean variable in the PlaybackHandler class that is set when audio tracks are playing.
    }

    public boolean isRecording() {
        return isRecording;
    }

}
