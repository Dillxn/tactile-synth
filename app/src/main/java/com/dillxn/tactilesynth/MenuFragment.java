package com.dillxn.tactilesynth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    FragmentManager fragmentManager;
    SynthFragment synthFrag;
    Button button;

    Database db;
    User user;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getParentFragmentManager();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = ((MainActivity) getActivity()).getDb();
        user = ((MainActivity) getActivity()).getUser();

        // GRABBING MENU BUTTONS AND MENU LAYOUTS
        Button[] menuButtons = {(Button) view.findViewById(R.id.settingsBtn),
                                (Button) view.findViewById(R.id.tuningBtn),
                                (Button) view.findViewById(R.id.recordingBtn),
                                (Button) view.findViewById(R.id.effectsBtn)};

        LinearLayout[] menus = {(LinearLayout) view.findViewById(R.id.settings_menu),
                                (LinearLayout) view.findViewById(R.id.tuning_menu),
                                (LinearLayout) view.findViewById(R.id.recording_menu),
                                (LinearLayout) view.findViewById(R.id.effects_menu)};

        // POPULATE AND STYLE TUNING MENU SPINNERS
        Spinner scaleSpinner = (Spinner) view.findViewById(R.id.scaleSpinner);
        scaleSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.color_spinner_layout, db.getScales()));

        Spinner keySpinner = (Spinner) view.findViewById(R.id.keySpinner);
        keySpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.color_spinner_layout, db.getKeys()));

        // SET UP LISTENERS FOR MENU BUTTONS
        setMenuListeners(menuButtons, menus);
        setSettingsListeners();
        setSpinnerListeners();
        updateMenu();
    }



    public void setMenuListeners(Button[] menuButtons, LinearLayout[] menus){
        for (int i = 0; i < 4; i++){
            int finalI = i;
            menuButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuChange(menus, finalI);
                }
            });
        }
    }

    public void setSettingsListeners(){
        button = getView().findViewById(R.id.debug);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggleDebug();
            }
        });

        button = getView().findViewById(R.id.grid);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggleGrid();
            }
        });

        for (int i = 1; i < 8; i++){
            String barID = "tuningMenu_freqBar"+i;
            String textID = "tuningMenu_freqText"+i;

            int barRes = getResources().getIdentifier(barID,"id", getActivity().getPackageName());
            int textRes = getResources().getIdentifier(textID,"id", getActivity().getPackageName());

            SeekBar seekBar = getView().findViewById(barRes);
            TextView textView = getView().findViewById(textRes);

            setFreqBarListeners(seekBar, textView, i);
            //setSpinnerListeners();
        }
    }

    public void menuChange(LinearLayout [] menus, int x){
        for (int i = 0; i < 4; i++){
            if(i == x){
                menus[i].setVisibility(View.VISIBLE);
            } else {
                menus[i].setVisibility(View.GONE);
            }
        }
    }

    public void toggleDebug(){
        db.setDebug(!db.getDebug());
        updateMenu();
    }
    public void toggleGrid(){
        db.setGrid(!db.getGrid());
        updateMenu();
    }

    public void updateMenu(){
        button = getView().findViewById(R.id.debug);
        if (db.getDebug()){
            button.setText("Debug Enabled");
        } else {
            button.setText("Debug Disabled");
        }

        button = getView().findViewById(R.id.grid);
        if (db.getGrid()){
            button.setText("Grid Enabled");
        } else {
            button.setText("Grid Disabled");
        }
        updateFreqs();
    }

    public void updateFreqs(){
        for (int i = 1; i < 8; i++) {
            String barID = "tuningMenu_freqBar" + i;
            String textID = "tuningMenu_freqText" + i;

            int barRes = getResources().getIdentifier(barID, "id", getActivity().getPackageName());
            int textRes = getResources().getIdentifier(textID, "id", getActivity().getPackageName());

            SeekBar seekBar = getView().findViewById(barRes);
            TextView textView = getView().findViewById(textRes);

            try {
                textView.setText(db.getPreset().optJSONArray("frequencies").getDouble(i - 1) + "hz");
                seekBar.setProgress((int) (db.getPreset().optJSONArray("frequencies").getDouble(i - 1) * 100));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setFreqBarListeners(SeekBar bar, TextView text, int index){
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                text.setText(new Float(bar.getProgress())/100 + "hz");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    db.getPreset().optJSONArray("frequencies").put(index-1,new Double(seekBar.getProgress())/100);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setSpinnerListeners(){
        Spinner keySpinner = getView().findViewById(R.id.keySpinner);
        Spinner scaleSpinner = getView().findViewById(R.id.scaleSpinner);

        keySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                updateFreqs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        scaleSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    user.setKey(keySpinner.getSelectedItem().toString(),scaleSpinner.getSelectedItem().toString());
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
}
