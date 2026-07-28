#include "NavigationManager.hpp"

namespace NaTrack {

void NavigationManager::begin(DeviceState* state) noexcept {
    state_ = state;
    transitioning_ = false;
    transitionStartMs_ = 0UL;
    slideLeft_ = true;
    settingsIndex_ = 0;

    if (state_ != nullptr) {
        state_->activeScreen = ScreenId::Home;
        state_->previousScreen = ScreenId::Home;
    }
}

void NavigationManager::onButtonEvent(ButtonId button, ButtonEvent event) noexcept {
    if (state_ == nullptr) {
        return;
    }

    // Long press on OK returns Home
    if (button == ButtonId::Ok && event == ButtonEvent::LongPressed) {
        if (state_->activeScreen != ScreenId::Home) {
            state_->previousScreen = state_->activeScreen;
            state_->activeScreen = ScreenId::Home;
            transitioning_ = true;
            slideLeft_ = false; // slide right/back to home
            transitionStartMs_ = millis();
        }
        return;
    }

    if (event != ButtonEvent::Pressed) {
        return; // Only trigger navigation on short press
    }

    if (transitioning_) {
        return; // Ignore inputs during active transitions
    }

    if (button == ButtonId::Left) {
        // LEFT -> Go to previous screen (slides screen right)
        state_->previousScreen = state_->activeScreen;
        int nextId = static_cast<int>(state_->activeScreen) - 1;
        if (nextId < 0) {
            nextId = static_cast<int>(ScreenId::Count) - 1;
        }
        state_->activeScreen = static_cast<ScreenId>(nextId);
        transitioning_ = true;
        slideLeft_ = false;
        transitionStartMs_ = millis();
    } else if (button == ButtonId::Right) {
        // RIGHT -> Go to next screen (slides screen left)
        state_->previousScreen = state_->activeScreen;
        int nextId = static_cast<int>(state_->activeScreen) + 1;
        if (nextId >= static_cast<int>(ScreenId::Count)) {
            nextId = 0;
        }
        state_->activeScreen = static_cast<ScreenId>(nextId);
        transitioning_ = true;
        slideLeft_ = true;
        transitionStartMs_ = millis();
    } else if (button == ButtonId::Ok) {
        // OK -> Open Screen / Select
        if (state_->activeScreen == ScreenId::Settings) {
            // Cycle through settings menu items
            settingsIndex_ = (settingsIndex_ + 1) % 5;
        }
    }
}

int16_t NavigationManager::getSlideOffset() const noexcept {
    if (!transitioning_) {
        return 0;
    }
    unsigned long elapsed = millis() - transitionStartMs_;
    float t = static_cast<float>(elapsed) / static_cast<float>(TransitionDurationMs);
    if (t > 1.0f) {
        t = 1.0f;
    }

    if (slideLeft_) {
        return static_cast<int16_t>(128.0f * (1.0f - t));
    } else {
        return static_cast<int16_t>(-128.0f * (1.0f - t));
    }
}

void NavigationManager::update() noexcept {
    if (transitioning_) {
        const unsigned long nowMs = millis();
        if ((nowMs - transitionStartMs_) >= TransitionDurationMs) {
            transitioning_ = false;
        }
    }
}

} // namespace NaTrack
