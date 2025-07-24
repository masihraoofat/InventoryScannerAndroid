package com.example.inventoryscannerandroid.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VinValidator {
    private static final Pattern VIN_PATTERN = Pattern.compile("([A-HJ-NPR-Z\\d]{3})([A-HJ-NPR-Z\\d]{5})([\\dX])(([A-HJ-NPR-Z\\d])([A-HJ-NPR-Z\\d])([A-HJ-NPR-Z\\d]{6}))");
    
    public static String sanitizeVin(String vin) {
        if (vin == null) return "";
        
        Matcher matcher = VIN_PATTERN.matcher(vin.toUpperCase());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }
    
    public static boolean isValidVin(String vin) {
        if (vin == null) return false;
        String sanitized = sanitizeVin(vin);
        return sanitized.length() == 17;
    }
} 