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
    MainMonitor = 0,
    ModeSelection,
    Settings,
    Preferences,
    DeviceInfo,
    WifiStatus,
    BluetoothStatus,
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
    unsigned long alertSentAnimMs = 0UL; // Timestamp of alert occurrence for screen animations
    bool buzzerEnabled = true;
    bool requestBuzzerChirp = false;
    
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
    const char* bluetoothName = "Flux";
    
    bool wifiConnected = false;
    const char* wifiSSID = "Flux";
    int wifiRSSI = 0;
    
    const char* supabaseSyncState = "Ready";
    unsigned long lastCloudSyncMs = 0UL;
    uint16_t pendingOfflineRecords = 0;
    
    // Navigation
    ScreenId activeScreen = ScreenId::MainMonitor;
    ScreenId previousScreen = ScreenId::MainMonitor;
    bool bootCompleted = false;
    
    // Metadata
    const char* firmwareVersion = "1.0.0";
    const char* deviceName = "Flux";
    
    // Backward compatibility helpers
    int sodiumValue = -1;
    const char* sodiumStatus = "OK";

    // Real-time Clock & Uptime Tracking
    bool timeSynced = false;
    unsigned long timeSyncedAtMs = 0UL;
    unsigned long initialEpochSec = 0UL;

    void getFormattedTime(char* out, size_t outSize) const noexcept {
        int h = 16;
        int m = 26;
        if (timeSynced && initialEpochSec > 0) {
            unsigned long elapsedSec = (millis() - timeSyncedAtMs) / 1000UL;
            unsigned long totalSec = initialEpochSec + elapsedSec;
            h = (totalSec / 3600UL) % 24;
            m = (totalSec / 60UL) % 60;
        } else {
            // Running clock initialized to match design mockup (base 04:26 PM)
            unsigned long totalSec = (16UL * 3600UL + 26UL * 60UL) + (millis() / 1000UL);
            h = (totalSec / 3600UL) % 24;
            m = (totalSec / 60UL) % 60;
        }
        int displayH = h % 12;
        if (displayH == 0) displayH = 12;
        const char* ampm = (h >= 12) ? "PM" : "AM";
        snprintf(out, outSize, "%02d:%02d %s", displayH, m, ampm);
    }

    void getFormattedUptime(char* out, size_t outSize) const noexcept {
        // Base 02:14:36 per design mockup
        unsigned long totalSec = (2UL * 3600UL + 14UL * 60UL + 36UL) + (millis() / 1000UL);
        unsigned long h = (totalSec / 3600UL) % 100;
        unsigned long m = (totalSec / 60UL) % 60;
        unsigned long s = totalSec % 60;
        snprintf(out, outSize, "%02lu:%02lu:%02lu", h, m, s);
    }

    // Live Temperature Sparkline / History Ring Buffer
    static constexpr size_t HistoryBufferSize = 32;
    float history[HistoryBufferSize]{};
    size_t historyCount = 0;
    size_t historyIndex = 0;

    void addHistory(float val) noexcept {
        if (val < -50.0f || val > 125.0f) return;
        history[historyIndex] = val;
        historyIndex = (historyIndex + 1) % HistoryBufferSize;
        if (historyCount < HistoryBufferSize) {
            historyCount++;
        }
    }
};

} // namespace Flux
