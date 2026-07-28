#include "IconLibrary.hpp"

namespace NaTrack::IconLibrary {

void drawBattery(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t w, uint8_t h, uint8_t percent, bool charging, uint16_t color) noexcept {
    // Battery body outline
    display.drawRect(x, y, w - 2, h, color);
    // Battery tip (positive terminal)
    display.fillRect(x + w - 2, y + h / 4, 2, h / 2, color);
    
    // Draw inside level
    if (percent > 0) {
        if (percent > 100) percent = 100;
        // Inner width is w - 6 max
        uint8_t maxInnerW = w - 6;
        uint8_t fillW = (maxInnerW * percent) / 100;
        if (fillW == 0 && percent > 0) fillW = 1;
        display.fillRect(x + 2, y + 2, fillW, h - 4, color);
    }

    // Charging indicator (lightning bolt outline overlay)
    if (charging) {
        int16_t cx = x + (w - 2) / 2;
        int16_t cy = y + h / 2;
        display.drawLine(cx, cy - h/2 + 2, cx - 2, cy + 1, SSD1306_INVERSE);
        display.drawLine(cx - 2, cy + 1, cx + 2, cy - 1, SSD1306_INVERSE);
        display.drawLine(cx + 2, cy - 1, cx, cy + h/2 - 2, SSD1306_INVERSE);
    }
}

void drawBluetooth(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, bool connected, bool blinkState, uint16_t color) noexcept {
    // Blink when disconnected
    if (!connected && !blinkState) {
        return;
    }

    int16_t w = h / 3;
    int16_t cx = x + w;
    int16_t cy = y + h / 2;

    // Classic Bluetooth outline rune
    display.drawLine(cx - w, cy - h/4, cx + w, cy + h/4, color);
    display.drawLine(cx + w, cy + h/4, cx, cy + h/2, color);
    display.drawLine(cx, cy + h/2, cx, cy - h/2, color);
    display.drawLine(cx, cy - h/2, cx + w, cy - h/4, color);
    display.drawLine(cx + w, cy - h/4, cx - w, cy + h/4, color);
}

void drawThermometer(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t h, uint16_t color) noexcept {
    uint8_t bulbRadius = h / 4;
    int16_t bulbX = x + bulbRadius;
    int16_t bulbY = y + h - bulbRadius;
    
    // Bottom bulb
    display.drawCircle(bulbX, bulbY, bulbRadius, color);
    display.fillCircle(bulbX, bulbY, bulbRadius - 2, color);

    // Stem
    int16_t stemW = bulbRadius;
    int16_t stemX = bulbX - stemW / 2;
    int16_t stemY = y;
    int16_t stemH = h - bulbRadius * 2 + 1;
    display.drawRect(stemX, stemY, stemW + 1, stemH, color);

    // Fill stem slightly to mock thermometer level
    display.fillRect(bulbX - 1, stemY + 3, 3, stemH - 2, color);
}

void drawDroplet(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t w, uint8_t h, bool filled, uint16_t color) noexcept {
    int16_t cx = x + w / 2;
    int16_t cy = y + h - w / 2;
    int16_t r = w / 2;

    if (filled) {
        // Render filled droplet with progressive triangles and circles
        for (int i = 0; i <= r; i++) {
            display.drawCircleHelper(cx, cy, i, 12, color); // quadrants 3 & 4
        }
        for (int16_t dy = 0; dy <= h - r; dy++) {
            int16_t curW = (r * dy) / (h - r);
            display.drawFastHLine(cx - curW, y + dy, curW * 2 + 1, color);
        }
    } else {
        // Outline
        display.drawCircleHelper(cx, cy, r, 12, color); // quadrants 3 & 4
        display.drawLine(cx, y, x, cy, color);
        display.drawLine(cx, y, x + w, cy, color);
    }
}

void drawSettings(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept {
    int16_t cx = x + size / 2;
    int16_t cy = y + size / 2;
    int16_t r = size / 3;

    // Draw central hub
    display.drawCircle(cx, cy, r, color);
    display.drawCircle(cx, cy, r - 2, color);

    // Draw teeth/spokes
    for (int i = 0; i < 8; i++) {
        float angle = i * 45.0f * 3.14159f / 180.0f;
        int16_t x1 = cx + (r * cos(angle));
        int16_t y1 = cy + (r * sin(angle));
        int16_t x2 = cx + ((r + 3) * cos(angle));
        int16_t y2 = cy + ((r + 3) * sin(angle));
        display.drawLine(x1, y1, x2, y2, color);
    }
}

void drawHistory(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept {
    int16_t cx = x + size / 2;
    int16_t cy = y + size / 2;
    int16_t r = size / 2 - 1;

    // Circle
    display.drawCircle(cx, cy, r, color);
    
    // Clock hands
    display.drawLine(cx, cy, cx, cy - r + 3, color);
    display.drawLine(cx, cy, cx + r - 3, cy, color);

    // Arrowhead for clock/loop history feeling
    display.drawLine(cx - 2, cy - r + 2, cx, cy - r, color);
    display.drawLine(cx - 2, cy - r - 2, cx, cy - r, color);
}

void drawWarning(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept {
    int16_t cx = x + size / 2;
    
    // Triangle
    display.drawLine(cx, y, x, y + size, color);
    display.drawLine(cx, y, x + size, y + size, color);
    display.drawLine(x, y + size, x + size, y + size, color);

    // Exclamation point
    display.drawFastVLine(cx, y + 4, size - 8, color);
    display.drawPixel(cx, y + size - 2, color);
}

void drawSuccess(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, uint16_t color) noexcept {
    // Checkmark inside boundary
    display.drawLine(x + 2, y + size/2 + 1, x + size/3 + 1, y + size - 3, color);
    display.drawLine(x + size/3 + 1, y + size - 3, x + size - 2, y + 2, color);
}

void drawArrow(Adafruit_SSD1306& display, int16_t x, int16_t y, uint8_t size, bool up, uint16_t color) noexcept {
    int16_t cx = x + size / 2;
    if (up) {
        display.drawLine(cx, y, x, y + size/2, color);
        display.drawLine(cx, y, x + size, y + size/2, color);
        display.drawLine(cx, y, cx, y + size, color);
    } else {
        display.drawLine(cx, y + size, x, y + size/2, color);
        display.drawLine(cx, y + size, x + size, y + size/2, color);
        display.drawLine(cx, y, cx, y + size, color);
    }
}

} // namespace NaTrack::IconLibrary
