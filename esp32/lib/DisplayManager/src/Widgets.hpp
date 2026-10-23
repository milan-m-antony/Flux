#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "IconLibrary.hpp"
#include "DeviceState.hpp"

namespace Flux {

class HeaderWidget {
public:
    static void draw(Adafruit_SSD1306& display, int batteryPercent, bool isCharging, bool isBluetoothConnected, bool blinkState) noexcept;
};

class FooterWidget {
public:
    static void draw(Adafruit_SSD1306& display, const DeviceState& state) noexcept;
};

class CardWidget {
public:
    static void draw(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h, uint16_t color) noexcept;
};

class StatusWidget {
public:
    static void draw(Adafruit_SSD1306& display, int16_t x, int16_t y, const char* statusText, bool inverted) noexcept;
};

class ValueWidget {
public:
    static void drawLarge(Adafruit_SSD1306& display, int16_t x, int16_t y, const char* val, const char* unit) noexcept;
};

} // namespace Flux
