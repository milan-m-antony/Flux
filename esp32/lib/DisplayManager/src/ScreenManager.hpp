#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "DeviceState.hpp"
#include "Widgets.hpp"

namespace NaTrack {

class ScreenManager {
public:
    void drawScreen(Adafruit_SSD1306& display, ScreenId screen, int16_t offsetX,
                    const DeviceState& state, int settingsIndex) noexcept;

private:
    void drawHomeScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawSodiumScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawTemperatureScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawBatteryScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawBluetoothScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, int settingsIndex) noexcept;
    void drawAboutScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
};

} // namespace NaTrack
