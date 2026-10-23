#pragma once

#include <Arduino.h>

namespace Flux {

enum class ProfileMode : uint8_t {
    Live = 0,
    Environment,
    Liquid,
    Fermentation,
    Count
};

inline const char* getProfileName(ProfileMode mode) noexcept {
    switch (mode) {
    case ProfileMode::Live: return "Live Monitor";
    case ProfileMode::Environment: return "Environment";
    case ProfileMode::Liquid: return "Liquid Probe";
    case ProfileMode::Fermentation: return "Fermentation";
    default: return "Unknown";
    }
}

enum class TempUnit : uint8_t {
    Celsius = 0,
    Fahrenheit
};

enum class ScreenId : uint8_t {
    Dashboard = 0,
    LiveMonitor,
    Environment,
    Liquid,
    Fermentation,
    StatisticsTimer,
    SystemStatus,
    Settings,
    Count
};

struct DeviceState {
    float temperature = 0.0f;           // Realtime temp in Celsius
    float temperatureF = 32.0f;         // Formatted F temp: (C * 9/5) + 32
    TempUnit preferredUnit = TempUnit::Celsius;
    ProfileMode activeProfile = ProfileMode::Live;
    
    // Session Statistics & Monotonic Timer
    float minTemperature = 999.0f;
    float maxTemperature = -999.0f;
    float avgTemperature = 0.0f;
    double tempSum = 0.0;
    uint32_t sampleCount = 0;
    unsigned long sessionStartMs = 0UL;
    
    // Thresholds & Alerts
    float lowThreshold = 18.0f;         // in Celsius
    float highThreshold = 28.0f;        // in Celsius
    bool isThresholdAlert = false;
    
    // Sensor Health
    bool sensorConnected = false;
    const char* sensorStatus = "Initializing";
    
    // Battery & Hardware
    int batteryPercentage = 87;
    float batteryVoltage = 4.02f;
    bool charging = false;
    
    // Connectivity & Cloud
    bool bluetoothConnected = false;
    bool bluetoothEnabled = true;
    const char* connectedDeviceName = "";
    unsigned long lastConnectionTime = 0UL;
    int signalStrength = -65;
    const char* bluetoothName = "Flux-Telemetry";
    
    bool wifiConnected = false;
    const char* wifiSSID = "None";
    int wifiRSSI = 0;
    
    const char* supabaseSyncState = "Ready";
    unsigned long lastCloudSyncMs = 0UL;
    uint16_t pendingOfflineRecords = 0;
    
    // Navigation
    ScreenId activeScreen = ScreenId::Dashboard;
    ScreenId previousScreen = ScreenId::Dashboard;
    bool bootCompleted = false;
    
    // Metadata
    const char* firmwareVersion = "v2.0.0";
    const char* deviceName = "Flux Telemetry v2";
    
    // Backward compatibility helpers
    int sodiumValue = -1;
    const char* sodiumStatus = "OK";
};

} // namespace Flux
