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
    void drawDashboard(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawLiveMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawEnvironment(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawLiquid(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawFermentation(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawStatisticsTimer(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawSystemStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept;
};

} // namespace NaTrack
