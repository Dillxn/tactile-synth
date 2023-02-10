package com.dillxn.tactilesynth;

import android.os.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class User {
  Database db;
  
  public User(Database db) {
    this.db = db;
  }
    
  // setKey() - sets the user's key frequencies
  // based on the scale they have selected
  public void setKey(String key, String scale) throws JSONException {
    JSONArray keys = db.getModel().optJSONArray("keys");
    JSONArray scales = db.getModel().optJSONArray("scales");
    
    // get root frequencies from the scale all tuned starting from c0
    JSONArray rootFrequencies = new JSONArray();
    for (int i = 0; i < scales.length(); i++) {
      JSONObject scaleObj = scales.optJSONObject(i);
      if (scaleObj.optString("name").equals(scale)) {
        rootFrequencies = scaleObj.optJSONArray("frequencies");
        break;
      }
    }
    

    // get the index of the key in the keys array
    int keyIndex = 0;
    for (int i = 0; i < keys.length(); i++) {
      if (keys.optString(i).equals(key)) {
        keyIndex = i;
        break;
      }
    }
    
    // transpose the root frequencies the keyIndex # of semitones
    JSONArray keyFrequencies = new JSONArray();
    for (int i = 0; i < rootFrequencies.length(); i++) {
      Double frequency = rootFrequencies.optDouble(i) * Math.pow(2, (double)keyIndex / 12);
      keyFrequencies.put(frequency);
    }

    
    // set the keyFrequencies in the preset
    db.getPreset().put("frequencies", keyFrequencies);
  }
}
