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

    ArrayList<float[]> selectedRecordings = new ArrayList<>();
    ArrayList<float[]> recordings = new ArrayList<>();
    ArrayList<float[]> newRecordings = new ArrayList<>();
    File dirPath;
    File recordingsBase;
    File recordingsFolderFile;
    int count = 0;
    String defaultFileName = "recording";


    public PlaybackHandler(File dirPath) {
        this.dirPath = dirPath;
        //if the dirPath exists
        if(this.dirPath.exists()){
            recordingsBase = new File(dirPath, "recording");
            //if the dir path + "recording" exists.
            if(recordingsBase.exists()){
                recordingsFolderFile = new File(recordingsBase, "recordings");
                if(!recordingsFolderFile.exists()){
                    System.out.println("Somehow recordingsFolderFile does not exist");
                }
                loadAll();
                updateCount();

            }else{//what happens if recordings base does not exist
                Boolean isCreated = recordingsBase.mkdir();
                if(isCreated){
                    System.out.println("recordingsBase created");
                    //since recordingsbase is good now create the recordings folder
                    recordingsFolderFile = new File(recordingsBase, "recordings");
                    Boolean isRecordingsCreated = recordingsFolderFile.mkdir();
                    if(isRecordingsCreated){
                        System.out.println("recordingsFolderFile created");
                    }else{
                        System.out.println("failed to create recordingsFolderFile");
                    }
                }else{
                    System.out.println("failed to create recordingsBase");
                }
                updateCount();
            }
        }
        //WILL DELETE ALL RECORDINGS IF UNCOMMENTED, TESTING PURPOSES
        /*for(File f : recordingsFolderFile.listFiles()){
            f.delete();
        }*/
        System.out.println("hello");
        //solid break point
    }
    public ArrayList<float[]> getRecordings(){
        return recordings;
    }
    private void updateCount(){
        int largestCount = 0;
        for(File file : recordingsFolderFile.listFiles()){
            String name = file.getName();
            int temp = getFileNumber(name);
            if(temp > largestCount){
                largestCount = temp;
            }
        }
        count = largestCount+1;
    }
    private int getFileNumber(String s){
        return Integer.valueOf(s.substring(9,s.length()-4));
    }

    //from the activity pass in the argument getApplicationContext().getFilesDir() as the path
    public void save(float[] data){
        File file = new File(recordingsFolderFile, (defaultFileName + count++ + ".bin"));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            for (float f : data) {
                dataOutputStream.writeFloat(f);
            }
            dataOutputStream.close();
            fileOutputStream.close();
        }catch(Exception e) {
            System.out.println("ERROR IN SAVING");
            System.out.println(e.getMessage());
        }
    }

    public void saveAll(){
        for(float[] recording : newRecordings){
            save(recording);
        }
    }
    public float[] load(File file){
        ArrayList<Float> fileArray = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            boolean notDone = true;
            while(notDone){
                try{
                    fileArray.add(dataInputStream.readFloat());
                }catch(Exception e){
                    notDone = false;
                }
            }
            dataInputStream.close();
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        float[] ret = new float[fileArray.size()];
        for(int i = 0; i < fileArray.size(); i++){
            ret[i] = fileArray.get(i);
        }
        return ret;
        
    }
    public void loadAll(){
        File[] files = recordingsFolderFile.listFiles();
        for(File f : files){
            recordings.add(load(f));
        }
    }

    public void addRecording(){
        stopRecord();
        float[] temp = getRecordedAudioData();
        recordings.add(temp);
        save(temp);
    }
    public void startRecording(){
        startRecord();
    }
    public void playSelected(){
        for(int i = 0; i < selectedRecordings.size(); i++){
            play(selectedRecordings.get(i));
        }
    }
    public void flushSelected(){
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
