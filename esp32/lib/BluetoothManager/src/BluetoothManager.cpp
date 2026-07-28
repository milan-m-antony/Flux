#include "BluetoothManager.hpp"

namespace NaTrack {

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

void BluetoothManager::begin(DeviceState* state) noexcept {
    state_ = state;
    connected_ = false;
    rssi_ = -65;
    lastRssiUpdateMs_ = millis();
    lastTempNotifyMs_ = millis();

    if (state_ != nullptr) {
        state_->bluetoothConnected = false;
        state_->bluetoothEnabled = true;
    }

    try {
        // Initialize BLE Device
        BLEDevice::init("NaTrack");
        
        // CRITICAL: Set ESP32 MTU to 512 so it can send strings longer than 20 bytes!
        BLEDevice::setMTU(512);

        // Create BLE Server
        pServer_ = BLEDevice::createServer();
        pServer_->setCallbacks(this);

        // Create BLE Service with 35 handles to accommodate 8 characteristics
        pService_ = pServer_->createService(BLEUUID(std::string(SERVICE_UUID)), 35);

        // Helper to add standard read/notify characteristics with BLE2902 descriptors
        auto addChar = [this](const char* uuid) -> BLECharacteristic* {
            BLECharacteristic* pChar = pService_->createCharacteristic(
                uuid,
                BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
            );
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
        pCharJson_ = addChar(JSON_CHAR_UUID);

        // Set static characteristics
        if (state_ != nullptr) {
            pCharFirmware_->setValue(state_->firmwareVersion);
            pCharName_->setValue(state_->bluetoothName);
        } else {
            pCharFirmware_->setValue("1.0.0");
            pCharName_->setValue("NaTrack");
        }

        // Start Service
        pService_->start();

        // Start Advertising
        BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
        pAdvertising->addServiceUUID(SERVICE_UUID);
        pAdvertising->setScanResponse(true);
        pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issues
        pAdvertising->setMinPreferred(0x12);
        BLEDevice::startAdvertising();

        Serial.println("BLE Initialized");
        Serial.println("Advertising Started");
    } catch (...) {
        Serial.println("BLE Init Failed");
    }
}

void BluetoothManager::onConnect(BLEServer* pServer) {
    connected_ = true;
    if (state_ != nullptr) {
        state_->bluetoothConnected = true;
        state_->connectedDeviceName = "Android Client";
        state_->lastConnectionTime = millis();
    }
    Serial.println("Client Connected");
}

void BluetoothManager::onDisconnect(BLEServer* pServer) {
    connected_ = false;
    if (state_ != nullptr) {
        state_->bluetoothConnected = false;
        state_->connectedDeviceName = "";
    }
    Serial.println("Client Disconnected");
    
    // Restart advertising immediately
    BLEDevice::startAdvertising();
    Serial.println("Advertising Started");
}

void BluetoothManager::onWrite(BLECharacteristic* pCharacteristic) {
    // Standard handler if Android app writes (reserved/future-ready)
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
        Serial.println("Notification Sent");
    }
    if (abs(state_->batteryVoltage - lastNotifiedVoltage_) > 0.01f) {
        lastNotifiedVoltage_ = state_->batteryVoltage;
        char buf[16];
        dtostrf(lastNotifiedVoltage_, 4, 2, buf);
        pCharVoltage_->setValue(buf);
        pCharVoltage_->notify();
        Serial.println("Notification Sent");
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
        Serial.println("Notification Sent");
    }
    if (strcmp(state_->sodiumStatus, lastNotifiedStatus_) != 0) {
        lastNotifiedStatus_ = state_->sodiumStatus;
        pCharStatus_->setValue(const_cast<char*>(lastNotifiedStatus_));
        pCharStatus_->notify();
        Serial.println("Notification Sent");
    }

    // 2. Format and send System JSON payload via 6E400009 characteristic
    // Construct manual JSON payload to avoid large library overheads
    char json[192];
    snprintf(json, sizeof(json),
             "{\"temperature\":%.1f,\"battery\":%d,\"batteryVoltage\":%.2f,\"charging\":%s,\"sodium\":\"%s\",\"status\":\"%s\",\"firmware\":\"%s\",\"device\":\"%s\"}",
             state_->temperature,
             state_->batteryPercentage,
             state_->batteryVoltage,
             state_->charging ? "true" : "false",
             state_->sodiumValue == -1 ? "---" : String(state_->sodiumValue).c_str(),
             state_->sodiumStatus,
             state_->firmwareVersion,
             state_->bluetoothName);

    pCharJson_->setValue(json);
    pCharJson_->notify();
}

void BluetoothManager::update() noexcept {
    const unsigned long nowMs = millis();
    
    // Simulate RSSI fluctuate when connected
    if (connected_ && (nowMs - lastRssiUpdateMs_) >= 5000UL) {
        lastRssiUpdateMs_ = nowMs;
        rssi_ = -60 - (rand() % 15);
        if (state_ != nullptr) {
            state_->signalStrength = rssi_;
        }
    }

    // Notify temperature once every second if connected
    if (connected_ && (nowMs - lastTempNotifyMs_) >= 1000UL) {
        lastTempNotifyMs_ = nowMs;
        
        // Notify Temperature
        float tempVal = state_ ? state_->temperature : 25.0f;
        pCharTemp_->setValue(tempVal);
        pCharTemp_->notify();
        Serial.printf("Temperature Notification Sent: %.1f\n", tempVal);

        // Format and send System JSON payload
        notifyDataChange();
    }
}

} // namespace NaTrack
