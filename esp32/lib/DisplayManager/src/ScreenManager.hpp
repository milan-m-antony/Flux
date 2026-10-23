#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "DeviceState.hpp"
#include "Widgets.hpp"

namespace Flux {

class ScreenManager {
public:
    void drawScreen(Adafruit_SSD1306& display, ScreenId screen, int16_t offsetX,
                    const DeviceState& state, int settingsIndex) noexcept;

private:
    void drawMainMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawModeSelection(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int selectionIndex) noexcept;
    void drawSettings(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept;
    void drawDeviceInfo(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawWifiStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
    void drawBluetoothStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept;
};

} // namespace Flux
