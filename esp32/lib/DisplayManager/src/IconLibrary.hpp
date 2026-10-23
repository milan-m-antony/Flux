#pragma once

#include <Arduino.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

namespace Flux::IconLibrary {

// Header Icons
void drawFlame(Adafruit_SSD1306& display, int16_t cx, int16_t cy, uint8_t size, uint16_t color) noexcept;
void drawFlameHeader(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, bool connected, bool blinkState, uint16_t color) noexcept;
void drawWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, bool connected, bool blinkState, uint16_t color) noexcept;

// Large Feature & Profile Icons
void drawBigThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawHouseThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawDropletThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawFermenterBottle(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawMicrochip(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawBigWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;
void drawBigBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color = SSD1306_WHITE) noexcept;

// Small Menu Row Icons (8x8)
void drawSmallBars(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallHouse(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallDroplet(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallFlask(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallGear(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawSmallInfo(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;
void drawChevronRight(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept;

// Bottom Capsule Pill
void drawBottomPill(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h,
                    const char* statusText, const char* timeStr, uint16_t color) noexcept;

} // namespace Flux::IconLibrary
