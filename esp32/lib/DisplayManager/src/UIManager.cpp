#include "UIManager.hpp"

namespace Flux {

void UIManager::begin(NavigationManager* nav) noexcept {
    nav_ = nav;
}

void UIManager::render(Adafruit_SSD1306& display, const DeviceState& state, bool btBlinkState) noexcept {
    if (nav_ == nullptr) {
        return;
    }

    int batteryPercent = state.batteryPercentage;
    bool isCharging = state.charging;
    bool isBluetoothConnected = state.bluetoothConnected;
    float tempCelsius = state.temperature;

    // Draw static Top and Bottom bars
    HeaderWidget::draw(display, batteryPercent, isCharging, isBluetoothConnected, btBlinkState);
    FooterWidget::draw(display, state);

    bool transitioning = nav_->isTransitioning();
    int16_t slideOffset = nav_->getSlideOffset();
    int settingsIndex = nav_->getSettingsIndex();

    if (transitioning) {
        int16_t prevOffset = 0;
        if (slideOffset > 0) {
            prevOffset = slideOffset - 128;
        } else {
            prevOffset = slideOffset + 128;
        }

        // Draw previous screen content
        screenMgr_.drawScreen(display, state.previousScreen, prevOffset, state, settingsIndex);

        // Draw active screen content
        screenMgr_.drawScreen(display, state.activeScreen, slideOffset, state, settingsIndex);
    } else {
        // Draw normal active screen content
        screenMgr_.drawScreen(display, state.activeScreen, 0, state, settingsIndex);
    }
}

} // namespace Flux
