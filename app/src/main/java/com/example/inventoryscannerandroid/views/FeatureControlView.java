package com.example.inventoryscannerandroid.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.inventoryscannerandroid.models.*;

public class FeatureControlView {
    private Context context;
    private LinearLayout containerLayout;
    private Map<String, CheckBox> checkBoxes;
    private Map<String, EditText> editTexts;
    private ScanEntry currentEntry;

    public FeatureControlView(Context context, LinearLayout containerLayout) {
        this.context = context;
        this.containerLayout = containerLayout;
        this.checkBoxes = new HashMap<>();
        this.editTexts = new HashMap<>();
    }

    public void updateFeatureControls(List<Feature> features, ScanEntry entry) {
        // Save current entry features before clearing
        if (currentEntry != null) {
            setEntryFeatures(currentEntry);
        }

        // Clear existing controls
        containerLayout.removeAllViews();
        checkBoxes.clear();
        editTexts.clear();
        currentEntry = entry;

        // Create controls for each feature
        for (Feature feature : features) {
            createFeatureControl(feature, entry);
        }
    }

    private void createFeatureControl(Feature feature, ScanEntry entry) {
        String featureType = feature.getType();
        
        switch (featureType) {
            case "FeatureBool":
                createCheckBoxControl(feature.name, entry);
                break;
            case "FeatureString":
            case "FeatureInt":
            case "FeatureFloat":
                createTextControl(feature.name, featureType, entry);
                break;
        }
    }

    private void createCheckBoxControl(String featureName, ScanEntry entry) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(featureName);
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 4, 8, 4);
        checkBox.setLayoutParams(params);

        // Set initial value from entry
        setControlFromEntry(checkBox, entry, featureName);

        checkBoxes.put(featureName, checkBox);
        containerLayout.addView(checkBox);
    }

    private void createTextControl(String featureName, String featureType, ScanEntry entry) {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(8, 4, 8, 4);
        rowLayout.setLayoutParams(rowParams);

        // Create EditText
        EditText editText = new EditText(context);
        editText.setHint(featureName);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        editText.setLayoutParams(editParams);

        // Set input type based on feature type
        switch (featureType) {
            case "FeatureInt":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                break;
            case "FeatureFloat":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            default:
                editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }

        // Create Label
        TextView label = new TextView(context);
        label.setText(featureName);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(8, 0, 0, 0);
        label.setLayoutParams(labelParams);

        // Set initial value from entry
        setControlFromEntry(editText, entry, featureName);

        editTexts.put(featureName, editText);
        
        rowLayout.addView(editText);
        rowLayout.addView(label);
        containerLayout.addView(rowLayout);
    }

    private void setControlFromEntry(Object control, ScanEntry entry, String featureName) {
        if (entry == null || entry.features == null) return;

        for (Feature feature : entry.features) {
            if (feature.name.equals(featureName)) {
                try {
                    switch (feature.getClass().getSimpleName()) {
                        case "FeatureBool":
                            if (control instanceof CheckBox) {
                                ((CheckBox) control).setChecked(((FeatureBool) feature).value);
                            }
                            break;
                        case "FeatureString":
                            if (control instanceof EditText) {
                                ((EditText) control).setText(((FeatureString) feature).value);
                            }
                            break;
                        case "FeatureInt":
                            if (control instanceof EditText) {
                                ((EditText) control).setText(String.valueOf(((FeatureInt) feature).value));
                            }
                            break;
                        case "FeatureFloat":
                            if (control instanceof EditText) {
                                ((EditText) control).setText(((FeatureFloat) feature).value.toString());
                            }
                            break;
                    }
                } catch (Exception e) {
                    // Ignore casting errors
                }
                break;
            }
        }
    }

    public void setEntryFeatures(ScanEntry entry) {
        if (entry == null) return;

        entry.features = new ArrayList<>();

        // Collect values from CheckBoxes
        for (Map.Entry<String, CheckBox> checkBoxEntry : checkBoxes.entrySet()) {
            FeatureBool feature = new FeatureBool();
            feature.name = checkBoxEntry.getKey();
            feature.value = checkBoxEntry.getValue().isChecked();
            entry.features.add(feature);
        }

        // Collect values from EditTexts
        for (Map.Entry<String, EditText> editTextEntry : editTexts.entrySet()) {
            String name = editTextEntry.getKey();
            String value = editTextEntry.getValue().getText().toString().trim();
            
            if (!value.isEmpty()) {
                // Try to determine the correct feature type based on value
                try {
                    // Try integer first
                    int intValue = Integer.parseInt(value);
                    FeatureInt feature = new FeatureInt();
                    feature.name = name;
                    feature.value = intValue;
                    entry.features.add(feature);
                } catch (NumberFormatException e1) {
                    try {
                        // Try decimal
                        BigDecimal decimalValue = new BigDecimal(value);
                        FeatureFloat feature = new FeatureFloat();
                        feature.name = name;
                        feature.value = decimalValue;
                        entry.features.add(feature);
                    } catch (NumberFormatException e2) {
                        // Default to string
                        FeatureString feature = new FeatureString();
                        feature.name = name;
                        feature.value = value;
                        entry.features.add(feature);
                    }
                }
            } else {
                // Empty value, add as empty string
                FeatureString feature = new FeatureString();
                feature.name = name;
                feature.value = value;
                entry.features.add(feature);
            }
        }
    }
} 