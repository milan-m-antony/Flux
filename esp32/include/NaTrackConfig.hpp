#pragma once

#include <Arduino.h>

namespace NaTrack::Config {

// Serial baud rate used by every firmware module in this first architecture layer.
constexpr unsigned long SerialBaudRate = 115200UL;

// Period in milliseconds between status reports on the Serial Monitor.
constexpr unsigned long StatusReportIntervalMs = 1000UL;

// DS18B20 data pin
constexpr uint8_t TemperatureSensorPin = 4U;

// Short label printed before the firmware status starts.
constexpr const char* DeviceStartedLabel = "Device Started";

// Label printed before the amount of free heap memory.
constexpr const char* FreeHeapLabel = "Free Heap";

// Label printed before the ESP32 chip model string.
constexpr const char* ChipModelLabel = "Chip Model";

// Label printed before the flash size value.
constexpr const char* FlashSizeLabel = "Flash Size";

// Label printed before the SDK version string.
constexpr const char* SdkVersionLabel = "SDK Version";

// Label printed before the firmware running time value.
constexpr const char* RunningTimeLabel = "Running Time";

// Unit suffix used when printing bytes.
constexpr const char* BytesUnit = "bytes";

// Unit suffix used when printing milliseconds.
constexpr const char* MillisecondsUnit = "ms";

} // namespace NaTrack::Config

