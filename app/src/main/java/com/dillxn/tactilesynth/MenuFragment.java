package com.dillxn.tactilesynth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;


public class MenuFragment extends Fragment {
    FragmentManager fragmentManager;
    SynthFragment synthFrag;
    View view;
    Button button;

    Database db;

    ArrayList<float[]> recordings = null;
    static RecordingsAdapter adapter = null;

    public MenuFragment() {
        // Required empty public constructor
    }
    public void updateRecordings(ArrayList<float []> recordings) {
        this.recordings = recordings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getParentFragmentManager();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        synthFrag = (SynthFragment) fragmentManager.findFragmentByTag("synthPrime");
        //get the recordings from the playback handler


        // Inflate the layout for this fragment
        return view ;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = ((MainActivity) getActivity()).getDb();


        LinearLayout settingsMenu = (LinearLayout) view.findViewById(R.id.settings_menu);
        LinearLayout tuningMenu = (LinearLayout) view.findViewById(R.id.tuning_menu);
        LinearLayout effectsMenu = (LinearLayout) view.findViewById(R.id.effects_menu);
        LinearLayout recordingMenu = (LinearLayout) view.findViewById(R.id.recording_menu);
        ListView recordingList = (ListView) view.findViewById(R.id.recording_list);
        //somehow figure out how to update the listview with the recordings. or figure out another system to make it interactable
        ArrayList<float[]> recordings = ((MainActivity) getActivity()).playback.getRecordings();
        adapter = new RecordingsAdapter(getActivity(), recordings);
        recordingList.setAdapter(adapter);
        //temperary fix, need to figure out how to maintain persistence of checked settings.
        ((MainActivity) getActivity()).playback.flushSelected();

        // GRABBING MENU BUTTONS AND MENU LAYOUTS
        Button[] menuButtons = {(Button) view.findViewById(R.id.settingsBtn),
                                (Button) view.findViewById(R.id.tuningBtn),
                                (Button) view.findViewById(R.id.recordingBtn),
                                (Button) view.findViewById(R.id.effectsBtn)};

        LinearLayout[] menus = {(LinearLayout) view.findViewById(R.id.settings_menu),
                                (LinearLayout) view.findViewById(R.id.tuning_menu),
                                (LinearLayout) view.findViewById(R.id.recording_menu),
                                (LinearLayout) view.findViewById(R.id.effects_menu)};


        // SET UP LISTENERS FOR MENU BUTTONS
        setMenuListeners(menuButtons, menus);
        setSettingsListeners();
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
    }
}
