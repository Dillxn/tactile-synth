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

/*
1) have an AudioRecorder that is configured inside of this class. Handle the recording of audio in
    this class. Also load and store the recordered sessions inside of this class.
2)handle the playback of the recordered sessions here using an AudioTrack
3)store the byte array in internal storage as an mp3 then load it into here
    on first attempt just try to store the byte array without encoding it. If successful then try
    to encode into mp3.
 */
public class PlaybackHandler {
    Context context;
    private AudioRecord audioRecorder;
    byte[] newRecording = null;
    private ArrayList<AudioTrack> tracks = new ArrayList<>();
    private ArrayList<byte[]> byteRecordings = new ArrayList<>();
    String SAVE_DIR = "/recordings";//might need to be more specific

    private static final int SAMPLING_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int MODE = AudioTrack.MODE_STREAM;
    Database db;

    public PlaybackHandler(Context c, Activity a, Database db) {
        this.db = db;
        this.context = c;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[] {"Manifest.permission.RECORD_AUDIO"}, 0);
            /*
            possibly need to handle the permission checking in the main activity that this will occur in. UNless I restructure this
            to be it's own activity. Not sure what that entails or even means so I need to check that out next. The permission request
            will return a result to the onPermissionRequestResults with the code provided. This method needs to be overriden and handle
            what happens if accepted/rejected.
             */

        }
        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

    }
    public void startRecording(){
        audioRecorder.startRecording();
        newRecording = new byte[BUFFER_SIZE];
        audioRecorder.read(newRecording, 0, BUFFER_SIZE);
    }
    public void stopRecording(){
        audioRecorder.stop();
        audioRecorder.release();
        this.saveRecording();
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
                AudioTrack loadedTrack = new AudioTrack(STREAM_TYPE, SAMPLING_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, temp.length, MODE);
                tracks.add(loadedTrack);
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
