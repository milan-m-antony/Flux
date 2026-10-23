#include "Widgets.hpp"

namespace Flux {

void HeaderWidget::draw(Adafruit_SSD1306& display, const DeviceState& state, bool blinkState) noexcept {
    // 1. Left: Flame icon
    IconLibrary::drawFlameHeader(display, 6, 1, SSD1306_WHITE);
    
    // 2. Bold "Flux" typography (Size 2)
    display.setTextSize(2);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(22, 1);
    display.print("Flux");

    // 3. Right: Bluetooth icon (with 'x' indicator if disconnected/offline)
    IconLibrary::drawBluetooth(display, 94, 2, 12, state.bluetoothConnected, blinkState, SSD1306_WHITE);

    // 4. Far Right: Wi-Fi icon (with 'x' indicator if disconnected/offline)
    IconLibrary::drawWifi(display, 112, 2, 12, state.wifiConnected, blinkState, SSD1306_WHITE);

    // 5. Solid divider line at Y=16
    display.drawFastHLine(0, 16, 128, SSD1306_WHITE);
}

void CardWidget::draw(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h, uint16_t color) noexcept {
    display.drawRoundRect(x, y, w, h, 2, color);
}

} // namespace Flux
