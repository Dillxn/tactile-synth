package com.dillxn.tactilesynth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.Settings;
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
    Button button;
    Database db;
    User user;
    TuningMenu tm;
    SettingsMenu sm;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getParentFragmentManager();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = ((MainActivity) getActivity()).getDb();
        user = ((MainActivity) getActivity()).getUser();
        tm = new TuningMenu(getContext(), view, user);
        sm = new SettingsMenu(getContext(), view);


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
        // KEEPS SETTINGS MENU INFO UP TO DATE VISUALLY
        sm.updateMenu();
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

    public void menuChange(LinearLayout [] menus, int x){
        for (int i = 0; i < 4; i++){
            if(i == x){
                menus[i].setVisibility(View.VISIBLE);
            } else {
                menus[i].setVisibility(View.GONE);
            }
        }
    }

}
