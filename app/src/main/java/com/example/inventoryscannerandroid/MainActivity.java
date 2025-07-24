package com.example.inventoryscannerandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.client.android.BuildConfig;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

import com.example.inventoryscannerandroid.models.*;
import com.example.inventoryscannerandroid.utils.*;
import com.example.inventoryscannerandroid.views.FeatureControlView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    
    // UI Components
    private Spinner spinnerZone;
    private TextView textCurrentZone;
    private EditText editManualVin;
    private Button btnManualAdd;
    private Button btnScan;
    private LinearLayout layoutFeatures;
    private ListView listVins;
    private Button btnDelete;
    private Button btnClear;
    private Button btnSync;
    
    // Data and Utilities
    private Persistence persistence;
    private HttpClient httpClient;
    private XmlHttpClient xmlHttpClient; // XML client for original web service compatibility
    private FeatureControlView featureControlView;
    
    // Data
    private List<ScanEntry> scanEntries;
    private Option option;
    private Setting setting;
    private Zone currentZone;
    private ScanEntry selectedEntry;
    
    // Adapters
    private ArrayAdapter<Zone> zoneAdapter;
    private ArrayAdapter<ScanEntry> vinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        initializeData();
        setupEventListeners();
        loadFromStorage();
        updateUI();
    }

    private void initializeComponents() {
        // Find UI components
        spinnerZone = findViewById(R.id.spinnerZone);
        textCurrentZone = findViewById(R.id.textCurrentZone);
        editManualVin = findViewById(R.id.editManualVin);
        btnManualAdd = findViewById(R.id.btnManualAdd);
        btnScan = findViewById(R.id.btnScan);
        layoutFeatures = findViewById(R.id.layoutFeatures);
        listVins = findViewById(R.id.listVins);
        btnDelete = findViewById(R.id.btnDelete);
        btnClear = findViewById(R.id.btnClear);
        btnSync = findViewById(R.id.btnSync);
        
        // Initialize utilities
        persistence = new Persistence(this);
        httpClient = new HttpClient(BuildConfig.DEBUG);
        xmlHttpClient = new XmlHttpClient(BuildConfig.DEBUG); // XML client for compatibility
        featureControlView = new FeatureControlView(this, layoutFeatures);
    }

    private void initializeData() {
        scanEntries = new ArrayList<>();
        option = new Option();
        setting = new Setting();
        
        // Initialize adapters
        zoneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZone.setAdapter(zoneAdapter);
        
        vinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listVins.setAdapter(vinAdapter);
    }

    private void setupEventListeners() {
        // Zone selection
        spinnerZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < option.zones.size()) {
                    selectedEntry = null;
                    editManualVin.setText("");
                    listVins.clearChoices();
                    currentZone = option.zones.get(position);
                    updateUI();
                    saveToStorage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // VIN list selection
        listVins.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedEntry != null) {
                featureControlView.setEntryFeatures(selectedEntry);
            }
            selectedEntry = vinAdapter.getItem(position);
            if (selectedEntry != null) {
                editManualVin.setText(selectedEntry.code);
                updateButtonStates();
                updateFeatureControls();
            }
        });

        // Manual VIN input
        editManualVin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonStates();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Manual add button
        btnManualAdd.setOnClickListener(v -> {
            String vin = editManualVin.getText().toString().trim().toUpperCase();
            
            if (!VinValidator.isValidVin(vin)) {
                showError("Invalid VIN number");
                return;
            }

            String sanitizedVin = VinValidator.sanitizeVin(vin);
            
            if (selectedEntry == null) {
                addScanEntry(sanitizedVin);
                selectedEntry = null;
            } else {
                selectedEntry.code = sanitizedVin;
                vinAdapter.notifyDataSetChanged();
            }

            editManualVin.setText("");
            updateButtonStates();
            updateFeatureControls();
        });

        // Scan button
        btnScan.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startBarcodeScanning();
            }
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            if (selectedEntry != null) {
                showConfirmDialog("Delete " + selectedEntry.code + "?", () -> {
                    scanEntries.remove(selectedEntry);
                    vinAdapter.remove(selectedEntry);
                    editManualVin.setText("");
                    selectedEntry = null;
                    updateButtonStates();
                });
            }
        });

        // Clear button
        btnClear.setOnClickListener(v -> {
            if (!scanEntries.isEmpty()) {
                showConfirmDialog("Clear all entries?", () -> {
                    scanEntries.clear();
                    vinAdapter.clear();
                    editManualVin.setText("");
                    selectedEntry = null;
                    updateUI();
                });
            }
        });

        // Sync button
        btnSync.setOnClickListener(v -> {
            if (setting.apiKey == null || setting.apiKey.isEmpty()) {
                showApiKeyDialog();
            } else {
                performSync();
            }
        });
    }

    private void loadFromStorage() {
        scanEntries = persistence.loadScanEntries();
        option = persistence.loadOption();
        setting = persistence.loadSetting();

        // Load sample data if empty (for testing purposes)
        SampleDataLoader.loadSampleDataIfEmpty(persistence);
        option = persistence.loadOption(); // Reload after potential sample data load

        // Populate zone spinner
        zoneAdapter.clear();
        zoneAdapter.addAll(option.zones);
        
        if (!option.zones.isEmpty() && currentZone == null) {
            currentZone = option.zones.get(0);
            spinnerZone.setSelection(0);
        }
    }

    private void saveToStorage() {
        try {
            persistence.saveScanEntries(scanEntries);
            persistence.saveOption(option);
            // Don't save setting automatically to avoid overwriting API key
        } catch (Exception e) {
            Log.e(TAG, "Error saving to storage", e);
        }
    }

    private void updateUI() {
        // Update zone display
        if (currentZone != null) {
            textCurrentZone.setText(currentZone.name);
        } else {
            textCurrentZone.setText("No zone selected");
        }

        // Update VIN list
        vinAdapter.clear();
        if (currentZone != null) {
            for (ScanEntry entry : scanEntries) {
                if (entry.zoneId == currentZone.id) {
                    vinAdapter.add(entry);
                }
            }
        }

        updateButtonStates();
        updateFeatureControls();
    }

    private void updateButtonStates() {
        String vinText = editManualVin.getText().toString().trim();
        boolean validVin = VinValidator.isValidVin(vinText);
        
        btnManualAdd.setEnabled(validVin);
        btnManualAdd.setText(selectedEntry != null ? "Update" : "Add");
        btnDelete.setEnabled(selectedEntry != null);
        btnClear.setEnabled(!scanEntries.isEmpty());
        btnSync.setEnabled(!scanEntries.isEmpty());
    }

    private void updateFeatureControls() {
        featureControlView.updateFeatureControls(option.features, selectedEntry);
    }

    private void addScanEntry(String code) {
        if (currentZone == null) {
            showError("Please select a zone first");
            return;
        }

        // Remove existing entry with same code
        scanEntries.removeIf(entry -> entry.code.equals(code));
        vinAdapter.clear();

        // Create new entry
        ScanEntry entry = new ScanEntry(code, currentZone.id);
        featureControlView.setEntryFeatures(entry);
        
        scanEntries.add(entry);
        
        // Update UI
        updateUI();
        
        // Select the new entry
        for (int i = 0; i < vinAdapter.getCount(); i++) {
            if (vinAdapter.getItem(i).code.equals(code)) {
                listVins.setSelection(i);
                selectedEntry = vinAdapter.getItem(i);
                break;
            }
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void startBarcodeScanning() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a VIN barcode");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String scannedCode = VinValidator.sanitizeVin(result.getContents());
                
                if (!VinValidator.isValidVin(scannedCode)) {
                    selectedEntry = null;
                    editManualVin.setText(result.getContents());
                    showError("Invalid VIN number: " + result.getContents());
                    updateButtonStates();
                    return;
                }

                addScanEntry(scannedCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanning();
            } else {
                Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showConfirmDialog(String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    private void showApiKeyDialog() {
        EditText editText = new EditText(this);
        editText.setHint("Enter API Key");
        if (setting.apiKey != null) {
            editText.setText(setting.apiKey);
        }

        new AlertDialog.Builder(this)
                .setTitle("API Key Required")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String apiKey = editText.getText().toString().trim();
                    if (!apiKey.isEmpty()) {
                        setting.apiKey = apiKey;
                        persistence.saveSetting(setting);
                        performSync();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSync() {
        saveToStorage();
        
        // First get options using XML client (matches original exactly)
        xmlHttpClient.getOptions(setting.apiKey, new XmlHttpClient.OptionsCallback() {
            @Override
            public void onSuccess(Option newOption) {
                runOnUiThread(() -> {
                    option = newOption;
                    persistence.saveOption(option);
                    
                    // Update zone spinner
                    zoneAdapter.clear();
                    zoneAdapter.addAll(option.zones);
                    
                    // Post scan entries using XML client (matches original exactly)
                    xmlHttpClient.postScanEntries(setting.apiKey, scanEntries, new XmlHttpClient.SyncCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Sync successful", Toast.LENGTH_LONG).show();
                                
                                // Clear entries after successful sync
                                showConfirmDialog("Sync successful! Clear all entries?", () -> {
                                    scanEntries.clear();
                                    vinAdapter.clear();
                                    editManualVin.setText("");
                                    selectedEntry = null;
                                    updateUI();
                                });
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> showError("Sync failed: " + error));
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showError("Failed to get options using API Key: " + error));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (selectedEntry != null) {
            featureControlView.setEntryFeatures(selectedEntry);
        }
        saveToStorage();
    }
} 