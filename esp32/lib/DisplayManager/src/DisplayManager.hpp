#pragma once

#include <Arduino.h>
#include <Wire.h>

#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#include "DisplayManagerConfig.hpp"
#include "DeviceState.hpp"
#include "UIManager.hpp"

namespace NaTrack {

class NavigationManager;

class DisplayManager {
public:
    void begin(DeviceState* state, UIManager* uiMgr, NavigationManager* nav) noexcept;
    void update() noexcept;

private:
    bool tryInitializeDisplay(uint8_t address) noexcept;
    void drawBootAnimation(unsigned long elapsed) noexcept;

    Adafruit_SSD1306 display_{
        DisplayManagerConfig::DisplayWidth,
        DisplayManagerConfig::DisplayHeight,
        &Wire,
        -1
    };

    bool displayReady_ = false;
    unsigned long bootStartMs_ = 0UL;

    DeviceState* state_ = nullptr;
    UIManager* uiMgr_ = nullptr;
    NavigationManager* nav_ = nullptr;

    // Cache to check if anything changed before redrawing
    int lastSodium_ = -1;
    float lastTemp_ = -1.0f;
    int lastBatPercent_ = -1;
    float lastBatVolts_ = -1.0f;
    bool lastBtConnected_ = false;
    int lastSettingsIndex_ = -1;
    ScreenId lastActiveScreen_ = ScreenId::Count;
    bool lastTransitioning_ = false;
    int16_t lastSlideOffset_ = -999;
    bool lastBlinkState_ = false;

    // Bluetooth blinking
    unsigned long lastBtBlinkToggleMs_ = 0UL;
    bool btBlinkState_ = false;
};

} // namespace NaTrack