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

    // Stage 1: 0 - 600ms: Flame Ignition & Pulsing Rings
    if (elapsed < 600) {
        int16_t ringR = (int16_t)((elapsed / 25) % 24);
        if (ringR > 2) {
            display_.drawCircle(64, 24, ringR, SSD1306_WHITE);
        }
        uint8_t flameSize = (elapsed < 400) ? (uint8_t)(6 + (elapsed * 6 / 400)) : 12;
        IconLibrary::drawFlame(display_, 64, 24, flameSize, SSD1306_WHITE);
        display_.display();
        return;
    }

    // Stage 2: 600ms - 1500ms: Flame + FLUX Typography
    if (elapsed < 1500) {
        // Glowing flame with breathing pulse
        uint8_t pulse = ((elapsed / 150) % 2 == 0) ? 12 : 13;
        IconLibrary::drawFlame(display_, 64, 18, pulse, SSD1306_WHITE);
        display_.drawCircle(64, 18, 17, SSD1306_WHITE);

        // Bold Title
        display_.setTextSize(2);
        display_.setTextColor(SSD1306_WHITE);
        display_.setCursor(40, 34);
        display_.print("FLUX");

        // Subtitle
        display_.setTextSize(1);
        display_.setCursor(18, 52);
        display_.print("SMART TELEMETRY");
        display_.display();
        return;
    }

    // Stage 3: 1500ms - 2400ms: Hardware Diagnostics & Progress Bar Loading
    // Flame & Title compact layout
    IconLibrary::drawFlame(display_, 64, 13, 10, SSD1306_WHITE);
    display_.setTextSize(2);
    display_.setTextColor(SSD1306_WHITE);
    display_.setCursor(40, 24);
    display_.print("FLUX");

    // Dynamic status text
    display_.setTextSize(1);
    if (elapsed < 1800) {
        display_.setCursor(18, 41);
        display_.print("INITIALIZING BLE...");
    } else if (elapsed < 2100) {
        display_.setCursor(24, 41);
        display_.print("PROBE SENSORS OK");
    } else {
        display_.setCursor(26, 41);
        display_.print("SYSTEM ONLINE");
    }

    // Progress bar fills across 1500ms -> 2400ms
    display_.drawRoundRect(16, 52, 96, 7, 2, SSD1306_WHITE);
    uint8_t prog = (elapsed >= 2400) ? 92 : (uint8_t)((elapsed - 1500) * 92 / 900);
    if (prog > 2) {
        display_.fillRoundRect(18, 54, prog - 2, 3, 1, SSD1306_WHITE);
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
        if (elapsed >= 2400UL) {
            state_->bootCompleted = true;
            // Force redraw on first render
            lastActiveScreen_ = ScreenId::Count;
        } else {
            drawBootAnimation(elapsed);
            return;
        }
    }

    // Check if the seconds tick changed to update the bottom-left clock display
    unsigned long currentSec = nowMs / 1000UL;
    static unsigned long lastRenderSec = 0UL;
    bool secondChanged = (currentSec != lastRenderSec);
    if (secondChanged) {
        lastRenderSec = currentSec;
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
    bool isWifiConn = state_->wifiConnected;
    
    bool transitioning = nav_ ? nav_->isTransitioning() : false;
    int16_t slideOffset = nav_ ? nav_->getSlideOffset() : 0;
    int settingsIndex = nav_ ? nav_->getSettingsIndex() : 0;

    static int lastUnit = -1;
    static int lastProfile = -1;
    static bool lastSensorConn = false;
    static bool lastAlert = false;
    static bool lastWifiConn = false;

    bool stateChanged = (secondChanged ||
                         abs(tempCelsius - lastTemp_) > 0.05f ||
                         batteryPercent != lastBatPercent_ ||
                         abs(batteryVolts - lastBatVolts_) > 0.01f ||
                         isBluetoothConnected != lastBtConnected_ ||
                         isWifiConn != lastWifiConn ||
                         activeScreen != lastActiveScreen_ ||
                         transitioning != lastTransitioning_ ||
                         slideOffset != lastSlideOffset_ ||
                         settingsIndex != lastSettingsIndex_ ||
                         preferredUnitInt != lastUnit ||
                         activeProfileInt != lastProfile ||
                         isSensorConn != lastSensorConn ||
                         isAlert != lastAlert ||
                         ((isBluetoothConnected == false || isWifiConn == false) && btBlinkState_ != lastBlinkState_));

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
    lastWifiConn = isWifiConn;

    display_.clearDisplay();
    uiMgr_->render(display_, *state_, btBlinkState_);
    display_.display();
}

} // namespace Flux