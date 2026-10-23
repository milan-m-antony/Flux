#include "DisplayManager.hpp"

namespace Flux {

void DisplayManager::begin(DeviceState* state, UIManager* uiMgr, NavigationManager* nav) noexcept {
    state_ = state;
    uiMgr_ = uiMgr;
    nav_ = nav;

    // Start I2C bus
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

    // 0 - 200ms: Black buffer
    if (elapsed < 200) {
        display_.display();
        return;
    }

    // Centered FLUX title in large font
    display_.setTextSize(2);
    display_.setTextColor(SSD1306_WHITE);
    // "FLUX" is 4 chars * 12px = 48px width. (128 - 48)/2 = 40
    display_.setCursor(40, 14);
    display_.print("FLUX");

    // Subtitle
    display_.setTextSize(1);
    display_.setCursor(2, 34);
    display_.print("Smart Telemetry System");

    // Progress bar (fills across 1800ms)
    uint8_t progressPercent = (elapsed > 1800) ? 100 : (elapsed * 100 / 1800);
    display_.drawRect(14, 48, 100, 7, SSD1306_WHITE);
    uint8_t innerW = (progressPercent * 96) / 100;
    if (innerW > 0) {
        display_.fillRect(16, 50, innerW, 3, SSD1306_WHITE);
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
        if (elapsed >= 1800UL) {
            state_->bootCompleted = true;
            // Force redraw on first render
            lastActiveScreen_ = ScreenId::Count;
        } else {
            drawBootAnimation(elapsed);
            return;
        }
    }

    // Cache checking
    float tempCelsius = state_->temperature;
    int batteryPercent = state_->batteryPercentage;
    float batteryVolts = state_->batteryVoltage;
    bool isBluetoothConnected = state_->bluetoothConnected;
    ScreenId activeScreen = state_->activeScreen;
    int preferredUnitInt = static_cast<int>(state_->preferredUnit);
    int activeProfileInt = static_cast<int>(state_->activeProfile);
    bool isSensorConn = state_->sensorConnected;
    bool isAlert = state_->isThresholdAlert;
    
    bool transitioning = nav_ ? nav_->isTransitioning() : false;
    int16_t slideOffset = nav_ ? nav_->getSlideOffset() : 0;
    int settingsIndex = nav_ ? nav_->getSettingsIndex() : 0;

    static int lastUnit = -1;
    static int lastProfile = -1;
    static bool lastSensorConn = false;
    static bool lastAlert = false;

    bool stateChanged = (abs(tempCelsius - lastTemp_) > 0.05f ||
                         batteryPercent != lastBatPercent_ ||
                         abs(batteryVolts - lastBatVolts_) > 0.01f ||
                         isBluetoothConnected != lastBtConnected_ ||
                         activeScreen != lastActiveScreen_ ||
                         transitioning != lastTransitioning_ ||
                         slideOffset != lastSlideOffset_ ||
                         settingsIndex != lastSettingsIndex_ ||
                         preferredUnitInt != lastUnit ||
                         activeProfileInt != lastProfile ||
                         isSensorConn != lastSensorConn ||
                         isAlert != lastAlert ||
                         (isBluetoothConnected == false && btBlinkState_ != lastBlinkState_));

    if (!stateChanged) {
        return; 
    }

    lastTemp_ = tempCelsius;
    lastBatPercent_ = batteryPercent;
    lastBatVolts_ = batteryVolts;
    lastBtConnected_ = isBluetoothConnected;
    lastActiveScreen_ = activeScreen;
    lastTransitioning_ = transitioning;
    lastSlideOffset_ = slideOffset;
    lastSettingsIndex_ = settingsIndex;
    lastBlinkState_ = btBlinkState_;
    lastUnit = preferredUnitInt;
    lastProfile = activeProfileInt;
    lastSensorConn = isSensorConn;
    lastAlert = isAlert;

    display_.clearDisplay();
    uiMgr_->render(display_, *state_, btBlinkState_);
    display_.display();
}

} // namespace Flux