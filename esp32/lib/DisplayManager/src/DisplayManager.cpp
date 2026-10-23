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

    // 1. Centered Flame Icon at top
    IconLibrary::drawFlame(display_, 64, 13, 11, SSD1306_WHITE);

    // 2. Bold "Flux" Typography (Size 2)
    display_.setTextSize(2);
    display_.setTextColor(SSD1306_WHITE);
    display_.setCursor(40, 26);
    display_.print("Flux");

    // 3. Rounded Pill Progress Bar: (X=18, Y=43, W=92, H=8, r=3)
    display_.drawRoundRect(18, 43, 92, 8, 3, SSD1306_WHITE);

    // Dynamic Progress Fill: 0 to 86 px width
    uint8_t fillW = 0;
    if (elapsed > 300) {
        fillW = (elapsed >= 2200) ? 86 : (uint8_t)((elapsed - 300) * 86 / 1900);
    }
    if (fillW > 0) {
        display_.fillRoundRect(21, 45, fillW, 4, 2, SSD1306_WHITE);
    }

    // 4. Centered "Initializing..." Status Text (Size 1)
    display_.setTextSize(1);
    display_.setTextColor(SSD1306_WHITE);
    display_.setCursor(22, 54);
    display_.print("Initializing...");

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