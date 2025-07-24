package com.example.inventoryscannerandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.example.inventoryscannerandroid.models.*;

public class Persistence {
    private static final String PREFS_NAME = "InventoryScan";
    private SharedPreferences prefs;
    private Gson gson;

    public Persistence(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Create Gson with custom serializer/deserializer for Feature hierarchy
        gson = new GsonBuilder()
                .registerTypeAdapter(Feature.class, new FeatureTypeAdapter())
                .create();
    }

    public void saveScanEntries(List<ScanEntry> scanEntries) {
        try {
            String json = gson.toJson(scanEntries);
            prefs.edit().putString("scan_entries", json).apply();
        } catch (Exception e) {
            Log.e("Persistence", "Error saving scan entries", e);
        }
    }

    public List<ScanEntry> loadScanEntries() {
        try {
            String json = prefs.getString("scan_entries", "[]");
            ScanEntry[] array = gson.fromJson(json, ScanEntry[].class);
            List<ScanEntry> list = new ArrayList<>();
            if (array != null) {
                for (ScanEntry entry : array) {
                    list.add(entry);
                }
            }
            return list;
        } catch (Exception e) {
            Log.e("Persistence", "Error loading scan entries", e);
            return new ArrayList<>();
        }
    }

    public void saveOption(Option option) {
        try {
            String json = gson.toJson(option);
            prefs.edit().putString("option", json).apply();
        } catch (Exception e) {
            Log.e("Persistence", "Error saving option", e);
        }
    }

    public Option loadOption() {
        try {
            String json = prefs.getString("option", "{}");
            Option option = gson.fromJson(json, Option.class);
            return option != null ? option : new Option();
        } catch (Exception e) {
            Log.e("Persistence", "Error loading option", e);
            return new Option();
        }
    }

    public void saveSetting(Setting setting) {
        try {
            String json = gson.toJson(setting);
            prefs.edit().putString("setting", json).apply();
        } catch (Exception e) {
            Log.e("Persistence", "Error saving setting", e);
        }
    }

    public Setting loadSetting() {
        try {
            String json = prefs.getString("setting", "{}");
            Setting setting = gson.fromJson(json, Setting.class);
            return setting != null ? setting : new Setting();
        } catch (Exception e) {
            Log.e("Persistence", "Error loading setting", e);
            return new Setting();
        }
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // Custom type adapter for Feature polymorphism
    private static class FeatureTypeAdapter implements JsonSerializer<Feature>, JsonDeserializer<Feature> {
        @Override
        public JsonElement serialize(Feature src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", src.getClass().getSimpleName());
            jsonObject.addProperty("name", src.name);
            
            if (src instanceof FeatureBool) {
                jsonObject.addProperty("value", ((FeatureBool) src).value);
            } else if (src instanceof FeatureString) {
                jsonObject.addProperty("value", ((FeatureString) src).value);
            } else if (src instanceof FeatureInt) {
                jsonObject.addProperty("value", ((FeatureInt) src).value);
            } else if (src instanceof FeatureFloat) {
                jsonObject.addProperty("value", ((FeatureFloat) src).value);
            }
            
            return jsonObject;
        }

        @Override
        public Feature deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            String name = jsonObject.get("name").getAsString();
            
            switch (type) {
                case "FeatureBool":
                    boolean boolValue = jsonObject.get("value").getAsBoolean();
                    return new FeatureBool(name, boolValue);
                case "FeatureString":
                    String stringValue = jsonObject.get("value").getAsString();
                    return new FeatureString(name, stringValue);
                case "FeatureInt":
                    int intValue = jsonObject.get("value").getAsInt();
                    return new FeatureInt(name, intValue);
                case "FeatureFloat":
                    double doubleValue = jsonObject.get("value").getAsDouble();
                    return new FeatureFloat(name, java.math.BigDecimal.valueOf(doubleValue));
                default:
                    return new Feature(name);
            }
        }
    }
} 