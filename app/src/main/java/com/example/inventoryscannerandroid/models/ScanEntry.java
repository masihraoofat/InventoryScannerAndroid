package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Root(name = "ScanEntry")
public class ScanEntry implements Serializable {
    
    @Element(name = "zoneId")
    public int zoneId;
    
    @Element(name = "code")
    public String code;
    
    @ElementList(name = "features", required = false, inline = true)
    public List<Feature> features;

    public ScanEntry() {
        this.features = new ArrayList<>();
    }

    public ScanEntry(String code, int zoneId) {
        this.code = code;
        this.zoneId = zoneId;
        this.features = new ArrayList<>();
    }

    @Override
    public String toString() {
        List<Feature> enabledFeatures = new ArrayList<>();
        for (Feature feature : this.features) {
            if (feature instanceof FeatureBool && ((FeatureBool) feature).value) {
                enabledFeatures.add(feature);
            }
        }
        if (!enabledFeatures.isEmpty()) {
            return code + " - " + enabledFeatures.toString();
        }
        return code;
    }
} 