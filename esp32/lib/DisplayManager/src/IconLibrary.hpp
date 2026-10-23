#pragma once

#include <Arduino.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

namespace Flux::IconLibrary {

void drawBattery(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t w, uint8_t h, uint8_t percent, bool charging, uint16_t color) noexcept;
void drawBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, bool connected, bool blinkState, uint16_t color) noexcept;
void drawWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, bool connected, bool blinkState, uint16_t color) noexcept;
void drawThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, uint16_t color) noexcept;
void drawDroplet(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t w, uint8_t h, bool filled, uint16_t color) noexcept;
void drawSettings(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept;
void drawHistory(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept;
void drawWarning(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept;
void drawSuccess(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept;
void drawArrow(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, bool up, uint16_t color) noexcept;
void drawClock(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept;
void drawFlame(Adafruit_SSD1306& display, int16_t cx, int16_t cy, uint8_t size, uint16_t color) noexcept;

} // namespace Flux::IconLibrary
