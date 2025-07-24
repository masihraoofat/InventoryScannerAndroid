package com.example.inventoryscannerandroid.utils;

import android.util.Log;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.inventoryscannerandroid.models.Feature;
import com.example.inventoryscannerandroid.models.FeatureBool;
import com.example.inventoryscannerandroid.models.FeatureFloat;
import com.example.inventoryscannerandroid.models.FeatureInt;
import com.example.inventoryscannerandroid.models.FeatureString;

public class FeatureListConverter implements Converter<List<Feature>> {
    private static final String TAG = "FeatureListConverter";

    @Override
    public List<Feature> read(InputNode node) throws Exception {
        List<Feature> features = new ArrayList<>();
        
        InputNode child = node.getNext();
        while (child != null) {
            Feature feature = readFeature(child);
            if (feature != null) {
                features.add(feature);
                Log.d(TAG, "Added feature: " + child.getName() + " -> " + feature.getClass().getSimpleName() + " name: " + feature.name);
            }
            child = node.getNext();
        }
        
        return features;
    }
    
    private Feature readFeature(InputNode node) throws Exception {
        String elementName = node.getName();
        String name = null;
        
        // Read the name element
        InputNode nameNode = node.getNext("name");
        if (nameNode != null) {
            name = nameNode.getValue();
        }
        
        // Create appropriate Feature subclass based on element name
        Feature feature;
        switch (elementName) {
            case "FeatureBool":
                feature = new FeatureBool();
                if (name != null) feature.name = name;
                
                // Read boolean value if present
                InputNode valueNode = node.getNext("value");
                if (valueNode != null) {
                    ((FeatureBool) feature).value = Boolean.parseBoolean(valueNode.getValue());
                }
                break;
            case "FeatureString":
                feature = new FeatureString();
                if (name != null) feature.name = name;
                
                // Read string value if present
                valueNode = node.getNext("value");
                if (valueNode != null) {
                    ((FeatureString) feature).value = valueNode.getValue();
                }
                break;
            case "FeatureInt":
                feature = new FeatureInt();
                if (name != null) feature.name = name;
                
                // Read int value if present
                valueNode = node.getNext("value");
                if (valueNode != null) {
                    ((FeatureInt) feature).value = Integer.parseInt(valueNode.getValue());
                }
                break;
            case "FeatureFloat":
                feature = new FeatureFloat();
                if (name != null) feature.name = name;
                
                // Read BigDecimal value if present
                valueNode = node.getNext("value");
                if (valueNode != null) {
                    ((FeatureFloat) feature).value = new BigDecimal(valueNode.getValue());
                }
                break;
            default:
                feature = new Feature();
                if (name != null) feature.name = name;
                break;
        }
        
        return feature;
    }
    
    @Override
    public void write(OutputNode node, List<Feature> features) throws Exception {
        // Implementation for writing XML (if needed)
        for (Feature feature : features) {
            String elementName = feature.getClass().getSimpleName();
            OutputNode child = node.getChild(elementName);
            child.getChild("name").setValue(feature.name);
            
            if (feature instanceof FeatureBool) {
                child.getChild("value").setValue(String.valueOf(((FeatureBool) feature).value));
            } else if (feature instanceof FeatureString) {
                child.getChild("value").setValue(((FeatureString) feature).value);
            } else if (feature instanceof FeatureInt) {
                child.getChild("value").setValue(String.valueOf(((FeatureInt) feature).value));
            } else if (feature instanceof FeatureFloat) {
                child.getChild("value").setValue(((FeatureFloat) feature).value.toString());
            }
        }
    }
} 