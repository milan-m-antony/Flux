#include "BluetoothManager.hpp"
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <nvs_flash.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

namespace Flux {

static WebServer s_apServer(80);

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
        // Initialize NVS Flash partition cleanly
        esp_err_t nvsErr = nvs_flash_init();
        if (nvsErr == ESP_ERR_NVS_NO_FREE_PAGES || nvsErr == ESP_ERR_NVS_NEW_VERSION_FOUND) {
            nvs_flash_erase();
            nvs_flash_init();
        }

        // Initialize BLE Device
        BLEDevice::init("Flux1");
        
        // CRITICAL: Set ESP32 MTU to 512 so it can send strings longer than 20 bytes
        BLEDevice::setMTU(512);

        // Create BLE Server
        pServer_ = BLEDevice::createServer();
        pServer_->setCallbacks(this);

        // Create BLE Service with 35 handles to accommodate characteristics & descriptors
        pService_ = pServer_->createService(BLEUUID(SERVICE_UUID), 35);

        // Helper to add standard read/notify/write characteristics with BLE2902 descriptors
        auto addChar = [this](const char* uuid, bool writable = false) -> BLECharacteristic* {
            uint32_t props = BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY;
            if (writable) {
                props |= BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR;
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

        // Start Advertising with explicit Device Name & UUID in both adv & scan response
        BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
        pAdvertising->addServiceUUID(SERVICE_UUID);

        BLEAdvertisementData advData;
        advData.setName("Flux1");
        advData.setFlags(0x06); // BR_EDR_NOT_SUPPORTED | GENERAL_DISC_MODE
        advData.setCompleteServices(BLEUUID(SERVICE_UUID));
        pAdvertising->setAdvertisementData(advData);

        BLEAdvertisementData scanRespData;
        scanRespData.setName("Flux1");
        pAdvertising->setScanResponseData(scanRespData);

        pAdvertising->setScanResponse(true);
        pAdvertising->setMinPreferred(0x06);
        pAdvertising->setMinPreferred(0x12);
        BLEDevice::startAdvertising();

        Serial.println("Flux BLE Initialized with explicit device name Flux1");
        Serial.println("Advertising Started");

        // Initialize Wi-Fi in dual AP+Station mode
        WiFi.mode(WIFI_AP_STA);

        // Start SoftAP setup server for fallback Wi-Fi configuration (port 80)
        startApProvisioningServer();

        // Load saved Wi-Fi credentials from NVS and initiate station connection
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
    String rawVal = pCharacteristic->getValue();
    if (rawVal.isEmpty()) return;
    std::string val(rawVal.c_str());

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
        std::string ssid = (comma != std::string::npos) ? param.substr(0, comma) : param;
        std::string pass = (comma != std::string::npos) ? param.substr(comma + 1) : "";

        // Trim any trailing \r, \n, spaces
        while (!ssid.empty() && (ssid.back() == '\r' || ssid.back() == '\n' || ssid.back() == ' ')) ssid.pop_back();
        while (!pass.empty() && (pass.back() == '\r' || pass.back() == '\n')) pass.pop_back();

        Serial.printf("Received Wi-Fi config over BLE: SSID='%s' pass_len=%d\n", ssid.c_str(), (int)pass.length());

        if (pass.empty()) {
            // Check if NVS already has a saved password for this SSID
            Preferences prefs;
            prefs.begin("flux_wifi", true);
            String savedSSID = prefs.getString("ssid", "");
            String savedPass = prefs.getString("pass", "");
            prefs.end();
            if (savedSSID == ssid.c_str() && savedPass.length() > 0) {
                pass = savedPass.c_str();
                Serial.printf("Preserving existing saved password for SSID='%s'\n", ssid.c_str());
            }
        }

        saveWifiCredentials(ssid.c_str(), pass.c_str());
        lastWifiRetryMs_ = millis();
        wifiConnecting_ = true;
        WiFi.persistent(true);
        WiFi.setAutoReconnect(true);
        WiFi.disconnect();
        delay(100);
        if (pass.empty()) {
            WiFi.begin(ssid.c_str());
        } else {
            WiFi.begin(ssid.c_str(), pass.c_str());
        }
        Serial.printf("Wi-Fi connection initiated for SSID='%s'\n", ssid.c_str());
    } else if (val.find("SET_BUZZER:1") != std::string::npos || val.find("BUZZER_ON") != std::string::npos || val.find("\"buzzer\":true") != std::string::npos) {
        state_->buzzerEnabled = true;
        Serial.println("Buzzer enabled via BLE");
    } else if (val.find("SET_BUZZER:0") != std::string::npos || val.find("BUZZER_OFF") != std::string::npos || val.find("\"buzzer\":false") != std::string::npos) {
        state_->buzzerEnabled = false;
        Serial.println("Buzzer disabled via BLE");
    } else if (val.find("BLE_OFF") != std::string::npos || val.find("STOP_BLE") != std::string::npos) {
        Serial.println("Stopping BLE Advertising upon request");
        BLEDevice::getAdvertising()->stop();
        if (pServer_ != nullptr && connected_) {
            pServer_->disconnect(pServer_->getConnId());
        }
        if (state_ != nullptr) {
            state_->bluetoothEnabled = false;
            state_->bluetoothConnected = false;
        }
    } else if (val.find("PING") != std::string::npos) {
        Serial.println("BLE PING received - peripheral responsive");
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
            lastWifiRetryMs_ = millis();
            wifiConnecting_ = true;
            WiFi.persistent(true);
            WiFi.setAutoReconnect(true);
            Serial.printf("Auto-reconnecting to saved Wi-Fi: '%s' (pass_len=%d)\n", savedSSID.c_str(), (int)savedPass.length());
            WiFi.disconnect();
            delay(50);
            if (savedPass.length() > 0) {
                WiFi.begin(savedSSID.c_str(), savedPass.c_str());
            } else {
                WiFi.begin(savedSSID.c_str());
            }
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
    char json[420];
    snprintf(json, sizeof(json),
             "{\"wifiConnected\":%s,\"wifiRssi\":%d,\"wifiSsid\":\"%s\",\"temperature\":%.1f,\"battery\":%d,\"batteryVoltage\":%.2f,\"charging\":%s,\"sodium\":\"%s\",\"status\":\"%s\",\"firmware\":\"%s\",\"device\":\"%s\",\"mode\":\"%s\",\"unit\":\"%s\",\"minTemp\":%.1f,\"maxTemp\":%.1f,\"avgTemp\":%.1f,\"elapsedSec\":%lu,\"alert\":%s,\"buzzer\":%s}",
             state_->wifiConnected ? "true" : "false",
             state_->wifiRSSI,
             state_->wifiSSID ? state_->wifiSSID : "",
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
             state_->buzzerEnabled ? "true" : "false");

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
        wl_status_t currentStatus = WiFi.status();
        if (currentStatus == WL_CONNECTED) {
            if (!state_->wifiConnected) {
                Serial.printf("Wi-Fi Connected! SSID: '%s', IP: %s, RSSI: %d dBm\n",
                              WiFi.SSID().c_str(), WiFi.localIP().toString().c_str(), WiFi.RSSI());
                state_->wifiConnected = true;
                state_->wifiRSSI = WiFi.RSSI();
                currentWifiSsid_ = WiFi.SSID();
                state_->wifiSSID = currentWifiSsid_.c_str();
                wifiConnecting_ = false;
                // Immediately push updated Wi-Fi state over BLE so phone doesn't wait
                notifyDataChange();
            } else {
                state_->wifiConnected = true;
                state_->wifiRSSI = WiFi.RSSI();
                currentWifiSsid_ = WiFi.SSID();
                state_->wifiSSID = currentWifiSsid_.c_str();
            }
            attemptCloudUpload();
        } else {
            if (state_->wifiConnected) {
                Serial.printf("Wi-Fi Disconnected (status=%d)\n", (int)currentStatus);
                state_->wifiConnected = false;
                state_->supabaseSyncState = "Offline";
                notifyDataChange();
            } else {
                state_->wifiConnected = false;
                state_->supabaseSyncState = "Offline";
            }

            // Periodic auto-reconnect retry every 35s if saved SSID exists
            if (currentWifiSsid_.length() > 0 && (nowMs - lastWifiRetryMs_) >= 35000UL) {
                lastWifiRetryMs_ = nowMs;
                Preferences prefs;
                prefs.begin("flux_wifi", true);
                String sSsid = prefs.getString("ssid", "");
                String sPass = prefs.getString("pass", "");
                prefs.end();
                if (sSsid.length() > 0) {
                    Serial.printf("Wi-Fi auto-reconnect retry to '%s' (status=%d)...\n", sSsid.c_str(), (int)currentStatus);
                    WiFi.disconnect();
                    delay(50);
                    if (sPass.length() > 0) {
                        WiFi.begin(sSsid.c_str(), sPass.c_str());
                    } else {
                        WiFi.begin(sSsid.c_str());
                    }
                }
            }
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

    // Process incoming HTTP Wi-Fi provisioning requests in AP mode
    handleApClient();
}

void BluetoothManager::startApProvisioningServer() noexcept {
    try {
        WiFi.mode(WIFI_AP_STA);
        WiFi.softAP("Flux-Setup");
        Serial.printf("SoftAP Provisioning Online: SSID='Flux-Setup' Gateway=%s\n",
                      WiFi.softAPIP().toString().c_str());

        // GET /status: Query ESP32 connection state
        s_apServer.on("/status", HTTP_GET, [this]() {
            s_apServer.sendHeader("Access-Control-Allow-Origin", "*");
            char buf[256];
            snprintf(buf, sizeof(buf),
                     "{\"status\":\"ok\",\"device\":\"Flux1\",\"wifiConnected\":%s,\"wifiSSID\":\"%s\",\"wifiRssi\":%d,\"bleEnabled\":%s}",
                     (state_ && state_->wifiConnected) ? "true" : "false",
                     (state_ && state_->wifiSSID) ? state_->wifiSSID : "",
                     (state_) ? state_->wifiRSSI : 0,
                     (state_ && state_->bluetoothEnabled) ? "true" : "false");
            s_apServer.send(200, "application/json", buf);
        });

        // POST /wifi: Configure Wi-Fi via SoftAP
        s_apServer.on("/wifi", HTTP_POST, [this]() {
            s_apServer.sendHeader("Access-Control-Allow-Origin", "*");
            String ssid = "";
            String pass = "";

            if (s_apServer.hasArg("plain")) {
                String body = s_apServer.arg("plain");
                int sIdx = body.indexOf("\"ssid\"");
                if (sIdx >= 0) {
                    int c = body.indexOf(':', sIdx);
                    int q1 = body.indexOf('"', c);
                    int q2 = body.indexOf('"', q1 + 1);
                    if (q1 >= 0 && q2 > q1) ssid = body.substring(q1 + 1, q2);
                }
                int pIdx = body.indexOf("\"pass\"");
                if (pIdx < 0) pIdx = body.indexOf("\"password\"");
                if (pIdx >= 0) {
                    int c = body.indexOf(':', pIdx);
                    int q1 = body.indexOf('"', c);
                    int q2 = body.indexOf('"', q1 + 1);
                    if (q1 >= 0 && q2 > q1) pass = body.substring(q1 + 1, q2);
                }
            }
            if (ssid.isEmpty() && s_apServer.hasArg("ssid")) {
                ssid = s_apServer.arg("ssid");
                pass = s_apServer.arg("pass");
            }

            if (ssid.length() > 0) {
                Serial.printf("SoftAP Provisioning: Setting SSID='%s'\n", ssid.c_str());
                saveWifiCredentials(ssid.c_str(), pass.c_str());
                lastWifiRetryMs_ = millis();
                wifiConnecting_ = true;
                WiFi.persistent(true);
                WiFi.setAutoReconnect(true);
                WiFi.disconnect();
                delay(50);
                if (pass.isEmpty()) {
                    WiFi.begin(ssid.c_str());
                } else {
                    WiFi.begin(ssid.c_str(), pass.c_str());
                }
                char resp[160];
                snprintf(resp, sizeof(resp), "{\"status\":\"connecting\",\"ssid\":\"%s\"}", ssid.c_str());
                s_apServer.send(200, "application/json", resp);
            } else {
                s_apServer.send(400, "application/json", "{\"error\":\"Missing Wi-Fi name\"}");
            }
        });

        // Preflight CORS and 404 handler
        s_apServer.onNotFound([this]() {
            s_apServer.sendHeader("Access-Control-Allow-Origin", "*");
            s_apServer.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            s_apServer.sendHeader("Access-Control-Allow-Headers", "Content-Type");
            if (s_apServer.method() == HTTP_OPTIONS) {
                s_apServer.send(204);
            } else {
                s_apServer.send(404, "text/plain", "Not Found");
            }
        });

        s_apServer.begin();
        apServerStarted_ = true;
        Serial.println("SoftAP HTTP Server listening on port 80");
    } catch (...) {
        Serial.println("Failed to start SoftAP HTTP Server");
    }
}

void BluetoothManager::handleApClient() noexcept {
    if (apServerStarted_) {
        s_apServer.handleClient();
    }
}

} // namespace Flux
