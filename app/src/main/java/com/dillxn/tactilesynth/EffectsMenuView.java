package com.dillxn.tactilesynth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class EffectsMenuView extends BaseCustomView {

    private LinearLayout axisButtonsContainer;
    private LinearLayout effectsContainer;

    public EffectsMenuView(Context context) {
        super(context);
    }

    public EffectsMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ViewConstructor")
    public EffectsMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Your initialization code here
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.effects_menu_content;
    }

    @Override
    protected void onInit() {
        axisButtonsContainer = findViewById(R.id.axis_buttons_container);
        effectsContainer = findViewById(R.id.effects_container);

        ImageView axisXButton = findViewById(R.id.axis_x_button);
        ImageView axisYButton = findViewById(R.id.axis_y_button);
        ImageView axisZButton = findViewById(R.id.axis_z_button);

        // Set the tag for each button to identify the axis
        axisXButton.setTag("x");
        axisYButton.setTag("y");
        axisZButton.setTag("z");

        // Set the onClickListener for each button
        View.OnClickListener axisButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAxisButtonClick(v);
            }
        };

        axisXButton.setOnClickListener(axisButtonClickListener);
        axisYButton.setOnClickListener(axisButtonClickListener);
        axisZButton.setOnClickListener(axisButtonClickListener);

        // Initialize the effects for the default axis (e.g., X-axis)
        onAxisButtonClick(axisXButton);
    }

    private void onAxisButtonClick(View view) {
        String axis = (String) view.getTag();        
        // Update the UI effects for the selected axis
        updateEffectsForAxis(axis);

        // Get the axis buttons
        ImageView axisXButton = findViewById(R.id.axis_x_button);
        ImageView axisYButton = findViewById(R.id.axis_y_button);
        ImageView axisZButton = findViewById(R.id.axis_z_button);

        // Set all buttons to 50% transparent initially
        axisXButton.setAlpha(0.3f);
        axisYButton.setAlpha(0.3f);
        axisZButton.setAlpha(0.3f);

        // Set the selected button to fully opaque
        view.setAlpha(1.0f);
    }


    private void updateEffectsForAxis(String axis) {
        // Clear the current effects
        effectsContainer.removeAllViews();

        // Add the effects for the selected axis
        JSONArray effects = Database.getInstance().getModel().optJSONArray("effects");
        for (int i = 0; i < effects.length(); i++) {
            String effect = effects.optString(i);
            View effectItem = inflate(getContext(), R.layout.effect_item, null);
            CheckBox effectCheckBox = effectItem.findViewById(R.id.effect_checkbox);
            effectCheckBox.setText(effect);

            // Set the effect state and parameters based on the axis
            // For example:
            effectCheckBox.setChecked(isEffectEnabledForAxis(effect, axis));    
            
            // save effects when checkbox is changed
            effectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Get the effect name
                    String effect = buttonView.getText().toString();

                    // Get the current preset
                    JSONObject preset = Database.getInstance().getPreset();

                    // Get the sensor effects for the current preset
                    JSONObject sensorEffects = preset.optJSONObject("sensorEffects");

                    // Get the effects for the selected axis
                    JSONArray axisEffects = sensorEffects.optJSONObject("gyroscope").optJSONArray(axis);
                    
                    // Reset the effect values
                    Synth.getInstance().resetEffects();

                    // Add or remove the effect from the axis effects
                    if (isChecked) {
                        axisEffects.put(effect);
                    } else {
                        for (int i = 0; i < axisEffects.length(); i++) {
                            if (axisEffects.opt(i).equals(effect)) {
                                axisEffects.remove(i);
                                break;
                            }
                        }
                    }
                }
            });

            effectsContainer.addView(effectItem);
        }
    }
    
    private boolean isEffectEnabledForAxis(String effect, String axis) {
        // Return the effect state for the selected axis
        JSONArray axisEffects = Database.getInstance().getPreset().optJSONObject("sensorEffects").optJSONObject("gyroscope").optJSONArray(axis);
        for (int i = 0; i < axisEffects.length(); i++) {

            if (axisEffects.opt(i).equals(effect)) {
                return true;
            }
        }
        return false;
    }


// Add methods related to the effects menu functionality here
}