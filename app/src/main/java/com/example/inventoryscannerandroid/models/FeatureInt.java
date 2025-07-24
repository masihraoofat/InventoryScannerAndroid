package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "FeatureInt")
public class FeatureInt extends Feature {
    
    @Element(name = "value", required = false)
    public int value;

    public FeatureInt() {}

    public FeatureInt(String name, int value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }
} 