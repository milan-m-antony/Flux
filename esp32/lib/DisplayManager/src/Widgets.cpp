#include "Widgets.hpp"

namespace Flux {

void HeaderWidget::draw(Adafruit_SSD1306& display, int batteryPercent, bool isCharging, bool isBluetoothConnected, bool blinkState) noexcept {
    // Top Bar is Y=0 to Y=11.
    // Battery icon top left
    IconLibrary::drawBattery(display, 2, 2, 14, 8, batteryPercent, isCharging, SSD1306_WHITE);
    
    // Centered FLUX title
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    // "FLUX" is 4 chars. 4 * 6 = 24 px. (128 - 24) / 2 = 52.
    display.setCursor(52, 2);
    display.print("FLUX");

    // Bluetooth icon top right
    IconLibrary::drawBluetooth(display, 114, 1, 10, isBluetoothConnected, blinkState, SSD1306_WHITE);

    // Divider line at Y=11
    display.drawFastHLine(0, 11, 128, SSD1306_WHITE);
}

void FooterWidget::draw(Adafruit_SSD1306& display, float temperature, TempUnit unit, int batteryPercent, bool isCharging) noexcept {
    // Bottom Bar is Y=52 to Y=63.
    // Divider at Y=51
    display.drawFastHLine(0, 51, 128, SSD1306_WHITE);

    // Temperature icon + value bottom left
    IconLibrary::drawThermometer(display, 2, 53, 9, SSD1306_WHITE);
    display.setCursor(12, 54);
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    
    if (temperature > -50.0f && temperature < 125.0f) {
        if (unit == TempUnit::Fahrenheit) {
            float tempF = (temperature * 9.0f / 5.0f) + 32.0f;
            display.printf("%.1fF", tempF);
        } else {
            display.printf("%.1fC", temperature);
        }
    } else {
        display.print("--.-");
    }

    // Battery % bottom right
    display.setCursor(92, 54);
    display.print(batteryPercent);
    display.print("%");
    
    IconLibrary::drawBattery(display, 114, 54, 12, 7, batteryPercent, isCharging, SSD1306_WHITE);
}

void CardWidget::draw(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h, uint16_t color) noexcept {
    display.drawRoundRect(x, y, w, h, 2, color);
}

void StatusWidget::draw(Adafruit_SSD1306& display, int16_t x, int16_t y, const char* statusText, bool inverted) noexcept {
    display.setTextSize(1);
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
    display.setTextSize(2);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(x, y);
    display.print(val);

    int16_t len = strlen(val);
    int16_t offsetW = len * 12 + 4;
    display.setTextSize(1);
    display.setCursor(x + offsetW, y + 8);
    display.print(unit);
}

} // namespace Flux
