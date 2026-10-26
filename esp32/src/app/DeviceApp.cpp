#include "DeviceApp.hpp"

namespace Flux {

void DeviceApp::begin() noexcept {
    // 1. Output concise production boot logs
    Serial.begin(Config::SerialBaudRate);
    Serial.println("Flux Telemetry Booting...");

    // Give managers static lifetimes
    static StatusReporter reporter{Serial, Config::StatusReportIntervalMs};
    static ButtonManager buttons{};
    static BluetoothManager bluetooth{};
    static PowerManager power{};
    static SensorManager sensors{};
    static BatteryManager battery{};
    static SettingsManager settings{};
    static NavigationManager navigation{};
    static UIManager uiManager{};
    static DisplayManager displayManager{};
    static BuzzerManager buzzer{};

    // Wire up listeners
    buttons.setListener(&navigation);

    // Save references to member pointers
    statusReporter_ = &reporter;
    buttonManager_ = &buttons;
    bluetoothManager_ = &bluetooth;
    powerManager_ = &power;
    sensorManager_ = &sensors;
    batteryManager_ = &battery;
    settingsManager_ = &settings;
    navManager_ = &navigation;
    uiManager_ = &uiManager;
    displayManager_ = &displayManager;
    buzzerManager_ = &buzzer;

    // Initialize state & managers
    statusReporter_->begin();
    buttonManager_->begin();
    bluetoothManager_->begin(&state_);
    powerManager_->begin();
    sensorManager_->begin(&state_);
    batteryManager_->begin(&state_);
    settingsManager_->begin(&state_);
    navManager_->begin(&state_);
    uiManager_->begin(navManager_);
    buzzerManager_->begin(&state_);

    // Begin DisplayManager, which handles Low-level SSD1306 and pacing
    displayManager_->begin(&state_, uiManager_, navManager_);

    Serial.println("Display Ready");
    Serial.println("Buttons Ready");
    Serial.println("Temperature Ready");
    Serial.println("Buzzer Ready (GPIO 25)");
    Serial.println("System Ready");
}

void DeviceApp::update() noexcept {
    if (buttonManager_ != nullptr) {
        buttonManager_->update();
    }
    if (navManager_ != nullptr) {
        navManager_->update();
    }
    if (sensorManager_ != nullptr) {
        sensorManager_->update();
    }
    if (batteryManager_ != nullptr) {
        batteryManager_->update();
    }
    if (bluetoothManager_ != nullptr) {
        bluetoothManager_->update();
    }
    if (displayManager_ != nullptr) {
        displayManager_->update();
    }
    if (statusReporter_ != nullptr) {
        statusReporter_->update();
        static unsigned long lastDiagMs = 0;
        if (millis() - lastDiagMs >= 1000) {
            lastDiagMs = millis();
            Serial.printf("[DIAG] Temp=%.2f C | Lo=%.1f Hi=%.1f | Alert=%d | BuzzerSounding=%d | buzzerEnabled=%d | Status=%s\n",
                          state_.temperature, state_.lowThreshold, state_.highThreshold,
                          state_.isThresholdAlert, buzzerManager_ ? buzzerManager_->isSounding() : -1,
                          state_.buzzerEnabled, state_.sensorStatus ? state_.sensorStatus : "null");
        }
    }
    if (powerManager_ != nullptr) {
        powerManager_->update();
    }
    if (settingsManager_ != nullptr) {
        settingsManager_->update();
    }
    if (buzzerManager_ != nullptr) {
        buzzerManager_->update();
    }
}

} // namespace Flux
