package com.example.inventoryscannerandroid.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.inventoryscannerandroid.models.*;

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static final String DEBUG_URL_OPTIONS = "http://webservice.pc-2416.hgregoire.com/inventory/options?format=xml";
    private static final String DEBUG_URL_LOCATION = "http://webservice.pc-2416.hgregoire.com/inventory/userLocations";
    
    private static final String RELEASE_URL_OPTIONS = "http://webservice.hgregoire.com/inventory/options?format=xml";
    private static final String RELEASE_URL_LOCATION = "http://webservice.hgregoire.com/inventory/userLocations";
    
    private OkHttpClient client;
    private Gson gson;
    private boolean isDebug;
    
    public HttpClient(boolean isDebug) {
        this.isDebug = isDebug;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Feature.class, new FeatureTypeAdapter())
                .create();
    }
    
    public interface OptionsCallback {
        void onSuccess(Option option);
        void onError(String error);
    }
    
    public interface SyncCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public void getOptions(String apiKey, OptionsCallback callback) {
        String url = isDebug ? DEBUG_URL_OPTIONS : RELEASE_URL_OPTIONS;
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "APIkey " + apiKey + " InventoryScan")
                .get()
                .build();
        
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Failed to get options by Http", e);
                callback.onError("Failed to connect: " + e.getMessage());
            }
            
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String jsonResponse = response.body().string();
                        Option option = gson.fromJson(jsonResponse, Option.class);
                        callback.onSuccess(option);
                    } else {
                        callback.onError("Server error: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse options response", e);
                    callback.onError("Failed to parse response: " + e.getMessage());
                } finally {
                    response.close();
                }
            }
        });
    }
    
    public void postScanEntries(String apiKey, List<ScanEntry> scanEntries, SyncCallback callback) {
        if (scanEntries.isEmpty()) {
            callback.onError("No entries to sync");
            return;
        }
        
        String url = isDebug ? DEBUG_URL_LOCATION : RELEASE_URL_LOCATION;
        
        try {
            String json = gson.toJson(scanEntries);
            RequestBody body = RequestBody.create(json, JSON);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "APIkey " + apiKey + " InventoryScan")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Failed to post scan entries", e);
                    callback.onError("Failed to connect: " + e.getMessage());
                }
                
                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        if (response.isSuccessful()) {
                            if (responseBody.toLowerCase().contains("succeeded")) {
                                callback.onSuccess(responseBody);
                            } else {
                                callback.onError("Sync failed: " + responseBody);
                            }
                        } else {
                            callback.onError("Server error: " + response.code() + " - " + responseBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse sync response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to create request", e);
            callback.onError("Failed to create request: " + e.getMessage());
        }
    }
    
    // Copy of the FeatureTypeAdapter from Persistence class
    private static class FeatureTypeAdapter implements com.google.gson.JsonSerializer<Feature>, com.google.gson.JsonDeserializer<Feature> {
        @Override
        public com.google.gson.JsonElement serialize(Feature src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
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
        public Feature deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            com.google.gson.JsonObject jsonObject = json.getAsJsonObject();
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