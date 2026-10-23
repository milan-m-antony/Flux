#include "Widgets.hpp"

namespace Flux {

void HeaderWidget::draw(Adafruit_SSD1306& display, const DeviceState& state, bool blinkState) noexcept {
    // Top Bar is Y=0 to Y=11.
    // 1. Top Left: WiFi icon (matching Bluetooth on right)
    IconLibrary::drawWifi(display, 4, 1, 10, state.wifiConnected, blinkState, SSD1306_WHITE);
    
    // 2. Centered FLUX title
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    // "FLUX" is 4 chars. 4 * 6 = 24 px. (128 - 24) / 2 = 52.
    display.setCursor(52, 2);
    display.print("FLUX");

    // 3. Top Right: Bluetooth icon
    IconLibrary::drawBluetooth(display, 114, 1, 10, state.bluetoothConnected, blinkState, SSD1306_WHITE);

    // Divider line at Y=11
    display.drawFastHLine(0, 11, 128, SSD1306_WHITE);
}

void FooterWidget::draw(Adafruit_SSD1306& display, const DeviceState& state) noexcept {
    // Bottom Bar is Y=52 to Y=63.
    // Divider at Y=51
    display.drawFastHLine(0, 51, 128, SSD1306_WHITE);

    // 1. Bottom Left: Clock icon + Live Time display (HH:MM:SS)
    IconLibrary::drawClock(display, 2, 53, 8, SSD1306_WHITE);
    char timeBuf[16];
    state.getFormattedTime(timeBuf, sizeof(timeBuf));
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(12, 54);
    display.print(timeBuf);

    // 2. Center: Empty (removed profile badge per user request)

    // 3. Bottom Right: Battery % and Battery icon
    int batX = (state.batteryPercentage >= 100) ? 90 : 94;
    display.setCursor(batX, 54);
    display.printf("%d%%", state.batteryPercentage);
    IconLibrary::drawBattery(display, 114, 53, 12, 8, state.batteryPercentage, state.charging, SSD1306_WHITE);
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
