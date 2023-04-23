package com.dillxn.tactilesynth;

import static android.app.PendingIntent.getActivity;

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
import android.util.DisplayMetrics;
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
    
    private native void startEngine();
    
    private native void stopEngine();
    
    
    FragmentManager fragmentManager = getSupportFragmentManager();

    VideoView background;

    Button menuButton;

    public static PlaybackHandler playback;

    Database db;
    Synth synth;
    boolean menu = false;

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(this);


        // get display res
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int xres = displayMetrics.widthPixels;
        int yres = displayMetrics.heightPixels;

        // Init synth
        synth = new Synth(xres, yres);

        startEngine();


        playback = new PlaybackHandler(getApplicationContext().getFilesDir());
        
        
        // make fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // prepare background
        background = findViewById(R.id.background);
        Uri bgUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background);
        background.setVideoURI(bgUri);
        background.start();

        background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });



        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, SynthFragment.class, null, "synthPrime")
                .commit();
    }

    /* JOSH - ENABLES AND DISABLES THE DEBUG UI */
    public void menuToggle(View layout) {
        FragmentTransaction fTransaction = fragmentManager.beginTransaction();
        Fragment synthFragment = fragmentManager.findFragmentByTag("synthPrime");
        Fragment menuFragment = (MenuFragment) fragmentManager.findFragmentByTag("optionsPrime");
        if (menuFragment != null) {
            ((MenuFragment) menuFragment).updateRecordings(playback.recordings);
        }

        if (!menu) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, MenuFragment.class, null, "optionsPrime")
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
            menu = !menu;
        } else {
            if (synthFragment == null) {
                fTransaction.replace(R.id.fragmentContainerView, SynthFragment.class, null, "synthPrime").commit();
                menu = !menu;
            } else {
                fTransaction.replace(R.id.fragmentContainerView, synthFragment, "synthPrime").commit();
                menu = !menu;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        background = findViewById(R.id.background);
        Uri bgUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background);
        background.setVideoURI(bgUri);
        background.start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopEngine();
    }
}