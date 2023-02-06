package com.dillxn.tactilesynth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;


public class MainActivity extends Activity implements SensorEventListener {


    // Used to load the 'tactilesynth' library on application startup.
    static {
        System.loadLibrary("tactilesynth");
    }
    private native void startEngine();
    private native void stopEngine();

    Synth synth;
    Database db;

    // class variables
    private SensorManager sensorManager;
    private final float[] rotationVector = new float[3];
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    VideoView background;

    TextView textView;
    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);
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
        this.synth = new Synth(xres, yres, db);

        // Load frequency values to UI
        setFreqUI();

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
                updateOrientationAngles();
                float x = (float) (orientationAngles[0] / Math.PI);
                float y = (float) (orientationAngles[1] * 2 / Math.PI);
                float z = (float) (orientationAngles[2] / Math.PI);
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
                if (z > maxZ) maxZ = z;
                synth.rotation(x, y, z);
                setRotationUI(x, y, z);
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

    // compute the three orientation angles based on the most recent
    // readings from the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // update rotation matrix
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // local coordinates
        //SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, )
        // use matrix to get orientation angles
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



    /*  JOSH - GRABS UI FREQUENCY ELEMENTS */
    public EditText[] getFreqUI(){
        return new EditText[]{
                findViewById(R.id.freq1),
                findViewById(R.id.freq2),
                findViewById(R.id.freq3),
                findViewById(R.id.freq4),
                findViewById(R.id.freq5),
                findViewById(R.id.freq6),
                findViewById(R.id.freq7)};
    }

    /*  JOSH - WRITES FREQUENCIES FOUND IN RUNNING MODEL TO UI */
    private void setFreqUI() {
        EditText[] freqsUI = getFreqUI();

        for(int i = 0; i < 7; i++){
            freqsUI[i].setText(synth.getNoteFrequency(i).toString());
        }
    }

    /*  JOSH - WRITES FREQUENCIES FOUND IN UI ELEMENTS INTO THE RUNNING MODEL */
    public void setFreqs(View layout) throws JSONException {
        JSONArray freqs = new JSONArray();
        EditText[] freqsUI = getFreqUI();

        for(int i = 0; i < 7; i++){
            freqs.put(String.valueOf(freqsUI[i].getText()));
        }

        db.getPreset().put("frequencies", freqs);
    }

    /* JOSH - POPULATES ROTATION VALUES FOUND IN THE DEBUG UI */
    public void setRotationUI(float x, float y, float z){
        TextView xRotation = findViewById(R.id.xRotation);
        TextView yRotation = findViewById(R.id.yRotation);
        TextView zRotation = findViewById(R.id.zRotation);

        xRotation.setText("X_ROTATION: " + String.valueOf(x));
        yRotation.setText("Y_ROTATION: " + String.valueOf(y));
        zRotation.setText("Z_ROTATION: " + String.valueOf(z));
    }

    /* JOSH - ENABLES AND DISABLES THE DEBUG UI */
    public void menuToggle(View layout){
        ConstraintLayout debugUI = findViewById(R.id.debugUI);

        if(debugUI.getVisibility() == View.VISIBLE){
            debugUI.setVisibility(View.INVISIBLE);
        } else {
            debugUI.setVisibility(View.VISIBLE);
        }
    }
}