package com.example.inventoryscannerandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventoryscannerandroid.utils.Persistence;

public class SettingsActivity extends AppCompatActivity {
    
    private Button btnRemoveApiKey;
    private Switch switchManualValidation;
    private Switch switchScanValidation;
    private Persistence persistence;
    private com.example.inventoryscannerandroid.models.Setting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        
        initializeComponents();
        loadSettings();
        setupEventListeners();
    }

    private void initializeComponents() {
        btnRemoveApiKey = findViewById(R.id.btnRemoveApiKey);
        switchManualValidation = findViewById(R.id.switchManualValidation);
        switchScanValidation = findViewById(R.id.switchScanValidation);
        
        persistence = new Persistence(this);
    }

    private void loadSettings() {
        setting = persistence.loadSetting();
        updateVinValidationSwitches();
    }

    private void setupEventListeners() {
        btnRemoveApiKey.setOnClickListener(v -> {
            // Show confirmation dialog before removing API key
            new AlertDialog.Builder(this)
                    .setTitle("Remove API Key")
                    .setMessage("Are you sure you want to remove the API key? You will need to enter a new one to use the app.")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        // Remove the API key
                        setting.apiKey = "";
                        persistence.saveSetting(setting);
                        Toast.makeText(this, "API Key removed. You will need to enter a new one when you return to the main screen.", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        switchManualValidation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setting.manualVinValidationEnabled = isChecked;
            persistence.saveSetting(setting);
            
            String message = isChecked ? 
                "Manual VIN validation enabled. Only valid VIN numbers will be accepted for manual entry." :
                "Manual VIN validation disabled. Any string can be entered manually.";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        switchScanValidation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setting.scanVinValidationEnabled = isChecked;
            persistence.saveSetting(setting);
            
            String message = isChecked ? 
                "Scan VIN validation enabled. Only valid VIN numbers will be accepted from scanning." :
                "Scan VIN validation disabled. Any scanned code will be accepted.";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateVinValidationSwitches() {
        switchManualValidation.setChecked(setting.manualVinValidationEnabled);
        switchScanValidation.setChecked(setting.scanVinValidationEnabled);
    }

    @Override
    public void onBackPressed() {
        // Allow back navigation - API key will be handled in MainActivity
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 