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

    PlaybackHandler playback;
    boolean menu = false;
    boolean debugActive = false;

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        playback = new PlaybackHandler(getApplicationContext().getFilesDir());

        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, SynthFragment.class, null , "synthPrime").commit();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        playback.close();
    }


    /* JOSH - ENABLES AND DISABLES THE DEBUG UI */
    public void menuToggle(View layout){
        FragmentTransaction fTransaction = fragmentManager.beginTransaction();
        Fragment synthFragment = fragmentManager.findFragmentByTag("synthPrime");
        Fragment menuFragment = fragmentManager.findFragmentByTag("optionsPrime");

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

    // JOSH - USED TO SET SYNTH DEBUG TO TRUE OR FALSE
    public void toggleDebug(View view){
        SynthFragment fragment = (SynthFragment) fragmentManager.findFragmentByTag("synthPrime");
        Button button = view.findViewById(R.id.debug);
        if(fragment != null){
            fragment.toggleDebug();
            if (fragment.debugMenuActive) {
                button.setText("Activate Debug");
            } else {
                button.setText("Deactivate Debug");
            }
        }
    }

    public void stopRecording(View view){
        System.out.println("STOPPED RECORDING");
        playback.addRecording();
        for(float[] test : playback.newRecordings){
            playback.play(test);
        }
        for(float[] test : playback.recordings){
            playback.play(test);
        }
    }
}