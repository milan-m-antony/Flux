#pragma once

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "DeviceState.hpp"

namespace Flux {

class BluetoothManager : public BLEServerCallbacks, public BLECharacteristicCallbacks {
public:
    void begin(DeviceState* state) noexcept;
    void update() noexcept;

    // Server Callback overrides
    void onConnect(BLEServer* pServer) override;
    void onDisconnect(BLEServer* pServer) override;

    // Characteristic Callback overrides
    void onWrite(BLECharacteristic* pCharacteristic) override;

    bool isConnected() const noexcept {
        return connected_;
    }

    const char* getDeviceName() const noexcept {
        return "Flux-Telemetry";
    }

    int16_t getRSSI() const noexcept {
        return rssi_;
    }

private:
    void notifyDataChange() noexcept;

    DeviceState* state_ = nullptr;
    bool connected_ = false;
    int16_t rssi_ = -65;
    unsigned long lastRssiUpdateMs_ = 0UL;
    unsigned long lastTempNotifyMs_ = 0UL;

    BLEServer* pServer_ = nullptr;
    BLEService* pService_ = nullptr;

    // Characteristics
    BLECharacteristic* pCharTemp_ = nullptr;
    BLECharacteristic* pCharBattery_ = nullptr;
    BLECharacteristic* pCharVoltage_ = nullptr;
    BLECharacteristic* pCharSodium_ = nullptr;
    BLECharacteristic* pCharStatus_ = nullptr;
    BLECharacteristic* pCharFirmware_ = nullptr;
    BLECharacteristic* pCharName_ = nullptr;
    BLECharacteristic* pCharJson_ = nullptr;

    // Cached variables for notify change check
    float lastNotifiedTemp_ = -999.0f;
    int lastNotifiedBattery_ = -1;
    float lastNotifiedVoltage_ = -999.0f;
    int lastNotifiedSodium_ = -1;
    const char* lastNotifiedStatus_ = "";
    bool lastNotifiedCharging_ = false;
};

} // namespace Flux
