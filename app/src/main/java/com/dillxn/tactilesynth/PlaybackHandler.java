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
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;

//TODO: figure out which xml is displayed when playing
//TODO: connect the red button to stop recording and get the data.
//TODO: add the data to the playback handler
//TODO: use the playbackhandler to show the recordings and do other stuff with.

public class PlaybackHandler {
    static {
        System.loadLibrary("tactilesynth");
    }
    public native void startRecord();
    public native void stopRecord();
    public native float[] getRecordedAudioData();
    public native int getSampleRate();
    public native int getBufferSize();

    ArrayList<float[]> recordings = new ArrayList<>();
    ArrayList<float[]> newRecordings = new ArrayList<>();
    File dirPath;
    File recordingsBase;
    File recordingsFolderFile;
    String defaultFileName = "recording";
    int totalRecordings = 0;


    public PlaybackHandler(File dirPath) {
        this.dirPath = dirPath;
        //if the dirPath exists
        if(this.dirPath.exists()){
            recordingsBase = new File(dirPath, "recording");
            //if the dir path + "recording" exists.
            if(recordingsBase.exists()){
                createCount();
                recordingsFolderFile = new File(recordingsBase, "recordings");
                if(!recordingsFolderFile.exists()){
                    System.out.println("Somehow recordingsFolderFile does not exist");
                }

            }else{//what happens if recordings base does not exist
                Boolean isCreated = recordingsBase.mkdir();
                if(isCreated){
                    System.out.println("recordingsBase created");
                    createCount();
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
            }
        }

    }
    private void createCount(){
        File countFile = new File(recordingsBase, "count.txt");
        //look for count.txt and if it exists get it's value
        if(countFile.exists()){
            try{
                Scanner sc = new Scanner(countFile);
                totalRecordings = sc.nextInt();
                sc.close();
            }catch(Exception e){
                System.out.println("Error creating scanner on countFile");
                System.out.println(e.getMessage());
            }
        }else{
            try{
                countFile.createNewFile();
                PrintWriter pr = new PrintWriter(countFile);
                pr.println("0");
                pr.close();
            }catch(Exception ex){
                System.out.println("Error - creating count.txt");
                System.out.println(ex.getMessage());
            }
        }
    }

    private void updateCount(){
        try{
            File count = new File(recordingsBase, "count.txt");
            File temp = new File(recordingsBase, "temp.txt");
            PrintWriter pw = new PrintWriter(temp);
            pw.write(totalRecordings);
            pw.close();

            count.delete();
            temp.renameTo(count);
        }catch(Exception e){
            System.out.println("ERROR - in updating files");
            System.out.println(e.getMessage());
        }
    }


    //from the activity pass in the argument getApplicationContext().getFilesDir() as the path
    public void save(float[] data){
        File file = new File(recordingsFolderFile, (defaultFileName + totalRecordings++ + ".bin"));
        try{
            file.createNewFile();
            FileOutputStream writer =  new FileOutputStream(file);
            DataOutputStream output = new DataOutputStream(writer);
            for(float value : data){
                output.writeFloat(value);
            }
            output.close();
            writer.close();
            System.out.println("Saved recording: " + totalRecordings);
        } catch(Exception e){
            System.out.println("Error in saving files");
            System.out.println("--------");
            System.out.println(e.getMessage());
            System.out.println("--------");

        }
    }

    public void saveAll(){
        for(float[] recording : newRecordings){
            save(recording);
            updateCount();
        }
    }
    public float[] load(File file){
        try{
            FileInputStream inputFile = new FileInputStream(file);
            DataInputStream input = new DataInputStream(inputFile);
            int numFloats = (int)(input.available()/4);
            float[] data = new float[numFloats];

            for(int i = 0; i <numFloats; i++){
                data[i] = input.readFloat();
            }
            inputFile.close();
            input.close();
            return data;

        }catch(Exception e){
            System.out.println("Error in loading file");
        }
        return null;
    }
    public void loadAll(){
        File[] files = recordingsFolderFile.listFiles();
        for(File f : files){
            newRecordings.add(load(f));
        }
    }


    public void close(){
        updateCount();
    }

    public void addRecording(){
        stopRecord();
        float[] temp = getRecordedAudioData();
        save(temp);
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
