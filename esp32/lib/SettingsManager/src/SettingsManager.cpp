#include "SettingsManager.hpp"

namespace Flux {

void SettingsManager::begin(DeviceState* state) noexcept {
    state_ = state;
    settingsIndex_ = 0;
}

void SettingsManager::update() noexcept {
    // Future settings logic (EEPROM read/write, contrast control, sleep timeout)
}

} // namespace Flux
