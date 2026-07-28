#pragma once

#include <Arduino.h>

namespace NaTrack {

enum class ScreenId : uint8_t {
    Home = 0,
    Sodium,
    Temperature,
    Battery,
    Bluetooth,
    Settings,
    About,
    Count
};

struct DeviceState {
    float temperature = 0.0f;
    int batteryPercentage = 87;
    float batteryVoltage = 4.02f;
    bool charging = false;
    bool bluetoothConnected = false;
    bool bluetoothEnabled = true;
    const char* connectedDeviceName = "";
    unsigned long lastConnectionTime = 0UL;
    int signalStrength = -65;
    const char* bluetoothName = "NaTrack";
    int sodiumValue = -1; // -1 represents unset/waiting
    const char* sodiumStatus = "Waiting For Sensor";
    ScreenId activeScreen = ScreenId::Home;
    ScreenId previousScreen = ScreenId::Home;
    bool bootCompleted = false;
    const char* firmwareVersion = "v1.0.0";
    const char* deviceName = "NaTrack Prototype v1";
};

} // namespace NaTrack
