#include "NavigationManager.hpp"

namespace Flux {

void NavigationManager::begin(DeviceState* state) noexcept {
    state_ = state;
    transitioning_ = false;
    transitionStartMs_ = 0UL;
    slideLeft_ = true;
    settingsIndex_ = 0;
    modeIndex_ = 0;

    if (state_ != nullptr) {
        state_->activeScreen = ScreenId::MainMonitor;
        state_->previousScreen = ScreenId::MainMonitor;
    }
}

void NavigationManager::onButtonEvent(ButtonId button, ButtonEvent event) noexcept {
    if (state_ == nullptr) {
        return;
    }

    // Pressing any button during boot instantly completes boot animation
    if (!state_->bootCompleted) {
        state_->bootCompleted = true;
        return;
    }

    // Long press on OK returns to MainMonitor from any screen
    if (button == ButtonId::Ok && event == ButtonEvent::LongPressed) {
        if (state_->activeScreen != ScreenId::MainMonitor) {
            state_->previousScreen = state_->activeScreen;
            state_->activeScreen = ScreenId::MainMonitor;
            transitioning_ = true;
            slideLeft_ = false;
            transitionStartMs_ = millis();
        }
        return;
    }

    if (event != ButtonEvent::Pressed) {
        return;
    }

    if (transitioning_) {
        return;
    }

    // 1. Handling inside ModeSelection menu
    if (state_->activeScreen == ScreenId::ModeSelection) {
        if (button == ButtonId::Left) {
            if (modeIndex_ == 0) {
                // Return back to MainMonitor when at top
                state_->previousScreen = ScreenId::ModeSelection;
                state_->activeScreen = ScreenId::MainMonitor;
                transitioning_ = true;
                slideLeft_ = false;
                transitionStartMs_ = millis();
            } else {
                modeIndex_--;
            }
        } else if (button == ButtonId::Right) {
            modeIndex_ = (modeIndex_ + 1) % 4;
        } else if (button == ButtonId::Ok) {
            // Select highlighted profile mode and return to MainMonitor
            state_->activeProfile = static_cast<ProfileMode>(modeIndex_);
            state_->previousScreen = ScreenId::ModeSelection;
            state_->activeScreen = ScreenId::MainMonitor;
            transitioning_ = true;
            slideLeft_ = false;
            transitionStartMs_ = millis();
        }
        return;
    }

    // 2. Handling inside Settings menu
    if (state_->activeScreen == ScreenId::Settings) {
        if (button == ButtonId::Left) {
            if (settingsIndex_ == 0) {
                // Return back to MainMonitor when at top
                state_->previousScreen = ScreenId::Settings;
                state_->activeScreen = ScreenId::MainMonitor;
                transitioning_ = true;
                slideLeft_ = false;
                transitionStartMs_ = millis();
            } else {
                settingsIndex_--;
            }
        } else if (button == ButtonId::Right) {
            settingsIndex_ = (settingsIndex_ + 1) % 4;
        } else if (button == ButtonId::Ok) {
            if (settingsIndex_ == 0) {
                // Wi-Fi Settings -> open WifiStatus
                state_->previousScreen = ScreenId::Settings;
                state_->activeScreen = ScreenId::WifiStatus;
                transitioning_ = true;
                slideLeft_ = true;
                transitionStartMs_ = millis();
            } else if (settingsIndex_ == 1) {
                // Bluetooth -> open BluetoothStatus
                state_->previousScreen = ScreenId::Settings;
                state_->activeScreen = ScreenId::BluetoothStatus;
                transitioning_ = true;
                slideLeft_ = true;
                transitionStartMs_ = millis();
            } else if (settingsIndex_ == 2) {
                // Preferences -> open Preferences screen
                state_->previousScreen = ScreenId::Settings;
                state_->activeScreen = ScreenId::Preferences;
                transitioning_ = true;
                slideLeft_ = true;
                transitionStartMs_ = millis();
            } else if (settingsIndex_ == 3) {
                // Device Info -> open DeviceInfo
                state_->previousScreen = ScreenId::Settings;
                state_->activeScreen = ScreenId::DeviceInfo;
                transitioning_ = true;
                slideLeft_ = true;
                transitionStartMs_ = millis();
            }
        }
        return;
    }

    // 2b. Handling inside Preferences screen
    if (state_->activeScreen == ScreenId::Preferences) {
        if (button == ButtonId::Left) {
            if (prefIndex_ == 0) {
                // Return back to Settings menu
                state_->previousScreen = ScreenId::Preferences;
                state_->activeScreen = ScreenId::Settings;
                transitioning_ = true;
                slideLeft_ = false;
                transitionStartMs_ = millis();
            } else {
                prefIndex_--;
            }
        } else if (button == ButtonId::Right) {
            prefIndex_ = (prefIndex_ + 1) % 4;
        } else if (button == ButtonId::Ok) {
            if (prefIndex_ == 0) {
                // Toggle Preferred Unit (°C <-> °F)
                state_->preferredUnit = (state_->preferredUnit == TempUnit::Celsius)
                                        ? TempUnit::Fahrenheit
                                        : TempUnit::Celsius;
            } else if (prefIndex_ == 1) {
                // Cycle High Threshold: 26 -> 28 -> 30 -> 32 -> 35 -> 26
                if (state_->highThreshold < 27.0f) state_->highThreshold = 28.0f;
                else if (state_->highThreshold < 29.0f) state_->highThreshold = 30.0f;
                else if (state_->highThreshold < 31.0f) state_->highThreshold = 32.0f;
                else if (state_->highThreshold < 34.0f) state_->highThreshold = 35.0f;
                else state_->highThreshold = 26.0f;
            } else if (prefIndex_ == 2) {
                // Cycle Low Threshold: 12 -> 15 -> 18 -> 20 -> 12
                if (state_->lowThreshold < 14.0f) state_->lowThreshold = 15.0f;
                else if (state_->lowThreshold < 17.0f) state_->lowThreshold = 18.0f;
                else if (state_->lowThreshold < 19.0f) state_->lowThreshold = 20.0f;
                else state_->lowThreshold = 12.0f;
            } else if (prefIndex_ == 3) {
                // Toggle Buzzer
                state_->buzzerEnabled = !state_->buzzerEnabled;
            }
        }
        return;
    }

    // 3. Handling inside sub-screens (DeviceInfo, WifiStatus, BluetoothStatus)
    if (state_->activeScreen == ScreenId::DeviceInfo ||
        state_->activeScreen == ScreenId::WifiStatus ||
        state_->activeScreen == ScreenId::BluetoothStatus) {
        if (button == ButtonId::Ok || button == ButtonId::Left) {
            // Return back to Settings menu
            state_->previousScreen = state_->activeScreen;
            state_->activeScreen = ScreenId::Settings;
            transitioning_ = true;
            slideLeft_ = false;
            transitionStartMs_ = millis();
        } else if (button == ButtonId::Right) {
            // Return to MainMonitor
            state_->previousScreen = state_->activeScreen;
            state_->activeScreen = ScreenId::MainMonitor;
            transitioning_ = true;
            slideLeft_ = true;
            transitionStartMs_ = millis();
        }
        return;
    }

    // 4. Handling on MainMonitor screen
    if (state_->activeScreen == ScreenId::MainMonitor) {
        if (button == ButtonId::Left) {
            state_->previousScreen = ScreenId::MainMonitor;
            state_->activeScreen = ScreenId::Settings;
            transitioning_ = true;
            slideLeft_ = false;
            transitionStartMs_ = millis();
        } else if (button == ButtonId::Right) {
            state_->previousScreen = ScreenId::MainMonitor;
            state_->activeScreen = ScreenId::ModeSelection;
            transitioning_ = true;
            slideLeft_ = true;
            transitionStartMs_ = millis();
        } else if (button == ButtonId::Ok) {
            // Quick toggle unit (Celsius <-> Fahrenheit)
            state_->preferredUnit = (state_->preferredUnit == TempUnit::Celsius)
                                    ? TempUnit::Fahrenheit
                                    : TempUnit::Celsius;
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

} // namespace Flux
