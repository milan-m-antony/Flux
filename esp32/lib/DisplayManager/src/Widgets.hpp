#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "IconLibrary.hpp"
#include "DeviceState.hpp"

namespace Flux {

class HeaderWidget {
public:
    static void draw(Adafruit_SSD1306& display, const DeviceState& state, bool blinkState) noexcept;
};

class CardWidget {
public:
    static void draw(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h, uint16_t color) noexcept;
};

} // namespace Flux
