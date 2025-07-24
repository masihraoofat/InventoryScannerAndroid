# Inventory Scanner Android

An Android port of the InventoryScan2008 Windows CE application, designed for scanning and managing vehicle VIN codes with configurable features and zone-based organization.

## Features

- **VIN Scanning**: Scan vehicle VIN codes using the device camera or enter manually
- **Zone Management**: Organize scanned entries by zones (e.g., different areas of a lot)
- **Dynamic Features**: Configurable feature controls (checkboxes, text fields, numbers)
- **Data Persistence**: Local storage of scan entries, zones, and settings
- **Web Synchronization**: Sync data with remote web service
- **VIN Validation**: Automatic validation and sanitization of VIN codes

## Architecture

### Models
- `ScanEntry`: Represents a scanned VIN with zone and features
- `Zone`: Represents a physical zone/area
- `Feature`: Base class for configurable features (Bool, String, Int, Float)
- `Option`: Configuration data containing zones and features
- `Setting`: Application settings including API key

### Core Components
- `MainActivity`: Main application interface
- `FeatureControlView`: Dynamic UI generation for features
- `Persistence`: Data storage using SharedPreferences and JSON
- `HttpClient`: Web service communication
- `VinValidator`: VIN code validation and sanitization

### Key Features Ported from Original

1. **Barcode Scanning**: Integrated ZXing library for camera-based scanning
2. **Zone-based Organization**: Entries are organized by selected zones
3. **Dynamic Feature Controls**: UI controls are generated based on configuration
4. **VIN Validation**: Same regex pattern as original for VIN validation
5. **Web Service Sync**: HTTP communication with original API endpoints
6. **Data Persistence**: Local storage replacing XML files

## Setup and Installation

### Prerequisites
- Android Studio
- Android SDK (API level 24+)
- Device or emulator with camera support

### Dependencies
- ZXing Android Embedded: Barcode scanning
- Gson: JSON serialization
- OkHttp: HTTP client
- AndroidX libraries

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync project dependencies
4. Build and run

## Usage

### Initial Setup
1. Launch the app
2. Sample zones and features will be loaded automatically
3. Enter API key when prompted for synchronization

### Scanning Process
1. Select a zone from the dropdown
2. Either:
   - Tap "Scan Barcode" and scan a VIN
   - Enter VIN manually and tap "Add"
3. Configure features using the dynamic controls
4. VIN appears in the list for the selected zone

### Synchronization
1. Tap "Sync" button
2. Enter API key if not already set
3. App downloads latest configuration and uploads scan entries
4. Successful sync prompts to clear local entries

### Data Management
- **Delete**: Remove individual entries
- **Clear All**: Remove all entries (with confirmation)
- **Zone Switching**: View entries by selected zone

## Technical Details

### VIN Validation
Uses the same regex pattern as the original:
```
([A-HJ-NPR-Z\d]{3})([A-HJ-NPR-Z\d]{5})([\dX])(([A-HJ-NPR-Z\d])([A-HJ-NPR-Z\d])([A-HJ-NPR-Z\d]{6}))
```

### Data Storage
- **Scan Entries**: JSON in SharedPreferences
- **Configuration**: JSON in SharedPreferences  
- **Settings**: JSON in SharedPreferences

### Feature Types
- **FeatureBool**: Checkbox controls
- **FeatureString**: Text input controls
- **FeatureInt**: Numeric input controls
- **FeatureFloat**: Decimal input controls

### API Endpoints
- **Debug**: `http://webservice.pc-2416.hgregoire.com/inventory/`
- **Release**: `http://webservice.hgregoire.com/inventory/`

## Permissions Required

- `CAMERA`: For barcode scanning
- `INTERNET`: For web service communication
- `ACCESS_NETWORK_STATE`: For network status checking

## Differences from Original

### Enhancements
- Modern Android UI with Material Design elements
- Touch-friendly interface optimized for mobile devices
- Better error handling and user feedback
- Automatic sample data loading for testing
- Improved VIN validation feedback

### Platform Adaptations
- JSON instead of XML for data serialization
- SharedPreferences instead of file-based storage
- Android permissions system integration
- Mobile-optimized layouts and controls

## Development Notes

### Sample Data
The app automatically loads sample zones and features for testing:
- 4 predefined zones (A-D)
- Mix of boolean, string, integer, and float features
- Typical automotive inventory features

### API Integration
The app maintains compatibility with the original web service API, using the same authentication scheme and data formats.

### Single Instance
Unlike the original Windows CE version, the Android app doesn't enforce single instance operation due to platform differences.

## Contributing

This is a faithful port of the original InventoryScan2008 application adapted for Android. When making changes, consider maintaining compatibility with the original API and data structures where possible. 