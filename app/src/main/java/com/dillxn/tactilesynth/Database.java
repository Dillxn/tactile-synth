package com.dillxn.tactilesynth;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Database {
    private String fileName = "db.json";
    private File dbFile;
    private JSONObject model;
    Context context;

    public Database(Context context) {
        // load custom db from internal storage
        this.context = context;
        this.dbFile = new File(this.context.getFilesDir(), fileName);

        // todo: determine how and when the default db should
        //      overwrite the internal storage db
        // currently: it always overwrites
        create();

        load();
    }

    private void load() {
        String json = null;
        // load the database file
        try {
            FileInputStream fis = new FileInputStream(dbFile);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            json = new String(buffer, "UTF-8");
            model = new JSONObject(json);

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }


    }

    // init() - loads default db and saves it to internal storage
    private void create() {
        // read default db from assets folder
        String json = null;
        try {
            InputStream is = context.getAssets().open("db.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // save to internal storage
        saveToInternalStorage(json);
    }
    private void saveToInternalStorage(String dbJSON) {
        try {
            FileOutputStream fos = new FileOutputStream(dbFile);
            fos.write(dbJSON.getBytes());
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        String json = model.toString();
        saveToInternalStorage(json);
    }

    public JSONObject getModel() {
        return model;
    }

    public JSONObject getConfig() {
        return model.optJSONObject("config");
    }

    public JSONObject getPreset() {
        JSONObject preset = null;
        // get preset id from config
        int presetId = getConfig().optInt("preset");
        // load presets
        JSONArray presets = getModel().optJSONArray("presets");
        // find the one of correct index
        for (int i = 0; i < presets.length(); i++) {
            JSONObject presetObj = presets.optJSONObject(i);
            if (presetObj.optInt("id") == presetId) {
                preset = presetObj;
            }
        }
        // return it
        return preset;
    }
}