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

void FooterWidget::draw(Adafruit_SSD1306& display, const DeviceState& state) noexcept {
    // Bottom Bar is Y=52 to Y=63.
    // Divider at Y=51
    display.drawFastHLine(0, 51, 128, SSD1306_WHITE);

    // 1. Bottom Left: Clock icon + Live Time display (00:00:00)
    IconLibrary::drawClock(display, 1, 53, 8, SSD1306_WHITE);
    char timeBuf[16];
    state.getFormattedTime(timeBuf, sizeof(timeBuf));
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(11, 54);
    display.print(timeBuf);

    // 2. Center: Active Profile Tag (LIVE, ENV, LIQ, FERM)
    const char* profileTag = "LIVE";
    if (state.activeProfile == ProfileMode::Environment) profileTag = "ENV";
    else if (state.activeProfile == ProfileMode::Liquid) profileTag = "LIQ";
    else if (state.activeProfile == ProfileMode::Fermentation) profileTag = "FERM";
    display.setCursor(64, 54);
    display.print(profileTag);

    // 3. Bottom Right: Battery % and icon
    int batX = (state.batteryPercentage >= 100) ? 91 : 95;
    display.setCursor(batX, 54);
    display.printf("%d%%", state.batteryPercentage);
    IconLibrary::drawBattery(display, 116, 54, 10, 7, state.batteryPercentage, state.charging, SSD1306_WHITE);
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
