package com.dillxn.tactilesynth;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class TuningMenu {
    private ArrayList<SeekBar> seekBars = new ArrayList<SeekBar>();
    private ArrayList<TextView> textViews = new ArrayList<TextView>();
    private Context context;
    private Database db;
    private User user;
    private View view;
    private SharedPreferences prefs;


    public TuningMenu (Context context, View view, User user){
        this.context = context;
        this.view = view;
        this.db = Database.getInstance();
        this.user = user;
        this.prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String selectedScale = prefs.getString("selectedScale", "");
        String selectedKey = prefs.getString("selectedKey", "");
        try {
            user.setKey(selectedKey,selectedScale);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for (int i = 1; i < 8; i++) {
            String barID = "tuningMenu_freqBar" + i;
            String textID = "tuningMenu_freqText" + i;

            int barRes = context.getResources().getIdentifier(barID, "id", context.getPackageName());
            int textRes = context.getResources().getIdentifier(textID, "id", context.getPackageName());

            seekBars.add(view.findViewById(barRes));
            textViews.add(view.findViewById(textRes));
        }
        setupSeekBars();
        setupSpinners();
    }

    private void setupSpinners() {
        Spinner scaleSpinner = (Spinner) view.findViewById(R.id.scaleSpinner);
        scaleSpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.color_spinner_layout, db.getScales()));
        // Set the default selected item for the spinner
        String selectedScale = prefs.getString("selectedScale","");
        if (!selectedScale.isEmpty()) {
            int spinnerPosition = ((ArrayAdapter<String>) scaleSpinner.getAdapter()).getPosition(selectedScale);
            scaleSpinner.setSelection(spinnerPosition);
        }

        Spinner keySpinner = (Spinner) view.findViewById(R.id.keySpinner);
        keySpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.color_spinner_layout, db.getKeys()));
        // Set the default selected item for the spinner
        String selectedKey = prefs.getString("selectedKey","");
        if (!selectedKey.isEmpty()) {
            int spinnerPosition = ((ArrayAdapter<String>) keySpinner.getAdapter()).getPosition(selectedKey);
            keySpinner.setSelection(spinnerPosition);
        }

        scaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
                    // Save the selected item to SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("selectedScale", scaleSpinner.getSelectedItem().toString()).apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                updateFreqs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        keySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
                    // Save the selected item to SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("selectedKey", keySpinner.getSelectedItem().toString()).apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                updateFreqs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    /*
    private void setupSpinners() {
        Spinner scaleSpinner = (Spinner) view.findViewById(R.id.scaleSpinner);
        scaleSpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.color_spinner_layout, db.getScales()));

        Spinner keySpinner = (Spinner) view.findViewById(R.id.keySpinner);
        keySpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.color_spinner_layout, db.getKeys()));

        scaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
                    sharedPreferences.edit().putInt("scaleSpinnerSelected", i).apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                updateFreqs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        keySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
                    sharedPreferences.edit().putInt("keySpinnerSelected", i).apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                updateFreqs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

     */

    private void setupSeekBars() {
        for (SeekBar seekBar : seekBars) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    TextView textView = textViews.get(seekBars.indexOf(seekBar));
                    textView.setText(String.format("%.2f",new Float(progress)/100) + "hz");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        db.getPreset().optJSONArray("frequencies").put(seekBars.indexOf(seekBar),new Double(seekBar.getProgress())/100);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private void updateFreqs(){
        try {
            for (SeekBar seekBar : seekBars) {
                seekBar.setProgress((int) (db.getPreset().optJSONArray("frequencies").getDouble(seekBars.indexOf(seekBar)) * 100));
                TextView textView = textViews.get(seekBars.indexOf(seekBar));
                textView.setText(String.format("%.2f", db.getPreset().optJSONArray("frequencies").getDouble(textViews.indexOf(textView))) + "hz");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
