#pragma once

#include <Arduino.h>

#include "NaTrackConfig.hpp"
#include "SystemInfo.hpp"

namespace NaTrack {

class StatusReporter {
public:
    // Creates a reporter that writes human-readable status lines to Serial.
    StatusReporter(Stream& output, unsigned long intervalMs) noexcept;

    // Prints the initial banner immediately after startup.
    void begin() noexcept;

    // Emits a new status report whenever the configured interval has elapsed.
    void update() noexcept;

private:
    // Writes one full status snapshot to the output stream.
    void printSnapshot() noexcept;

    // Stores the serial-style output stream used for all prints.
    Stream* output_ = nullptr;

    // Stores the interval between two status reports.
    unsigned long intervalMs_ = 0UL;

    // Stores the last time a snapshot was printed.
    unsigned long lastReportMs_ = 0UL;
};

} // namespace NaTrack
