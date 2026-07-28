#pragma once

#include <Arduino.h>

namespace NaTrack {

struct SystemInfo {
    // Stores the number of bytes currently available in the ESP32 heap.
    uint32_t freeHeapBytes = 0UL;

    // Stores the chip model string reported by the ESP32 core.
    String chipModel;

    // Stores the flash size string reported by the ESP32 core.
    String flashSize;

    // Stores the SDK version string reported by the ESP32 core.
    String sdkVersion;

    // Stores the elapsed time since boot in milliseconds.
    uint32_t runningTimeMs = 0UL;
};

class SystemInfoProvider {
public:
    // Collects a fresh snapshot of the ESP32 runtime and hardware information.
    static SystemInfo collect() noexcept;
};

} // namespace NaTrack
