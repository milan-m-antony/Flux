#include "DisplayManager.hpp"

namespace NaTrack {

void DisplayManager::begin(DeviceState* state, UIManager* uiMgr, NavigationManager* nav) noexcept {
    state_ = state;
    uiMgr_ = uiMgr;
    nav_ = nav;

    // Start I2C
    Wire.begin(DisplayManagerConfig::I2CSdaPin, DisplayManagerConfig::I2CSclPin);

    displayReady_ = tryInitializeDisplay(DisplayManagerConfig::PrimaryOledAddress) ||
                    tryInitializeDisplay(DisplayManagerConfig::SecondaryOledAddress);

    if (displayReady_) {
        display_.clearDisplay();
        display_.display();
    }

    bootStartMs_ = millis();
    lastBtBlinkToggleMs_ = millis();
    btBlinkState_ = false;

    if (state_ != nullptr) {
        state_->bootCompleted = false;
    }
}

bool DisplayManager::tryInitializeDisplay(uint8_t address) noexcept {
    return display_.begin(SSD1306_SWITCHCAPVCC, address);
}

void DisplayManager::drawBootAnimation(unsigned long elapsed) noexcept {
    display_.clearDisplay();

    if (elapsed < 300) {
        // Frame 1: Black screen
        display_.display();
        return;
    }

    // 300ms: Small droplet outline
    if (elapsed >= 300 && elapsed < 1000) {
        IconLibrary::drawDroplet(display_, 56, 12, 16, 24, false, SSD1306_WHITE);
    }
    // 1000ms: Droplet fills
    else if (elapsed >= 1000) {
        IconLibrary::drawDroplet(display_, 56, 12, 16, 24, true, SSD1306_WHITE);
    }

    // 1800ms: NaTrack fades in (centered)
    if (elapsed >= 1800) {
        display_.setTextSize(2);
        display_.setTextColor(SSD1306_WHITE);
        display_.setCursor(22, 38);
        display_.print("NaTrack");
    }

    // 2800ms: Smart Sodium Monitoring appears
    if (elapsed >= 2800) {
        display_.setTextSize(1);
        display_.setCursor(28, 54);
        display_.print("Smart Sodium");
    }

    // 3800ms: Loading dots ● ○ ○ -> ● ● ○ -> ● ● ●
    if (elapsed >= 3800) {
        int dotStage = ((elapsed - 3800) / 300) % 3;
        display_.setTextSize(1);
        display_.setCursor(52, 2);
        if (dotStage == 0) {
            display_.print("* o o");
        } else if (dotStage == 1) {
            display_.print("* * o");
        } else {
            display_.print("* * *");
        }
    }

    display_.display();
}

void DisplayManager::update() noexcept {
    if (!displayReady_ || state_ == nullptr || uiMgr_ == nullptr) {
        return;
    }

    const unsigned long nowMs = millis();

    // Toggle Bluetooth blink every 500ms when disconnected
    if ((nowMs - lastBtBlinkToggleMs_) >= 500UL) {
        lastBtBlinkToggleMs_ = nowMs;
        btBlinkState_ = !btBlinkState_;
    }

    if (!state_->bootCompleted) {
        unsigned long elapsed = nowMs - bootStartMs_;
        if (elapsed >= 5000UL) {
            state_->bootCompleted = true;
            // Force redraw on first render
            lastActiveScreen_ = ScreenId::Count;
        } else {
            drawBootAnimation(elapsed);
            return;
        }
    }

    // Cache checking
    int sodiumVal = state_->sodiumValue;
    float tempCelsius = state_->temperature;
    int batteryPercent = state_->batteryPercentage;
    float batteryVolts = state_->batteryVoltage;
    bool isBluetoothConnected = state_->bluetoothConnected;
    ScreenId activeScreen = state_->activeScreen;
    
    bool transitioning = nav_ ? nav_->isTransitioning() : false;
    int16_t slideOffset = nav_ ? nav_->getSlideOffset() : 0;
    int settingsIndex = nav_ ? nav_->getSettingsIndex() : 0;

    bool stateChanged = (sodiumVal != lastSodium_ ||
                         abs(tempCelsius - lastTemp_) > 0.05f ||
                         batteryPercent != lastBatPercent_ ||
                         abs(batteryVolts - lastBatVolts_) > 0.01f ||
                         isBluetoothConnected != lastBtConnected_ ||
                         activeScreen != lastActiveScreen_ ||
                         transitioning != lastTransitioning_ ||
                         slideOffset != lastSlideOffset_ ||
                         settingsIndex != lastSettingsIndex_ ||
                         (isBluetoothConnected == false && btBlinkState_ != lastBlinkState_));

    if (!stateChanged) {
        return; 
    }

    lastSodium_ = sodiumVal;
    lastTemp_ = tempCelsius;
    lastBatPercent_ = batteryPercent;
    lastBatVolts_ = batteryVolts;
    lastBtConnected_ = isBluetoothConnected;
    lastActiveScreen_ = activeScreen;
    lastTransitioning_ = transitioning;
    lastSlideOffset_ = slideOffset;
    lastSettingsIndex_ = settingsIndex;
    lastBlinkState_ = btBlinkState_;

    display_.clearDisplay();
    uiMgr_->render(display_, *state_, btBlinkState_);
    display_.display();
}

} // namespace NaTrack