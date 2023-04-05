package com.dillxn.tactilesynth;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class SettingsMenu {
    private Database db;
    private Context context;
    private View view;
    private Button gridBtn;
    private Button debugBtn;

    public SettingsMenu(Context context, View view){
        this.db = Database.getInstance();
        this.context = context;
        this.view = view;
        this.debugBtn = (Button) this.view.findViewById(R.id.debug);
        this.gridBtn = (Button) this.view.findViewById(R.id.grid);

        setButtonListeners();

    }

    private void setButtonListeners() {
        debugBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggleDebug();
            }
        });

        gridBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggleGrid();
            }
        });
    }

    private void toggleDebug(){
        db.setDebug(!db.getDebug());
        updateMenu();
    }
    private void toggleGrid(){
        db.setGrid(!db.getGrid());
        updateMenu();
    }

    public void updateMenu(){

        if (db.getDebug()){
            debugBtn.setText("Debug Enabled");
        } else {
            debugBtn.setText("Debug Disabled");
        }

        if (db.getGrid()){
            gridBtn.setText("Grid Enabled");
        } else {
            gridBtn.setText("Grid Disabled");
        }
    }

}
