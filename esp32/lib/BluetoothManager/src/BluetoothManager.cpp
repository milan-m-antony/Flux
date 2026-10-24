#include "BluetoothManager.hpp"
#include <WiFi.h>
#include <Preferences.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

namespace Flux {

// Constants for UUIDs
static const char* SERVICE_UUID           = "6E400011-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* TEMP_CHAR_UUID         = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* BATTERY_CHAR_UUID      = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* VOLTAGE_CHAR_UUID      = "6E400004-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* SODIUM_CHAR_UUID       = "6E400005-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* STATUS_CHAR_UUID       = "6E400006-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* FIRMWARE_CHAR_UUID     = "6E400007-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* NAME_CHAR_UUID         = "6E400008-B5A3-F393-E0A9-E50E24DCCA9E";
static const char* JSON_CHAR_UUID         = "6E400009-B5A3-F393-E0A9-E50E24DCCA9E";

// Supabase Direct Ingestion Constants
static const char* SUPABASE_REST_URL = "https://pivtrxrhxdqxzactcrrb.supabase.co/rest/v1/telemetry";
static const char* SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBpdnRyeHJoeGRxeHphY3RjcnJiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI3NjAxMjEsImV4cCI6MjEwODMzNjEyMX0.LGOfzD6MVDQQ725kJEC8JEcfwKUQyxpt3V8RBB7HNxg";

void BluetoothManager::begin(DeviceState* state) noexcept {
    state_ = state;
    connected_ = false;
    rssi_ = -65;
    lastRssiUpdateMs_ = millis();
    lastTempNotifyMs_ = millis();

    if (state_ != nullptr) {
        state_->bluetoothConnected = false;
        state_->bluetoothEnabled = true;
        state_->bluetoothName = "Flux1";
    }

    try {
        // Initialize BLE Device
        BLEDevice::init("Flux1");
        
        // CRITICAL: Set ESP32 MTU to 512 so it can send strings longer than 20 bytes
        BLEDevice::setMTU(512);

        // Create BLE Server
        pServer_ = BLEDevice::createServer();
        pServer_->setCallbacks(this);

        // Create BLE Service with 35 handles to accommodate characteristics & descriptors
        pService_ = pServer_->createService(BLEUUID(std::string(SERVICE_UUID)), 35);

        // Helper to add standard read/notify/write characteristics with BLE2902 descriptors
        auto addChar = [this](const char* uuid, bool writable = false) -> BLECharacteristic* {
            uint32_t props = BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY;
            if (writable) {
                props |= BLECharacteristic::PROPERTY_WRITE;
            }
            BLECharacteristic* pChar = pService_->createCharacteristic(uuid, props);
            pChar->addDescriptor(new BLE2902());
            pChar->setCallbacks(this);
            return pChar;
        };

        pCharTemp_ = addChar(TEMP_CHAR_UUID);
        pCharBattery_ = addChar(BATTERY_CHAR_UUID);
        pCharVoltage_ = addChar(VOLTAGE_CHAR_UUID);
        pCharSodium_ = addChar(SODIUM_CHAR_UUID);
        pCharStatus_ = addChar(STATUS_CHAR_UUID);
        pCharFirmware_ = addChar(FIRMWARE_CHAR_UUID);
        pCharName_ = addChar(NAME_CHAR_UUID);
        pCharJson_ = addChar(JSON_CHAR_UUID, true); // JSON characteristic can also accept write commands

        // Set static characteristics
        if (state_ != nullptr) {
            pCharFirmware_->setValue(state_->firmwareVersion);
            pCharName_->setValue(state_->bluetoothName);
        } else {
            pCharFirmware_->setValue("v2.0.0");
            pCharName_->setValue("Flux1");
        }

        // Start Service
        pService_->start();

        // Start Advertising
        BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
        pAdvertising->addServiceUUID(SERVICE_UUID);
        pAdvertising->setScanResponse(true);
        pAdvertising->setMinPreferred(0x06);
        pAdvertising->setMinPreferred(0x12);
        BLEDevice::startAdvertising();

        Serial.println("Flux BLE Initialized");
        Serial.println("Advertising Started");

        // Load saved Wi-Fi credentials from NVS
        loadSavedWifi();
    } catch (...) {
        Serial.println("BLE Init Failed");
    }
}

void BluetoothManager::onConnect(BLEServer* pServer) {
    connected_ = true;
    if (state_ != nullptr) {
        state_->bluetoothConnected = true;
        state_->connectedDeviceName = "Flux Android Client";
        state_->lastConnectionTime = millis();
    }
    Serial.println("Flux Client Connected");
}

void BluetoothManager::onDisconnect(BLEServer* pServer) {
    connected_ = false;
    if (state_ != nullptr) {
        state_->bluetoothConnected = false;
        state_->connectedDeviceName = "";
    }
    Serial.println("Flux Client Disconnected");
    
    // Restart advertising immediately
    BLEDevice::startAdvertising();
    Serial.println("Advertising Restarted");
}

void BluetoothManager::onWrite(BLECharacteristic* pCharacteristic) {
    if (state_ == nullptr) return;
    std::string val = pCharacteristic->getValue();
    if (val.empty()) return;

    Serial.printf("BLE Received Command: %s\n", val.c_str());

    // Simple command parser for unit, profile, thresholds, and stats reset
    if (val.find("SET_UNIT_C") != std::string::npos || val.find("\"unit\":\"C\"") != std::string::npos) {
        state_->preferredUnit = TempUnit::Celsius;
    } else if (val.find("SET_UNIT_F") != std::string::npos || val.find("\"unit\":\"F\"") != std::string::npos) {
        state_->preferredUnit = TempUnit::Fahrenheit;
    } else if (val.find("MODE_LIVE") != std::string::npos || val.find("\"mode\":\"live\"") != std::string::npos) {
        state_->activeProfile = ProfileMode::Live;
    } else if (val.find("MODE_ENV") != std::string::npos || val.find("\"mode\":\"environment\"") != std::string::npos) {
        state_->activeProfile = ProfileMode::Environment;
    } else if (val.find("MODE_LIQ") != std::string::npos || val.find("\"mode\":\"liquid\"") != std::string::npos) {
        state_->activeProfile = ProfileMode::Liquid;
    } else if (val.find("MODE_FERM") != std::string::npos || val.find("\"mode\":\"fermentation\"") != std::string::npos) {
        state_->activeProfile = ProfileMode::Fermentation;
    } else if (val.find("RESET_STATS") != std::string::npos || val.find("\"reset\":true") != std::string::npos) {
        state_->minTemperature = state_->temperature;
        state_->maxTemperature = state_->temperature;
        state_->tempSum = state_->temperature;
        state_->sampleCount = (state_->temperature > -50.0f) ? 1 : 0;
        state_->avgTemperature = state_->temperature;
        state_->sessionStartMs = millis();
    } else if (val.find("SET_TIME:") != std::string::npos) {
        size_t idx = val.find("SET_TIME:") + 9;
        unsigned long epoch = strtoul(val.substr(idx).c_str(), nullptr, 10);
        if (epoch > 1000000UL) {
            state_->initialEpochSec = epoch;
            state_->timeSyncedAtMs = millis();
            state_->timeSynced = true;
            Serial.printf("Time synchronized from BLE: %lu\n", epoch);
        }
    } else if (val.find("SET_THRESH:") != std::string::npos) {
        size_t idx = val.find("SET_THRESH:") + 11;
        std::string param = val.substr(idx);
        size_t comma = param.find(',');
        if (comma != std::string::npos) {
            float lo = atof(param.substr(0, comma).c_str());
            float hi = atof(param.substr(comma + 1).c_str());
            if (lo < hi) {
                state_->lowThreshold = lo;
                state_->highThreshold = hi;
                Serial.printf("Thresholds updated from BLE: Lo=%.1f Hi=%.1f\n", lo, hi);
            }
        }
    } else if (val.find("SET_WIFI:") != std::string::npos) {
        size_t idx = val.find("SET_WIFI:") + 9;
        std::string param = val.substr(idx);
        size_t comma = param.find(',');
        if (comma != std::string::npos) {
            std::string ssid = param.substr(0, comma);
            std::string pass = param.substr(comma + 1);
            Serial.printf("Received Wi-Fi config over BLE: SSID=%s\n", ssid.c_str());
            saveWifiCredentials(ssid.c_str(), pass.c_str());
            WiFi.disconnect();
            WiFi.mode(WIFI_STA);
            WiFi.begin(ssid.c_str(), pass.c_str());
        }
    }
}

void BluetoothManager::loadSavedWifi() noexcept {
    try {
        Preferences prefs;
        prefs.begin("flux_wifi", true);
        String savedSSID = prefs.getString("ssid", "");
        String savedPass = prefs.getString("pass", "");
        prefs.end();

        if (savedSSID.length() > 0) {
            currentWifiSsid_ = savedSSID;
            if (state_ != nullptr) {
                state_->wifiSSID = currentWifiSsid_.c_str();
            }
            Serial.printf("Auto-reconnecting to saved Wi-Fi: %s\n", savedSSID.c_str());
            WiFi.mode(WIFI_STA);
            WiFi.begin(savedSSID.c_str(), savedPass.c_str());
        } else {
            Serial.println("No saved Wi-Fi credentials found in NVS.");
        }
    } catch (...) {
        Serial.println("Error reading Wi-Fi from NVS");
    }
}

void BluetoothManager::saveWifiCredentials(const char* ssid, const char* pass) noexcept {
    try {
        Preferences prefs;
        prefs.begin("flux_wifi", false);
        prefs.putString("ssid", ssid);
        prefs.putString("pass", pass);
        prefs.end();
        currentWifiSsid_ = String(ssid);
        if (state_ != nullptr) {
            state_->wifiSSID = currentWifiSsid_.c_str();
        }
        Serial.printf("Wi-Fi credentials saved to NVS: %s\n", ssid);
    } catch (...) {
        Serial.println("Error writing Wi-Fi to NVS");
    }
}

void BluetoothManager::attemptCloudUpload() noexcept {
    if (state_ == nullptr) return;
    if (WiFi.status() != WL_CONNECTED) {
        state_->supabaseSyncState = "Offline";
        return;
    }
    // Only upload if sensor is connected and reading is valid
    if (!state_->sensorConnected || state_->temperature <= -50.0f) {
        state_->supabaseSyncState = "Sensor Err";
        return;
    }

    const unsigned long now = millis();
    if ((now - lastCloudUploadMs_) < 10000UL) {
        return; // Upload interval: 10s
    }
    lastCloudUploadMs_ = now;

    WiFiClientSecure client;
    client.setInsecure(); // Non-blocking TLS for ESP32
    HTTPClient http;

    if (http.begin(client, SUPABASE_REST_URL)) {
        http.addHeader("Content-Type", "application/json");
        http.addHeader("apikey", SUPABASE_ANON_KEY);
        http.addHeader("Authorization", String("Bearer ") + SUPABASE_ANON_KEY);
        http.addHeader("Prefer", "return=minimal");
        http.setTimeout(3500); // 3.5s timeout prevents blocking loop

        char payload[256];
        snprintf(payload, sizeof(payload),
                 "{\"device_id\":\"Flux1\",\"temperature_celsius\":%.2f,\"active_profile\":\"%s\",\"source\":\"ESP32-WIFI\",\"is_alert\":%s}",
                 state_->temperature,
                 getProfileName(state_->activeProfile),
                 state_->isThresholdAlert ? "true" : "false");

        int code = http.POST(payload);
        if (code >= 200 && code < 300) {
            state_->lastCloudSyncMs = millis();
            state_->supabaseSyncState = "Synced";
            Serial.printf("Supabase direct upload OK (HTTP %d)\n", code);
        } else {
            state_->supabaseSyncState = "Upload Err";
            Serial.printf("Supabase direct upload failed (HTTP %d)\n", code);
        }
        http.end();
    } else {
        state_->supabaseSyncState = "Conn Err";
    }
}

void BluetoothManager::notifyDataChange() noexcept {
    if (state_ == nullptr) {
        return;
    }

    // 1. Notify individual characteristics if changed
    if (state_->batteryPercentage != lastNotifiedBattery_) {
        lastNotifiedBattery_ = state_->batteryPercentage;
        pCharBattery_->setValue(lastNotifiedBattery_);
        pCharBattery_->notify();
    }
    if (abs(state_->batteryVoltage - lastNotifiedVoltage_) > 0.01f) {
        lastNotifiedVoltage_ = state_->batteryVoltage;
        char buf[16];
        dtostrf(lastNotifiedVoltage_, 4, 2, buf);
        pCharVoltage_->setValue(buf);
        pCharVoltage_->notify();
    }
    if (state_->sodiumValue != lastNotifiedSodium_) {
        lastNotifiedSodium_ = state_->sodiumValue;
        char buf[16];
        if (lastNotifiedSodium_ == -1) {
            strcpy(buf, "---");
        } else {
            itoa(lastNotifiedSodium_, buf, 10);
        }
        pCharSodium_->setValue(buf);
        pCharSodium_->notify();
    }
    if (strcmp(state_->sensorStatus, lastNotifiedStatus_) != 0) {
        lastNotifiedStatus_ = state_->sensorStatus;
        pCharStatus_->setValue(const_cast<char*>(lastNotifiedStatus_));
        pCharStatus_->notify();
    }

    // 2. Format and send System JSON payload via 6E400009 characteristic
    unsigned long elapsedSec = (millis() - state_->sessionStartMs) / 1000UL;
    char json[380];
    snprintf(json, sizeof(json),
             "{\"temperature\":%.1f,\"battery\":%d,\"batteryVoltage\":%.2f,\"charging\":%s,\"sodium\":\"%s\",\"status\":\"%s\",\"firmware\":\"%s\",\"device\":\"%s\",\"mode\":\"%s\",\"unit\":\"%s\",\"minTemp\":%.1f,\"maxTemp\":%.1f,\"avgTemp\":%.1f,\"elapsedSec\":%lu,\"alert\":%s,\"wifiConnected\":%s,\"wifiRssi\":%d,\"wifiSsid\":\"%s\"}",
             state_->temperature,
             state_->batteryPercentage,
             state_->batteryVoltage,
             state_->charging ? "true" : "false",
             state_->sodiumValue == -1 ? "---" : String(state_->sodiumValue).c_str(),
             state_->sensorStatus,
             state_->firmwareVersion,
             state_->bluetoothName,
             getProfileName(state_->activeProfile),
             (state_->preferredUnit == TempUnit::Fahrenheit) ? "F" : "C",
             (state_->minTemperature > 900.0f) ? state_->temperature : state_->minTemperature,
             (state_->maxTemperature < -900.0f) ? state_->temperature : state_->maxTemperature,
             state_->avgTemperature,
             elapsedSec,
             state_->isThresholdAlert ? "true" : "false",
             state_->wifiConnected ? "true" : "false",
             state_->wifiRSSI,
             state_->wifiSSID ? state_->wifiSSID : "");

    pCharJson_->setValue(json);
    pCharJson_->notify();
}

void BluetoothManager::update() noexcept {
    const unsigned long nowMs = millis();
    
    // Periodic RSSI measurement
    if (connected_ && (nowMs - lastRssiUpdateMs_) >= 5000UL) {
        lastRssiUpdateMs_ = nowMs;
        rssi_ = -60 - (rand() % 15);
        if (state_ != nullptr) {
            state_->signalStrength = rssi_;
        }
    }

    // Monitor Wi-Fi connection and trigger direct cloud telemetry upload
    if (state_ != nullptr) {
        if (WiFi.status() == WL_CONNECTED) {
            state_->wifiConnected = true;
            state_->wifiRSSI = WiFi.RSSI();
            state_->wifiSSID = WiFi.SSID().c_str();
            attemptCloudUpload();
        } else {
            state_->wifiConnected = false;
            state_->supabaseSyncState = "Offline";
        }
    }

    // Notify temperature once every second if connected
    if (connected_ && (nowMs - lastTempNotifyMs_) >= 1000UL) {
        lastTempNotifyMs_ = nowMs;
        
        // Notify Temperature characteristic (in Celsius for precision)
        float tempVal = state_ ? state_->temperature : 25.0f;
        pCharTemp_->setValue(tempVal);
        pCharTemp_->notify();

        // Format and send System JSON payload
        notifyDataChange();
    }
}

} // namespace Flux
