#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "DeviceState.hpp"
#include "ScreenManager.hpp"
#include "NavigationManager.hpp"

namespace Flux {

class UIManager {
public:
    void begin(NavigationManager* nav) noexcept;
    void render(Adafruit_SSD1306& display, const DeviceState& state, bool btBlinkState) noexcept;

private:
    ScreenManager screenMgr_;
    NavigationManager* nav_ = nullptr;
};

} // namespace Flux
