package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Root(name = "Option")
public class Option implements Serializable {
    
    @ElementList(name = "zones", entry = "Zone", inline = false, required = false)
    public List<Zone> zones;
    
    @Element(name = "features", required = false)
    @Convert(com.example.inventoryscannerandroid.utils.FeatureListConverter.class)
    public List<Feature> features;

    public Option() {
        this.zones = new ArrayList<>();
        this.features = new ArrayList<>();
    }
    
    /**
     * Post-process features to convert them to correct subtypes based on XML element names.
     * This method should be called after XML deserialization.
     */
    public void processFeatureTypes() {
        // This will be implemented to convert Feature objects to their correct subtypes
        // based on additional XML attributes or element context
    }
} 