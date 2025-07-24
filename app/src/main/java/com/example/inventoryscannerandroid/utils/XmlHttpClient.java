package com.example.inventoryscannerandroid.utils;

import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.inventoryscannerandroid.models.*;

public class XmlHttpClient {
    private static final String TAG = "XmlHttpClient";
    private static final MediaType XML = MediaType.get("text/xml; charset=utf-8");
    
    // Exact same URLs as original Windows CE version
    private static final String DEBUG_URL_OPTIONS = "http://webservice.pc-2416.hgregoire.com/inventory/options?format=xml";
    private static final String DEBUG_URL_LOCATION = "http://webservice.pc-2416.hgregoire.com/inventory/userLocations";
    
    private static final String RELEASE_URL_OPTIONS = "https://webservice.hgregoire.com/inventory/options?format=xml";
    private static final String RELEASE_URL_LOCATION = "https://webservice.hgregoire.com/inventory/userLocations";
    
    private OkHttpClient client;
    private Serializer serializer;
    private boolean isDebug;
    
    public XmlHttpClient(boolean isDebug) {
        this.isDebug = isDebug;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Setup XML serializer with transformations for BigDecimal and Feature polymorphism
        RegistryMatcher matcher = new RegistryMatcher();
        matcher.bind(BigDecimal.class, new BigDecimalTransform());
        
        // Create strategy that supports custom converters
        AnnotationStrategy strategy = new AnnotationStrategy();
        
        this.serializer = new Persister(strategy, matcher);
    }

    public interface OptionsCallback {
        void onSuccess(Option option);
        void onError(String error);
    }
    
    public interface SyncCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    /**
     * Gets options from web service exactly like the original Windows CE version
     */
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
                Log.e(TAG, "Failed to get options by Xml", e);
                callback.onError("Failed to connect: " + e.getMessage());
            }
            
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String xmlResponse = response.body().string();
                        Log.d(TAG, "Received XML: " + xmlResponse);
                        
                        // Parse XML exactly like the original C# XmlSerializer
                        java.io.StringReader stringReader = new java.io.StringReader(xmlResponse);
                        Option option = serializer.read(Option.class, stringReader);
                        
                        Log.d(TAG, "Parsed " + option.zones.size() + " zones and " + option.features.size() + " features");
                        
                        // Debug log each feature type
                        for (Feature feature : option.features) {
                            Log.d(TAG, "Feature: " + feature.getClass().getSimpleName() + " - " + feature.name);
                        }
                        
                        callback.onSuccess(option);
                    } else {
                        callback.onError("Server error: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse XML response", e);
                    callback.onError("Failed to parse response: " + e.getMessage());
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * Posts scan entries as XML to the web service
     */
    public void postScanEntries(String apiKey, List<ScanEntry> scanEntries, SyncCallback callback) {
        if (scanEntries.isEmpty()) {
            callback.onError("No entries to sync");
            return;
        }
        
        String url = isDebug ? DEBUG_URL_LOCATION : RELEASE_URL_LOCATION;
        
        try {
            // Create XML wrapper for scan entries (like CodeList in original)
            ScanEntriesWrapper wrapper = new ScanEntriesWrapper();
            wrapper.scanEntries = scanEntries;
            
            // Serialize to XML
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            serializer.write(wrapper, stringWriter);
            String xml = stringWriter.toString();
            Log.d(TAG, "Sending XML: " + xml);
            
            RequestBody body = RequestBody.create(xml, XML);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "APIkey " + apiKey + " InventoryScan")
                    .addHeader("Content-Type", "text/xml")
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
            Log.e(TAG, "Failed to create XML request", e);
            callback.onError("Failed to create request: " + e.getMessage());
        }
    }
    
    // Custom transform for BigDecimal in XML
    private static class BigDecimalTransform implements org.simpleframework.xml.transform.Transform<BigDecimal> {
        @Override
        public BigDecimal read(String value) throws Exception {
            return new BigDecimal(value);
        }

        @Override
        public String write(BigDecimal value) throws Exception {
            return value.toString();
        }
    }
} 