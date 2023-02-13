package com.dillxn.tactilesynth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.VideoView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends Activity implements SensorEventListener {


    // Used to load the 'tactilesynth' library on application startup.
    static {
        System.loadLibrary("tactilesynth");
    }
    private native void startEngine();
    private native void stopEngine();

    Synth synth;
    Database db;
    User user;

    // class variables
    private SensorManager sensorManager;
    private final float[] rotationVector = new float[3];
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    VideoView background;

    // EFFECT CHANGES STUFF
    TextView xEffects;
    TextView yEffects;
    TextView zEffects;
    boolean[] xSelectedEffects;
    boolean[] ySelectedEffects;
    boolean[] zSelectedEffects;
    ArrayList<Integer> xEffectList = new ArrayList<>();
    ArrayList<Integer> yEffectList = new ArrayList<>();
    ArrayList<Integer> zEffectList = new ArrayList<>();
    String[] effectArray = {"reverb", "voices", "filter"};
    boolean debugMenu = false;

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        user = new User(db);
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


        // JOSH
        // BEGIN SECTION FOR EFFECTS SELECTION IN DEBUG
        xEffects = findViewById(R.id.xEffects);
        yEffects = findViewById(R.id.yEffects);
        zEffects = findViewById(R.id.zEffects);

        initEffectsUI(xEffects, yEffects, zEffects);

        xSelectedEffects = new boolean[effectArray.length];
        ySelectedEffects = new boolean[effectArray.length];
        zSelectedEffects = new boolean[effectArray.length];

        xEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectSetDialog(xEffects, xSelectedEffects, xEffectList, "x");
            }
        });

        yEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectSetDialog(yEffects, ySelectedEffects, yEffectList, "y");
            }
        });

        zEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectSetDialog(zEffects, zSelectedEffects, zEffectList, "z");
            }
        });
        // END SECTION

        // start sensor listening
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // start audio engine
        startEngine();

    }

    // JOSH - POPULATES DEBUG TEXTVIEWS FOR EFFECTS ON APP STARTUP
    private void initEffectsUI(TextView xEffects, TextView yEffects, TextView zEffects) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            JSONArray tempEffectString = db.getPreset().getJSONObject("sensorEffects").getJSONObject("gyroscope").getJSONArray("x");
            for(int i = 0; i < tempEffectString.length(); i++){
                stringBuilder.append(tempEffectString.get(i).toString());

                if (i != tempEffectString.length() - 1) {
                    stringBuilder.append(", ");
                }
            }
            xEffects.setText(stringBuilder.toString());
            stringBuilder.delete(0,stringBuilder.length());

            tempEffectString = db.getPreset().getJSONObject("sensorEffects").getJSONObject("gyroscope").getJSONArray("y");
            for(int i = 0; i < tempEffectString.length(); i++){
                stringBuilder.append(tempEffectString.get(i).toString());

                if (i != tempEffectString.length() - 1) {
                    stringBuilder.append(", ");
                }
            }
            yEffects.setText(stringBuilder.toString());
            stringBuilder.delete(0,stringBuilder.length());

            tempEffectString = db.getPreset().getJSONObject("sensorEffects").getJSONObject("gyroscope").getJSONArray("z");
            for(int i = 0; i < tempEffectString.length(); i++){
                stringBuilder.append(tempEffectString.get(i).toString());

                if (i != tempEffectString.length() - 1) {
                    stringBuilder.append(", ");
                }
            }
            zEffects.setText(stringBuilder.toString());
            stringBuilder.delete(0,stringBuilder.length());


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // JOSH - USED FOR POPULATING EFFECT FOR AXIS
    private void effectSetDialog(TextView textView, boolean[] selectedEffects, ArrayList<Integer> effectList, String axis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Select Effect");

        builder.setCancelable(false);

        builder.setMultiChoiceItems(effectArray, selectedEffects, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    effectList.add(i);
                    Collections.sort(effectList);
                } else {
                    effectList.remove(Integer.valueOf(i));
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringBuilder stringBuilder = new StringBuilder();
                JSONArray effects = new JSONArray();

                JSONObject preset = db.getPreset();
                JSONObject gyroEffects = null;

                for (int j = 0; j < effectList.size(); j++){
                    stringBuilder.append(effectArray[effectList.get(j)]);
                    effects.put(effectArray[effectList.get(j)]);

                    if (j != effectList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                textView.setText(stringBuilder.toString());
                try {
                    db.getPreset().getJSONObject("sensorEffects").getJSONObject("gyroscope").put(axis, effects);
                    synth.resetEffects();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                JSONArray effects = new JSONArray();

                for (int j = 0; j < selectedEffects.length; j++){
                    selectedEffects[j] = false;
                    effectList.clear();
                    textView.setText("");
                }
                try {
                    db.getPreset().getJSONObject("sensorEffects").getJSONObject("gyroscope").put(axis, effects);
                    synth.resetEffects();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        builder.show();
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

                if(debugMenu) setRotationUI(x, y, z); // UPDATE GYRO ANGLES ONLY IF DEBUG MENU IS ACTIVE

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

        if(debugMenu){
            debugUI.setVisibility(View.INVISIBLE);
            debugMenu = !debugMenu;
        } else {
            debugUI.setVisibility(View.VISIBLE);
            debugMenu = !debugMenu;
        }
    }
}