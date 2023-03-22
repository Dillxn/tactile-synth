package com.dillxn.tactilesynth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {
    FragmentManager fragmentManager;
    SynthFragment synthFrag;

    View view;

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
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        synthFrag = (SynthFragment) fragmentManager.findFragmentByTag("synthPrime");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // GRABBING MENU BUTTONS AND MENU LAYOUTS
        Button settingsButton = (Button) view.findViewById(R.id.settingsBtn);
        Button tuningButton = (Button) view.findViewById(R.id.tuningBtn);
        Button recordingButton = (Button) view.findViewById(R.id.recordingBtn);
        Button effectsButton = (Button) view.findViewById(R.id.effectsBtn);

        LinearLayout settingsMenu = (LinearLayout) view.findViewById(R.id.settings_menu);
        LinearLayout tuningMenu = (LinearLayout) view.findViewById(R.id.tuning_menu);
        LinearLayout effectsMenu = (LinearLayout) view.findViewById(R.id.effects_menu);
        LinearLayout recordingMenu = (LinearLayout) view.findViewById(R.id.recording_menu);

        // SET UP LISTENERS FOR MENU BUTTONS
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsMenu.setVisibility(View.VISIBLE);
                tuningMenu.setVisibility(View.GONE);
                effectsMenu.setVisibility(View.GONE);
                recordingMenu.setVisibility(View.GONE);
            }
        });

        tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsMenu.setVisibility(View.GONE);
                tuningMenu.setVisibility(View.VISIBLE);
                effectsMenu.setVisibility(View.GONE);
                recordingMenu.setVisibility(View.GONE);
            }
        });

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsMenu.setVisibility(View.GONE);
                tuningMenu.setVisibility(View.GONE);
                effectsMenu.setVisibility(View.GONE);
                recordingMenu.setVisibility(View.VISIBLE);
            }
        });

        effectsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsMenu.setVisibility(View.GONE);
                tuningMenu.setVisibility(View.GONE);
                effectsMenu.setVisibility(View.VISIBLE);
                recordingMenu.setVisibility(View.GONE);
            }
        });
    }
}
