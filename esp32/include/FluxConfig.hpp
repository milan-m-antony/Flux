#pragma once

#include <Arduino.h>

namespace Flux::Config {

// Serial baud rate used by every firmware module
constexpr unsigned long SerialBaudRate = 115200UL;

// Period in milliseconds between status reports on the Serial Monitor
constexpr unsigned long StatusReportIntervalMs = 1000UL;

// DS18B20 1-Wire data pin
constexpr uint8_t TemperatureSensorPin = 4U;

// System startup labels
constexpr const char* DeviceStartedLabel = "Flux Telemetry Started";
constexpr const char* FreeHeapLabel = "Free Heap";
constexpr const char* ChipModelLabel = "Chip Model";
constexpr const char* FlashSizeLabel = "Flash Size";
constexpr const char* SdkVersionLabel = "SDK Version";
constexpr const char* RunningTimeLabel = "Running Time";
constexpr const char* BytesUnit = "bytes";
constexpr const char* MillisecondsUnit = "ms";

} // namespace Flux::Config
