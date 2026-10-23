#pragma once

#include <Arduino.h>

namespace Flux {

class PowerManager {
public:
    // Prepares the power layer placeholder for battery monitoring work later.
    void begin() noexcept;

    // Keeps the power module interface ready for non-blocking expansion.
    void update() noexcept;
};

} // namespace Flux
