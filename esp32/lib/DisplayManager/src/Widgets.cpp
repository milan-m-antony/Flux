#include "Widgets.hpp"

namespace NaTrack {

void HeaderWidget::draw(Adafruit_SSD1306& display, int batteryPercent, bool isCharging, bool isBluetoothConnected, bool blinkState) noexcept {
    // Top Bar is Y=0 to Y=11.
    // Battery icon top left
    IconLibrary::drawBattery(display, 2, 2, 14, 8, batteryPercent, isCharging, SSD1306_WHITE);
    
    // Centered NaTrack title
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    // "NaTrack" is 7 chars. Font is 6x8 pixels (including spacing). 7 * 6 = 42 pixels width.
    // Centered on 128px screen: (128 - 42) / 2 = 43.
    display.setCursor(43, 2);
    display.print("NaTrack");

    // Bluetooth icon top right
    IconLibrary::drawBluetooth(display, 114, 1, 10, isBluetoothConnected, blinkState, SSD1306_WHITE);

    // Divider line at Y=11
    display.drawFastHLine(0, 11, 128, SSD1306_WHITE);
}

void FooterWidget::draw(Adafruit_SSD1306& display, float temperature, int batteryPercent, bool isCharging) noexcept {
    // Bottom Bar is Y=52 to Y=63.
    // Divider at Y=51
    display.drawFastHLine(0, 51, 128, SSD1306_WHITE);

    // Temperature icon + value bottom left
    IconLibrary::drawThermometer(display, 2, 53, 9, SSD1306_WHITE);
    display.setCursor(12, 54);
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.print(temperature, 1);
    display.print("C");

    // Battery % bottom right
    display.setCursor(92, 54);
    display.print(batteryPercent);
    display.print("%");
    
    IconLibrary::drawBattery(display, 114, 54, 12, 7, batteryPercent, isCharging, SSD1306_WHITE);
}

void CardWidget::draw(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h, uint16_t color) noexcept {
    // Draw simple rounded rect simulating 2px radius
    display.drawRoundRect(x, y, w, h, 2, color);
}

void StatusWidget::draw(Adafruit_SSD1306& display, int16_t x, int16_t y, const char* statusText, bool inverted) noexcept {
    display.setTextSize(1);
    // Find text width (length * 6)
    int16_t len = strlen(statusText);
    int16_t w = len * 6 + 4;
    int16_t h = 10;

    if (inverted) {
        display.fillRect(x, y, w, h, SSD1306_WHITE);
        display.setTextColor(SSD1306_BLACK);
        display.setCursor(x + 2, y + 1);
        display.print(statusText);
    } else {
        display.drawRect(x, y, w, h, SSD1306_WHITE);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(x + 2, y + 1);
        display.print(statusText);
    }
}

void ValueWidget::drawLarge(Adafruit_SSD1306& display, int16_t x, int16_t y, const char* val, const char* unit) noexcept {
    // Draw large text (Size 2)
    display.setTextSize(2);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(x, y);
    display.print(val);

    // Draw unit (Size 1) on the side
    int16_t len = strlen(val);
    int16_t offsetW = len * 12 + 4; // size 2 chars are 12 pixels wide
    display.setTextSize(1);
    display.setCursor(x + offsetW, y + 8); // bottom aligned to the large text
    display.print(unit);
}

} // namespace NaTrack
