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
