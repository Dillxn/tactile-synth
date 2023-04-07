package com.dillxn.tactilesynth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;


public class MainActivity extends FragmentActivity {


    // Used to load the 'tactilesynth' library on application startup.
    static {
        System.loadLibrary("tactilesynth");
    }

    FragmentManager fragmentManager = getSupportFragmentManager();

    VideoView background;

    Button menuButton;
    Button armRecording;
    Button playRecordButton;
    Boolean isRecordingArmed = false;
    Boolean isRecording = false;
    public static PlaybackHandler playback;

    Database db;
    boolean menu = false;

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(this);
        // set up UI
        setContentView(R.layout.activity_main);
        // make fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // prepare background
        background = findViewById(R.id.background);
        Uri bgUri = Uri.parse("android.resource://" + getPackageName() +"/" + R.raw.background);
        background.setVideoURI(bgUri);
        background.start();
        //set recording and arm button
        armRecording = findViewById(R.id.armRecording);
        playRecordButton = findViewById(R.id.playRecording);

        background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        playback = new PlaybackHandler(getApplicationContext().getFilesDir());

        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, SynthFragment.class, null , "synthPrime").commit();
    }

    /* JOSH - ENABLES AND DISABLES THE DEBUG UI */
    public void menuToggle(View layout){
        FragmentTransaction fTransaction = fragmentManager.beginTransaction();
        Fragment synthFragment = fragmentManager.findFragmentByTag("synthPrime");
        Fragment menuFragment = (MenuFragment) fragmentManager.findFragmentByTag("optionsPrime");
        if (menuFragment != null){
            ((MenuFragment) menuFragment).updateRecordings(playback.recordings);
        }

        if(!menu) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, MenuFragment.class, null, "optionsPrime")
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
            menu = !menu;
        } else {
            if(synthFragment == null){
                fTransaction.replace(R.id.fragmentContainerView, SynthFragment.class, null , "synthPrime").commit();
                menu = !menu;
            } else {
                fTransaction.replace(R.id.fragmentContainerView, synthFragment, "synthPrime").commit();
                menu = !menu;
            }
        }
    }

@Override
    public void onResume(){
        super.onResume();
        background = findViewById(R.id.background);
        Uri bgUri = Uri.parse("android.resource://" + getPackageName() +"/" + R.raw.background);
        background.setVideoURI(bgUri);
        background.start();
}
    public void armRecording(View view){
        isRecordingArmed = !isRecordingArmed;
        if(isRecordingArmed){
            armRecording.setBackgroundColor(getColor(R.color.armed));
        }else {
            armRecording.setBackgroundColor(getColor(R.color.disarmed));
        }
        System.out.println("ARMED RECORDING");
    }
    public void startRecording(View view){
        if(isRecording){
            playRecordButton.setBackgroundColor(getColor(R.color.readyToRecord));
            playback.addRecording();
            isRecording = false;
        }else if(isRecordingArmed && !isRecording){
            playback.startRecording();
            playback.playSelected();
            playRecordButton.setBackgroundColor(getColor(R.color.recording));
            isRecording = true;
        }else{
            playRecordButton.setBackgroundColor(getColor(R.color.readyToRecord));
            playback.playSelected();

        }

        System.out.println("START/STOP/PLAY RECORDING");
    }
}