#include "PowerManager.hpp"

namespace NaTrack {

void PowerManager::begin() noexcept {
    // Battery support is not active yet, so there is nothing to initialize here.
}

void PowerManager::update() noexcept {
    // The future power-management loop will remain non-blocking as well.
}

} // namespace NaTrack
