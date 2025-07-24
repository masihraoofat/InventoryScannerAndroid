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
        option.zones.add(new Zone(1, "Zone A - New Vehicles", 0));
        option.zones.add(new Zone(2, "Zone B - Used Vehicles", 0));
        option.zones.add(new Zone(3, "Zone C - Service Area", 0));
        option.zones.add(new Zone(4, "Zone D - Parts Department", 0));
        
        // Create sample features
        option.features = new ArrayList<>();
        
        // Boolean features
        option.features.add(new FeatureBool("Has Keys", false));
        option.features.add(new FeatureBool("Clean", false));
        option.features.add(new FeatureBool("Damaged", false));
        option.features.add(new FeatureBool("Needs Inspection", false));
        
        // String features
        option.features.add(new FeatureString("Color", ""));
        option.features.add(new FeatureString("Model", ""));
        option.features.add(new FeatureString("Notes", ""));
        option.features.add(new FeatureString("Location Details", ""));
        
        // Integer features
        option.features.add(new FeatureInt("Year", 0));
        option.features.add(new FeatureInt("Mileage", 0));
        option.features.add(new FeatureInt("Condition Rating", 0));
        
        // Float features  
        option.features.add(new FeatureFloat("Price", BigDecimal.ZERO));
        option.features.add(new FeatureFloat("Trade Value", BigDecimal.ZERO));
        
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