package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;

@Root(name = "FeatureFloat")
public class FeatureFloat extends Feature {
    
    @Element(name = "value", required = false)
    public BigDecimal value;

    public FeatureFloat() {}

    public FeatureFloat(String name, BigDecimal value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }
} 