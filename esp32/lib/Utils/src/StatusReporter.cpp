#include "StatusReporter.hpp"

namespace NaTrack {

StatusReporter::StatusReporter(Stream& output, unsigned long intervalMs) noexcept
    : output_{&output}, intervalMs_{intervalMs}, lastReportMs_{0UL} {}

void StatusReporter::begin() noexcept {
    // Print the first status snapshot right away so startup feedback is immediate.
    printSnapshot();

    // Remember the current time so the next report waits for the configured interval.
    lastReportMs_ = millis();
}

void StatusReporter::update() noexcept {
    // Read the current uptime once so the timing decision stays consistent.
    const unsigned long nowMs = millis();

    // Exit early until the full interval has elapsed.
    if ((nowMs - lastReportMs_) < intervalMs_) {
        return;
    }

    // Refresh the timestamp before printing so the schedule stays stable.
    lastReportMs_ = nowMs;

    // Print the next status snapshot without blocking the rest of the firmware.
    printSnapshot();
}

void StatusReporter::printSnapshot() noexcept {
    // Avoid printing if the output stream has not been wired up yet.
    if (output_ == nullptr) {
        return;
    }

    // Collect the current system snapshot from the ESP32 runtime.
    const SystemInfo info = SystemInfoProvider::collect();

    // Print the startup marker every time so the monitor clearly shows a live system.
    output_->println(Config::DeviceStartedLabel);

    // Print the current heap value and its unit.
    output_->print(Config::FreeHeapLabel);
    output_->print(": ");
    output_->print(info.freeHeapBytes);
    output_->print(' ');
    output_->println(Config::BytesUnit);

    // Print the chip model string.
    output_->print(Config::ChipModelLabel);
    output_->print(": ");
    output_->println(info.chipModel);

    // Print the flash size value and its unit.
    output_->print(Config::FlashSizeLabel);
    output_->print(": ");
    output_->print(info.flashSize);
    output_->print(' ');
    output_->println(Config::BytesUnit);

    // Print the SDK version string.
    output_->print(Config::SdkVersionLabel);
    output_->print(": ");
    output_->println(info.sdkVersion);

    // Print the running time and its unit.
    output_->print(Config::RunningTimeLabel);
    output_->print(": ");
    output_->print(info.runningTimeMs);
    output_->print(' ');
    output_->println(Config::MillisecondsUnit);

    // Insert a blank line to keep each report easy to scan.
    output_->println();
}

} // namespace NaTrack
