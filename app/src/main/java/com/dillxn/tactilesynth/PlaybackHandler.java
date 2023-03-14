package com.dillxn.tactilesynth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.content.Context;
import android.app.Activity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import org.json.JSONArray;

public class PlaybackHandler {
    private ArrayList<float[]> floatRecordings = new ArrayList<>();
    String SAVE_DIR = "/recordings";//might need to be more specific
    public PlaybackHandler() {

    }

    public void loadRecordings(){
        //consider flushing the tracks and byte arrays and use this after each time is deleted.
        File directory = new File(SAVE_DIR);
        if(directory.exists() && directory.isDirectory()){
            File[] files = directory.listFiles();
            for(File file : files){
                byte[] temp = new byte[BUFFER_SIZE];
                try {
                    FileInputStream input = new FileInputStream(file);
                    input.read(temp);
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }

    }
    public void saveRecording(){
        if(newRecording != null){
            FileOutputStream outputStream = null;
            try{
                //write byte[] to new file at /recordings/recordingX where X is the number of recordings
                //flush newRecording
                outputStream = new FileOutputStream(SAVE_DIR + "/recording" + tracks.size());
                outputStream.write(newRecording);
                outputStream.close();
                AudioTrack loadedTrack = new AudioTrack(STREAM_TYPE, SAMPLING_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, newRecording.length, MODE);
                tracks.add(loadedTrack);
                byteRecordings.add(newRecording);
                newRecording = null;
            }catch(IOException e){
                System.out.println(e);
            }
        }
        return;
    }




}
