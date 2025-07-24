package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "FeatureString")
public class FeatureString extends Feature {
    
    @Element(name = "value", required = false)
    public String value;

    public FeatureString() {}

    public FeatureString(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public String getType() {
        return "FeatureString";
    }

    @Override
    public String toString() {
        return name;
    }
} 