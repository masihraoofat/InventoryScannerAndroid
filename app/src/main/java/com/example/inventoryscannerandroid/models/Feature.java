package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(name = "Feature")
public class Feature implements Serializable {
    
    @Element(name = "name")
    public String name;

    public Feature() {}

    public Feature(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
} 