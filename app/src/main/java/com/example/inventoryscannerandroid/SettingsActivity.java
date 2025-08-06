package com.example.inventoryscannerandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventoryscannerandroid.utils.Persistence;

public class SettingsActivity extends AppCompatActivity {
    
    private Button btnRemoveApiKey;
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
        
        persistence = new Persistence(this);
    }

    private void loadSettings() {
        setting = persistence.loadSetting();
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