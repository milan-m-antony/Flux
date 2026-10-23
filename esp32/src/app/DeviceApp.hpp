#pragma once

#include <Arduino.h>
#include "DeviceState.hpp"
#include <ButtonManager.hpp>
#include <BluetoothManager.hpp>
#include <DisplayManager.hpp>
#include <PowerManager.hpp>
#include <StatusReporter.hpp>
#include <SensorManager.hpp>
#include <BatteryManager.hpp>
#include <NavigationManager.hpp>
#include <SettingsManager.hpp>
#include <UIManager.hpp>

namespace Flux {

class DeviceApp {
public:
    void begin() noexcept;
    void update() noexcept;

private:
    DeviceState state_;
    
    StatusReporter* statusReporter_ = nullptr;
    ButtonManager* buttonManager_ = nullptr;
    BluetoothManager* bluetoothManager_ = nullptr;
    PowerManager* powerManager_ = nullptr;
    DisplayManager* displayManager_ = nullptr;
    SensorManager* sensorManager_ = nullptr;
    BatteryManager* batteryManager_ = nullptr;
    NavigationManager* navManager_ = nullptr;
    SettingsManager* settingsManager_ = nullptr;
    UIManager* uiManager_ = nullptr;
};

} // namespace Flux
