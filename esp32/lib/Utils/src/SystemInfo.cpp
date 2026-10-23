#include "SystemInfo.hpp"

namespace Flux {

SystemInfo SystemInfoProvider::collect() noexcept {
    // Start with a default-constructed record and fill each field explicitly.
    SystemInfo info{};

    // Read the current amount of free heap memory from the Arduino core.
    info.freeHeapBytes = ESP.getFreeHeap();

    // Read the chip model string from the ESP32 core.
    info.chipModel = ESP.getChipModel();

    // Read the flash size string from the ESP32 core.
    info.flashSize = String(ESP.getFlashChipSize());

    // Read the SDK version string from the ESP32 core.
    info.sdkVersion = ESP.getSdkVersion();

    // Read the current uptime in milliseconds from the Arduino timing API.
    info.runningTimeMs = millis();

    // Return the fully populated system snapshot to the caller.
    return info;
}

} // namespace Flux
