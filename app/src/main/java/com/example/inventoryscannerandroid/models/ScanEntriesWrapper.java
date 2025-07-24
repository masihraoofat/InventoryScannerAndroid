package com.example.inventoryscannerandroid.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * XML wrapper class to match the original CodeList structure 
 * that the web service expects for posting scan entries
 */
@Root(name = "ArrayOfScanEntry")
public class ScanEntriesWrapper {
    
    @ElementList(name = "ScanEntry", inline = true)
    public List<ScanEntry> scanEntries;
    
    public ScanEntriesWrapper() {}
} 