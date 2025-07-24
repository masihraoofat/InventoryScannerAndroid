package com.example.inventoryscannerandroid.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.inventoryscannerandroid.models.*;

public class SampleDataLoader {
    
    public static Option createSampleOption() {
        Option option = new Option();
        
        // Create sample zones
        option.zones = new ArrayList<>();
        option.zones.add(new Zone(1, "Not Loaded", 0));

        
        // Create sample features
        option.features = new ArrayList<>();
        
        // Boolean features
        option.features.add(new FeatureBool("Not Loaded", false));

        
        return option;
    }
    
    public static void loadSampleDataIfEmpty(Persistence persistence) {
        Option option = persistence.loadOption();
        
        // If no zones exist, load sample data
        if (option.zones.isEmpty()) {
            Option sampleOption = createSampleOption();
            persistence.saveOption(sampleOption);
        }
    }
} 