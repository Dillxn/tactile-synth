package com.dillxn.tactilesynth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class SynthFragment extends Fragment implements SensorEventListener {

    static {
        System.loadLibrary("tactilesynth");
    }

    private native void startEngine();
    private native void stopEngine();

    Synth synth;
    Database db;
    User user;

    private SensorManager sensorManager;
    private final float[] rotationVector = new float[3];
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;

    // EFFECT CHANGES STUFF
    TextView xEffects;
    TextView yEffects;
    TextView zEffects;
    TextView xRotation;
    TextView yRotation;
    TextView zRotation;
    boolean[] xSelectedEffects;
    boolean[] ySelectedEffects;
    boolean[] zSelectedEffects;
    ArrayList<Integer> xEffectList = new ArrayList<>();
    ArrayList<Integer> yEffectList = new ArrayList<>();
    ArrayList<Integer> zEffectList = new ArrayList<>();
    String[] effectArray = {"reverb", "voices", "filter"};

    boolean debugMenuActive = false;

    public SynthFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(getActivity());
        user = new User(db);

        // get display res
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int xres = displayMetrics.widthPixels;
        int yres = displayMetrics.heightPixels;

        // Init synth
        this.synth = new Synth(xres, yres, db);


        // start sensor listening
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        startEngine();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_synth, container, false);

        Touchpad touchpad = view.findViewById(R.id.touchpad);
        touchpad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                synth.touchEvent(event);
                return true;
            }
        });


        // JOSH
        // BEGIN SECTION FOR EFFECTS SELECTION IN DEBUG
        xEffects = view.findViewById(R.id.xEffects);
        yEffects = view.findViewById(R.id.yEffects);
        zEffects = view.findViewById(R.id.zEffects);
        xRotation = view.findViewById(R.id.xRotation);
        yRotation = view.findViewById(R.id.yRotation);
        zRotation = view.findViewById(R.id.zRotation);

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


        // Load frequency values to UI
        setFreqUI(view);

        return view;
    }
    // JOSH - USED TO UPDATE SYNTH WITH CHANGES MADE FROM SETTINGS
    @Override
    public void onStart() {
        super.onStart();
        updateSettings();
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

                setRotationUI(x,y,z);

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        super.onResume();

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

    @Override
    public void onDestroy() {
        stopEngine();
        super.onDestroy();
    }

    public void updateOrientationAngles() {
        // update rotation matrix
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // local coordinates
        //SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, )
        // use matrix to get orientation angles
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    private void setFreqUI(View view) {
        EditText[] freqsUI = getFreqUI(view);

        for(int i = 0; i < 7; i++){
            freqsUI[i].setText(synth.getNoteFrequency(i).toString());
        }
    }

    /* JOSH - POPULATES ROTATION VALUES FOUND IN THE DEBUG UI */
    public void setRotationUI(float x, float y, float z){
        xRotation.setText("X_ROTATION: " + String.valueOf(x));
        yRotation.setText("Y_ROTATION: " + String.valueOf(y));
        zRotation.setText("Z_ROTATION: " + String.valueOf(z));
    }

    public EditText[] getFreqUI(View view){
        return new EditText[]{
                view.findViewById(R.id.freq1),
                view.findViewById(R.id.freq2),
                view.findViewById(R.id.freq3),
                view.findViewById(R.id.freq4),
                view.findViewById(R.id.freq5),
                view.findViewById(R.id.freq6),
                view.findViewById(R.id.freq7)};
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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

    // JOSH - CALLED FROM MAIN ACTIVITY TO SET DEBUG TO TRUE OR FALSE
    public void toggleDebug(){
        if (debugMenuActive){
            debugMenuActive = !debugMenuActive;

        } else {
            debugMenuActive = !debugMenuActive;
        }
    }

    public void updateSettings(){
        if (debugMenuActive){
            getView().findViewById(R.id.debugUI).setVisibility(View.INVISIBLE);
        } else {
            getView().findViewById(R.id.debugUI).setVisibility(View.VISIBLE);
        }
    }

}