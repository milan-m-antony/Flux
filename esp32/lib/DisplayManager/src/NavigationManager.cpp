#include "NavigationManager.hpp"

namespace NaTrack {

void NavigationManager::begin(DeviceState* state) noexcept {
    state_ = state;
    transitioning_ = false;
    transitionStartMs_ = 0UL;
    slideLeft_ = true;
    settingsIndex_ = 0;

    if (state_ != nullptr) {
        state_->activeScreen = ScreenId::Dashboard;
        state_->previousScreen = ScreenId::Dashboard;
    }
}

void NavigationManager::onButtonEvent(ButtonId button, ButtonEvent event) noexcept {
    if (state_ == nullptr) {
        return;
    }

    // Long press on OK returns to Main Dashboard from any screen
    if (button == ButtonId::Ok && event == ButtonEvent::LongPressed) {
        if (state_->activeScreen != ScreenId::Dashboard) {
            state_->previousScreen = state_->activeScreen;
            state_->activeScreen = ScreenId::Dashboard;
            transitioning_ = true;
            slideLeft_ = false; // slide back to dashboard
            transitionStartMs_ = millis();
        }
        return;
    }

    if (event != ButtonEvent::Pressed) {
        return; // Trigger on short press
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
        // OK -> Interactive action on current screen
        if (state_->activeScreen == ScreenId::Settings) {
            // Cycle through settings options and apply immediate toggle
            if (settingsIndex_ == 0) {
                // Toggle Unit (C <-> F)
                state_->preferredUnit = (state_->preferredUnit == TempUnit::Celsius) 
                                        ? TempUnit::Fahrenheit 
                                        : TempUnit::Celsius;
            } else if (settingsIndex_ == 1) {
                // Cycle Profile Mode
                int nextMode = static_cast<int>(state_->activeProfile) + 1;
                if (nextMode >= static_cast<int>(ProfileMode::Count)) {
                    nextMode = 0;
                }
                state_->activeProfile = static_cast<ProfileMode>(nextMode);
            } else if (settingsIndex_ == 2) {
                // Reset Session Statistics & Elapsed Timer
                state_->minTemperature = state_->temperature;
                state_->maxTemperature = state_->temperature;
                state_->tempSum = state_->temperature;
                state_->sampleCount = (state_->temperature > -50.0f) ? 1 : 0;
                state_->avgTemperature = state_->temperature;
                state_->sessionStartMs = millis();
            } else if (settingsIndex_ == 3) {
                // Toggle Threshold presets (18-28C -> 15-25C -> 20-30C)
                if (state_->lowThreshold == 18.0f) {
                    state_->lowThreshold = 15.0f;
                    state_->highThreshold = 25.0f;
                } else if (state_->lowThreshold == 15.0f) {
                    state_->lowThreshold = 20.0f;
                    state_->highThreshold = 30.0f;
                } else {
                    state_->lowThreshold = 18.0f;
                    state_->highThreshold = 28.0f;
                }
            }
            settingsIndex_ = (settingsIndex_ + 1) % 4;
        } else if (state_->activeScreen == ScreenId::Dashboard || state_->activeScreen == ScreenId::LiveMonitor) {
            // Quick unit toggle from dashboard/live monitor
            state_->preferredUnit = (state_->preferredUnit == TempUnit::Celsius) 
                                    ? TempUnit::Fahrenheit 
                                    : TempUnit::Celsius;
        } else if (state_->activeScreen == ScreenId::StatisticsTimer) {
            // Reset statistics on OK press
            state_->minTemperature = state_->temperature;
            state_->maxTemperature = state_->temperature;
            state_->tempSum = state_->temperature;
            state_->sampleCount = (state_->temperature > -50.0f) ? 1 : 0;
            state_->avgTemperature = state_->temperature;
            state_->sessionStartMs = millis();
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
