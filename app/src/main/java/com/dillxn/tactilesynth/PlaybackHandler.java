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
    ArrayList<float[]> recordings = new ArrayList<>();
    ArrayList<float[]> newRecordings = new ArrayList<>();
    File dirPath;
    File recordingsFolderFile;
    String defaultFileName = "recording";
    int totalRecordings = 0;


    public PlaybackHandler(File dirPath) {
        this.dirPath = dirPath;
        this.recordingsFolderFile = new File(dirPath, "recordings/recordings");
        if(!recordingsFolderFile.exists()){
            boolean isDirectoryCreated = recordingsFolderFile.mkdir();

            if (isDirectoryCreated) {
                System.out.println("--- Directory created successfully. ---");
            } else {
                System.out.println("--- Failed to create directory. ---");
            }
        }
        try{
            Scanner countFile = new Scanner(new File(new File(dirPath, "recordings"), "count.txt"));
            totalRecordings = countFile.nextInt();
            countFile.close();
        }catch(Exception e){
            System.out.println("ERROR - in loading count file.");
            File count = new File(recordingsFolderFile, "count.txt");
            try{
                count.createNewFile();
            }catch(Exception ex){
                System.out.println("File count.txt already exists.");
            }
        }
    }

    private void updateCount(){
        try{
            File count = new File(new File(dirPath, "recordings"), "count.txt");
            File temp = new File(new File(dirPath, "recordings"), "temp.txt");
            PrintWriter pw = new PrintWriter(temp);
            pw.write(totalRecordings);
            pw.close();

            count.delete();
            temp.renameTo(count);
        }catch(Exception e){
            System.out.println("ERROR - in updating files");
        }
    }


    //from the activity pass in the argument getApplicationContext().getFilesDir() as the path
    public void save(float[] data){
        try{
            FileOutputStream writer =  new FileOutputStream(new File(recordingsFolderFile, (defaultFileName + totalRecordings++ + ".bin")));
            DataOutputStream output = new DataOutputStream(writer);
            for(float value : data){
                output.writeFloat(value);
            }
            output.close();
            writer.close();
        } catch(Exception e){
            System.out.println("Error in saving files");
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
}
