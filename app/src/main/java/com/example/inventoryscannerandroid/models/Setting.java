package com.example.inventoryscannerandroid.models;

import java.io.Serializable;

public class Setting implements Serializable {
    public String apiKey;

    public Setting() {}

    public Setting(String apiKey) {
        this.apiKey = apiKey;
    }
} 