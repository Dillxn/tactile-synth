package com.dillxn.tactilesynth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.VideoView;


public class MainActivity extends Activity implements SensorEventListener {

    // Used to load the 'tactilesynth' library on application startup.
    static {
        System.loadLibrary("tactilesynth");
    }
    private native void startEngine();
    private native void stopEngine();

    Synth synth;

    // class variables
    private SensorManager sensorManager;
    private final float[] rotationVector = new float[3];
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    VideoView background;

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
        Uri bgUri = Uri.parse("android.resource://"+getPackageName()
                +"/" + R.raw.background);
        background.setVideoURI(bgUri);
        background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        // get display res
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int xres = displayMetrics.widthPixels;
        int yres = displayMetrics.heightPixels;

        // Init synth
        this.synth = new Synth(xres, yres);

        // start sensor listening
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        // start audio engine
        startEngine();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // forward touch events to synth
        synth.touchEvent(event);
        return true;
    }

    protected void onResume() {
        super.onResume();

        // start background
        background.start();

        // register sensor listeners
        Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticFieldSensor != null) {
            sensorManager.registerListener(this, magneticFieldSensor,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        switch (sensor.getType()) {
            case Sensor.TYPE_GAME_ROTATION_VECTOR: {
                synth.rotation(event.values[0], event.values[1], event.values[2]);

                break;
            }
            case Sensor.TYPE_ACCELEROMETER: {
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
                break;
            }
        }
    }

    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    @Override
    public void onDestroy() {
        stopEngine();
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}