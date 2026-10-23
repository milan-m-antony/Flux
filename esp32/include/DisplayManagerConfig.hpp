#pragma once

#include <Arduino.h>

namespace NaTrack::DisplayManagerConfig {

// OLED panel width in pixels.
constexpr int DisplayWidth = 128;

// OLED panel height in pixels.
constexpr int DisplayHeight = 64;

// I2C SDA pin used by the OLED module.
constexpr uint8_t I2CSdaPin = 21U;

// I2C SCL pin used by the OLED module.
constexpr uint8_t I2CSclPin = 22U;

// Primary OLED I2C address.
constexpr uint8_t PrimaryOledAddress = 0x3CU;

// Fallback OLED I2C address.
constexpr uint8_t SecondaryOledAddress = 0x3DU;

// Time to show the startup splash before transitioning to the home screen.
constexpr unsigned long StartupScreenDurationMs = 2000UL;

// Startup title shown on power-up.
constexpr const char* StartupTitle = "FLUX";

// Subtitle shown on power-up.
constexpr const char* StartupSubtitle = "Smart Telemetry System";

// Home screen header.
constexpr const char* HomeTitle = "Dashboard";

// Home screen placeholder values.
constexpr const char* TemperaturePlaceholder = "Temp: --.- C";
constexpr const char* ModePlaceholder = "Mode: Live";
constexpr const char* BatteryPlaceholder = "Battery: --";
constexpr const char* BluetoothPlaceholder = "BLE: Ready";

} // namespace NaTrack::DisplayManagerConfig