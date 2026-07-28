#include <Arduino.h>

#include "app/DeviceApp.hpp"

namespace {

// Holds the single application object that coordinates the firmware modules.
NaTrack::DeviceApp app{};

} // namespace

void setup() {
    // Run the app initialization once after reset or power-up.
    app.begin();
}

void loop() {
    // Run the app loop continuously without using delay().
    app.update();
}