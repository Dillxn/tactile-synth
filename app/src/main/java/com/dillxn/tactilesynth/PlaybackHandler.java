package com.dillxn.tactilesynth;

import android.media.MediaRecorder;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
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
    private MediaRecorder mediaRecorder;
    //to handle multiple recordings at once we will need to have an array of AudioTracks.
    private ArrayList<AudioTrack> tracks = new ArrayList<>();

    private static final int SAMPLING_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int MODE = AudioTrack.MODE_STREAM;

    Database db;
    JSONArray recordings;

    byte[] newRecording;


    public PlaybackHandler(){
        mediaRecorder = new MediaRecorder();
        //get the json object where we will store file names, then if that is not empty. Build an audioTrack.

    }
    //create a method to record and get the byte[] and store it in newRecording. Then figure out how to save that.




}
