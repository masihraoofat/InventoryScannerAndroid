package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(name = "Zone")
public class Zone implements Serializable {
    
    @Element(name = "id")
    public int id;
    
    @Element(name = "name")
    public String name;
    
    @Element(name = "parentId", required = false)
    public int parentId = 0; // Default value

    public Zone() {}

    public Zone(int id, String name, int parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return name;
    }
} 