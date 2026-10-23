#include "IconLibrary.hpp"

namespace Flux::IconLibrary {

namespace {

// 12x14 Header Flame
static const uint8_t epd_bitmap_flame_header[] PROGMEM = {
    0x0C, 0x00, 0x1E, 0x00, 0x3F, 0x00, 0x3F, 0x00, 0x7F, 0x80, 0x7F, 0x80, 
    0xCF, 0xC0, 0xC7, 0xC0, 0xC7, 0xC0, 0xEF, 0xC0, 0x7F, 0x80, 0x7F, 0x80, 
    0x3F, 0x00, 0x1E, 0x00
};

// 18x26 Big Thermometer
static const uint8_t epd_bitmap_big_thermo[] PROGMEM = {
    0x07, 0xE0, 0x00, 0x1F, 0xF8, 0x00, 0x38, 0x1C, 0x00, 0x30, 0x0C, 0x00, 
    0x30, 0x0C, 0x00, 0x30, 0x0C, 0x00, 0x33, 0xCC, 0x00, 0x33, 0xCC, 0x00, 
    0x33, 0xCC, 0x00, 0x33, 0xCC, 0x00, 0x33, 0xCC, 0x00, 0x33, 0xCC, 0x00, 
    0x33, 0xCC, 0x00, 0x73, 0xCE, 0x00, 0x63, 0xC6, 0x00, 0x67, 0xE6, 0x00, 
    0xCF, 0xF3, 0x00, 0xDF, 0xFB, 0x00, 0xDF, 0xFB, 0x00, 0xDF, 0xFB, 0x00, 
    0xCF, 0xF3, 0x00, 0x67, 0xE6, 0x00, 0x71, 0x8E, 0x00, 0x38, 0x1C, 0x00, 
    0x1F, 0xF8, 0x00, 0x07, 0xE0, 0x00
};

// 22x22 House Thermometer (Environment)
static const uint8_t epd_bitmap_house_thermo[] PROGMEM = {
    0x00, 0x30, 0x00, 0x00, 0xFC, 0x00, 0x03, 0x33, 0x00, 0x0C, 0x30, 0xC0, 
    0x30, 0x30, 0x30, 0xF3, 0xFF, 0x3C, 0xC3, 0x03, 0x0C, 0xC3, 0x03, 0x0C, 
    0xC3, 0x33, 0x0C, 0xC3, 0x33, 0x0C, 0xC3, 0x33, 0x0C, 0xC3, 0x33, 0x0C, 
    0xC3, 0x33, 0x0C, 0xC7, 0x33, 0x8C, 0xC6, 0x79, 0x8C, 0xC6, 0xFD, 0x8C, 
    0xC6, 0xFD, 0x8C, 0xC6, 0x79, 0x8C, 0xC7, 0x03, 0x8C, 0xC3, 0xFF, 0x0C, 
    0xC0, 0x00, 0x0C, 0xFF, 0xFF, 0xFC
};

// 18x22 Droplet Thermometer (Liquid)
static const uint8_t epd_bitmap_droplet_thermo[] PROGMEM = {
    0x00, 0xC0, 0x00, 0x01, 0xE0, 0x00, 0x03, 0xF0, 0x00, 0x06, 0x18, 0x00, 
    0x0C, 0x0C, 0x00, 0x0C, 0x0C, 0x00, 0x18, 0xC6, 0x00, 0x31, 0xE3, 0x00, 
    0x31, 0xE3, 0x00, 0x61, 0xE1, 0x80, 0x61, 0xE1, 0x80, 0xC1, 0xE0, 0xC0, 
    0xC3, 0xF0, 0xC0, 0xC7, 0xF8, 0xC0, 0xC7, 0xF8, 0xC0, 0xC7, 0xF8, 0xC0, 
    0x63, 0xF1, 0x80, 0x61, 0xE1, 0x80, 0x30, 0x03, 0x00, 0x1C, 0x0E, 0x00, 
    0x07, 0xF8, 0x00, 0x01, 0xE0, 0x00
};

// 20x22 Fermentation Carboy (Fermentation)
static const uint8_t epd_bitmap_ferm_carboy[] PROGMEM = {
    0x03, 0xF0, 0x00, 0x03, 0xF0, 0x00, 0x03, 0x30, 0x00, 0x0E, 0x1C, 0x00, 
    0x3C, 0x0F, 0x00, 0x71, 0xE3, 0x80, 0xC3, 0xF0, 0xC0, 0xC3, 0xF0, 0xC0, 
    0xDB, 0x30, 0xC0, 0xDB, 0x30, 0xC0, 0xC3, 0x30, 0xC0, 0xC3, 0x30, 0xC0, 
    0xDB, 0x30, 0xC0, 0xC3, 0x30, 0xC0, 0xC7, 0x38, 0xC0, 0xC6, 0xD8, 0xC0, 
    0xC5, 0xE8, 0xC0, 0xC5, 0xE8, 0xC0, 0xC6, 0xD8, 0xC0, 0xC3, 0xF0, 0xC0, 
    0x60, 0x01, 0x80, 0x3F, 0xFF, 0x00
};

// 22x22 Microchip (Device Info)
static const uint8_t epd_bitmap_microchip[] PROGMEM = {
    0x22, 0x22, 0x20, 0x22, 0x22, 0x20, 0x7F, 0xFF, 0xF0, 0x7F, 0xFF, 0xF0, 
    0xC0, 0x00, 0x1C, 0xC0, 0x00, 0x1C, 0x8F, 0xFF, 0x8C, 0x8F, 0xFF, 0x8C, 
    0xC8, 0x00, 0x9C, 0xC8, 0x00, 0x9C, 0x88, 0xF8, 0x8C, 0x88, 0xF8, 0x8C, 
    0xC8, 0x00, 0x9C, 0xC8, 0x00, 0x9C, 0x8F, 0xFF, 0x8C, 0x8F, 0xFF, 0x8C, 
    0xC0, 0x00, 0x1C, 0xC0, 0x00, 0x1C, 0x7F, 0xFF, 0xF0, 0x7F, 0xFF, 0xF0, 
    0x22, 0x22, 0x20, 0x22, 0x22, 0x20
};

// 24x20 Big WiFi
static const uint8_t epd_bitmap_big_wifi[] PROGMEM = {
    0x01, 0xFF, 0x80, 0x0F, 0xFF, 0xF0, 0x3F, 0xFF, 0xFC, 0x7E, 0x00, 0x7E, 
    0xF8, 0x00, 0x1F, 0xF0, 0x00, 0x0F, 0x20, 0xFF, 0x04, 0x07, 0xFF, 0xE0, 
    0x1F, 0x81, 0xF8, 0x3E, 0x00, 0x7C, 0x00, 0x7E, 0x00, 0x03, 0xFF, 0xC0, 
    0x0F, 0x00, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x3C, 0x00, 0x00, 0xFF, 0x00, 
    0x00, 0xFF, 0x00, 0x00, 0x7E, 0x00, 0x00, 0x18, 0x00, 0x00, 0x3C, 0x00
};

// 16x24 Big Bluetooth
static const uint8_t epd_bitmap_big_bt[] PROGMEM = {
    0x01, 0x80, 0x01, 0xC0, 0x01, 0xE0, 0x01, 0xF0, 0x31, 0xF8, 0x39, 0xFC, 
    0x1D, 0xDC, 0x0F, 0xCC, 0x07, 0x8C, 0x03, 0x0C, 0x07, 0x8C, 0x0F, 0xCC, 
    0x1D, 0xDC, 0x39, 0xFC, 0x31, 0xF8, 0x01, 0xF0, 0x01, 0xE0, 0x01, 0xC0, 
    0x01, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

// 8x8 Small Bars
static const uint8_t epd_bitmap_small_bars[] PROGMEM = {
    0x03, 0x03, 0x1B, 0x1B, 0xDB, 0xDB, 0xDB, 0xDB
};

// 8x8 Small House
static const uint8_t epd_bitmap_small_house[] PROGMEM = {
    0x18, 0x3C, 0x7E, 0xFF, 0x66, 0x66, 0x66, 0x7E
};

// 8x8 Small Droplet
static const uint8_t epd_bitmap_small_drop[] PROGMEM = {
    0x18, 0x3C, 0x7E, 0xFF, 0xFF, 0xFF, 0x7E, 0x3C
};

// 8x8 Small Flask
static const uint8_t epd_bitmap_small_flask[] PROGMEM = {
    0x3C, 0x18, 0x18, 0x3C, 0x7E, 0x66, 0xFF, 0x7E
};

// 8x8 Small WiFi
static const uint8_t epd_bitmap_small_wifi[] PROGMEM = {
    0x7E, 0xC3, 0x3C, 0x42, 0x18, 0x00, 0x18, 0x18
};

// 8x8 Small Bluetooth
static const uint8_t epd_bitmap_small_bt[] PROGMEM = {
    0x18, 0x1C, 0x5E, 0x3E, 0x1C, 0x3E, 0x5E, 0x1C
};

// 8x8 Small Gear
static const uint8_t epd_bitmap_small_gear[] PROGMEM = {
    0x3C, 0x66, 0xE7, 0xC3, 0xC3, 0xE7, 0x66, 0x3C
};

// 8x8 Small Info
static const uint8_t epd_bitmap_small_info[] PROGMEM = {
    0x3C, 0x66, 0x6E, 0xE6, 0xEE, 0x6E, 0x66, 0x3C
};

// 4x7 Small Chevron
static const uint8_t epd_bitmap_small_chevron[] PROGMEM = {
    0x80, 0x40, 0x20, 0x10, 0x20, 0x40, 0x80
};

} // namespace

void drawFlame(Adafruit_SSD1306& display, int16_t cx, int16_t cy, uint8_t size, uint16_t color) noexcept {
    display.fillTriangle(cx, cy - size, cx - (size * 6) / 10, cy + size / 3, cx + (size * 6) / 10, cy + size / 3, color);
    display.fillCircle(cx, cy + size / 3, (size * 55) / 100, color);
    
    uint8_t inSize = (size * 5) / 10;
    if (inSize >= 2) {
        display.fillTriangle(cx, cy - inSize + 2, cx - inSize / 2, cy + inSize / 2, cx + inSize / 2, cy + inSize / 2, SSD1306_BLACK);
        display.fillCircle(cx, cy + inSize / 2, inSize / 2, SSD1306_BLACK);
    }
}

void drawFlameHeader(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_flame_header, 12, 14, color);
}

void drawBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, bool connected, bool blinkState, uint16_t color) noexcept {
    int16_t w = h / 3;
    int16_t cx = x + w;
    int16_t cy = y + h / 2;

    // Classic Bluetooth outline rune
    display.drawLine(cx - w, cy - h/4, cx + w, cy + h/4, color);
    display.drawLine(cx + w, cy + h/4, cx, cy + h/2, color);
    display.drawLine(cx, cy + h/2, cx, cy - h/2, color);
    display.drawLine(cx, cy - h/2, cx + w, cy - h/4, color);
    display.drawLine(cx + w, cy - h/4, cx - w, cy + h/4, color);

    // If disconnected, draw a small distinct 'x' next to the icon
    if (!connected) {
        int16_t crossX = x + w * 2 + 3;
        int16_t crossY = cy - 2;
        display.drawLine(crossX, crossY, crossX + 3, crossY + 3, color);
        display.drawLine(crossX, crossY + 3, crossX + 3, crossY, color);
    }
}

void drawWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, bool connected, bool blinkState, uint16_t color) noexcept {
    int16_t cx = x + size / 2;
    int16_t cy = y + size - 2;

    // Center bottom dot
    display.drawPixel(cx, cy, color);

    // Inner wave
    display.drawPixel(cx - 2, cy - 2, color);
    display.drawPixel(cx - 1, cy - 3, color);
    display.drawPixel(cx,     cy - 3, color);
    display.drawPixel(cx + 1, cy - 3, color);
    display.drawPixel(cx + 2, cy - 2, color);

    // Outer wave
    display.drawPixel(cx - 4, cy - 5, color);
    display.drawPixel(cx - 3, cy - 6, color);
    display.drawPixel(cx - 2, cy - 7, color);
    display.drawPixel(cx - 1, cy - 7, color);
    display.drawPixel(cx,     cy - 7, color);
    display.drawPixel(cx + 1, cy - 7, color);
    display.drawPixel(cx + 2, cy - 7, color);
    display.drawPixel(cx + 3, cy - 6, color);
    display.drawPixel(cx + 4, cy - 5, color);

    // If disconnected, draw a small distinct 'x' next to the icon
    if (!connected) {
        int16_t crossX = cx + 6;
        int16_t crossY = y + 2;
        display.drawLine(crossX, crossY, crossX + 3, crossY + 3, color);
        display.drawLine(crossX, crossY + 3, crossX + 3, crossY, color);
    }
}

void drawBigThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_big_thermo, 18, 26, color);
}

void drawHouseThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_house_thermo, 22, 22, color);
}

void drawDropletThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_droplet_thermo, 18, 22, color);
}

void drawFermenterBottle(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_ferm_carboy, 20, 22, color);
}

void drawMicrochip(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_microchip, 22, 22, color);
}

void drawBigWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_big_wifi, 24, 20, color);
}

void drawBigBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_big_bt, 16, 24, color);
}

void drawSmallBars(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_bars, 8, 8, color);
}

void drawSmallHouse(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_house, 8, 8, color);
}

void drawSmallDroplet(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_drop, 8, 8, color);
}

void drawSmallFlask(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_flask, 8, 8, color);
}

void drawSmallWifi(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_wifi, 8, 8, color);
}

void drawSmallBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_bt, 8, 8, color);
}

void drawSmallGear(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_gear, 8, 8, color);
}

void drawSmallInfo(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_info, 8, 8, color);
}

void drawChevronRight(Adafruit_SSD1306& display, int16_t x, int16_t y, uint16_t color) noexcept {
    display.drawBitmap(x, y, epd_bitmap_small_chevron, 4, 7, color);
}

void drawBottomPill(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h,
                    const char* statusText, const char* timeStr, uint16_t color) noexcept {
    display.drawRoundRect(x, y, w, h, 3, color);
    
    // Status dot
    display.fillCircle(x + 9, y + h / 2, 2, color);
    
    // Status text
    display.setTextSize(1);
    display.setTextColor(color);
    display.setCursor(x + 16, y + 3);
    display.print(statusText);
    
    // Vertical divider line
    display.drawFastVLine(x + 58, y + 2, h - 4, color);
    
    // Time text
    display.setCursor(x + 64, y + 3);
    display.print(timeStr);
}

} // namespace Flux::IconLibrary
