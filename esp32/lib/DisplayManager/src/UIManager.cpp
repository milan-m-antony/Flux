#include "UIManager.hpp"

namespace Flux {

void UIManager::begin(NavigationManager* nav) noexcept {
    nav_ = nav;
}

void UIManager::render(Adafruit_SSD1306& display, const DeviceState& state, bool btBlinkState) noexcept {
    if (nav_ == nullptr) {
        return;
    }

    // 1. Draw persistent top header (Flame + bold Flux + Bluetooth + Wi-Fi + divider line)
    HeaderWidget::draw(display, state, btBlinkState);

    bool transitioning = nav_->isTransitioning();
    int16_t slideOffset = nav_->getSlideOffset();
    int settingsIndex = nav_->getSettingsIndex();

    if (transitioning) {
        int16_t prevOffset = (slideOffset > 0) ? (slideOffset - 128) : (slideOffset + 128);

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
