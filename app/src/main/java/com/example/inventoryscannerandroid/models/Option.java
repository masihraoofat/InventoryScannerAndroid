package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Root(name = "Option")
public class Option implements Serializable {
    
    @ElementList(name = "zones", entry = "Zone", inline = false, required = false)
    public List<Zone> zones;
    
    @ElementList(name = "features", inline = false, required = false)
    public List<Feature> features;

    public Option() {
        this.zones = new ArrayList<>();
        this.features = new ArrayList<>();
    }
} 