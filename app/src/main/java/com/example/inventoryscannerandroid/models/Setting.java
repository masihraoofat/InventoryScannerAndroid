package com.example.inventoryscannerandroid.models;

import java.io.Serializable;

public class Setting implements Serializable {
    public String apiKey;
    public boolean manualVinValidationEnabled = true; // Default to enabled
    public boolean scanVinValidationEnabled = true; // Default to enabled

    public Setting() {}

    public Setting(String apiKey) {
        this.apiKey = apiKey;
        this.manualVinValidationEnabled = true;
        this.scanVinValidationEnabled = true;
    }
} 