package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "FeatureBool")
public class FeatureBool extends Feature {
    
    @Element(name = "value", required = false)
    public boolean value;

    public FeatureBool() {}

    public FeatureBool(String name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }
} 