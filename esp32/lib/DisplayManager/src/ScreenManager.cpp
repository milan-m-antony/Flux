#include "ScreenManager.hpp"
#include "IconLibrary.hpp"
#include <WiFi.h>
#include <math.h>

namespace Flux {

namespace {

void formatTemperature(float tempC, TempUnit unit, char* out, size_t outSize) noexcept {
    if (tempC < -50.0f || tempC > 125.0f) {
        snprintf(out, outSize, "--.-");
        return;
    }
    if (unit == TempUnit::Fahrenheit) {
        float tempF = (tempC * 9.0f / 5.0f) + 32.0f;
        dtostrf(tempF, 4, 1, out);
    } else {
        dtostrf(tempC, 4, 1, out);
    }
}

} // namespace

void ScreenManager::drawScreen(Adafruit_SSD1306& display, ScreenId screen, int16_t offsetX,
                              const DeviceState& state, int settingsIndex) noexcept {
    switch (screen) {
    case ScreenId::MainMonitor:
        drawMainMonitor(display, offsetX, state);
        break;
    case ScreenId::ModeSelection:
        drawModeSelection(display, offsetX, state, settingsIndex);
        break;
    case ScreenId::Settings:
        drawSettings(display, offsetX, state, settingsIndex);
        break;
    case ScreenId::Preferences:
        drawPreferences(display, offsetX, state, settingsIndex);
        break;
    case ScreenId::DeviceInfo:
        drawDeviceInfo(display, offsetX, state);
        break;
    case ScreenId::WifiStatus:
        drawWifiStatus(display, offsetX, state);
        break;
    case ScreenId::BluetoothStatus:
        drawBluetoothStatus(display, offsetX, state);
        break;
    default:
        drawMainMonitor(display, offsetX, state);
        break;
    }
}

void ScreenManager::drawMainMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    char timeBuf[16];
    state.getFormattedTime(timeBuf, sizeof(timeBuf));

    // Offline / Probe Disconnected State (Image 4 Screen 4 & Image 1 Screen 9)
    if (!state.sensorConnected) {
        IconLibrary::drawBigThermometer(display, 10 + offsetX, 19, SSD1306_WHITE);

        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(38 + offsetX, 19);
        display.print("Temperature");

        display.setTextSize(2);
        display.setCursor(38 + offsetX, 29);
        display.print("--.-");

        display.drawCircle(92 + offsetX, 31, 1, SSD1306_WHITE);
        display.setCursor(98 + offsetX, 29);
        display.print(state.preferredUnit == TempUnit::Fahrenheit ? "F" : "C");

        IconLibrary::drawBottomPill(display, 8 + offsetX, 48, 112, 14, "Offline", timeBuf, SSD1306_WHITE);
        return;
    }

    // Active Profile Layouts (Image 3 Screens 1-4)
    const char* titleStr = "Live Monitor";
    switch (state.activeProfile) {
    case ProfileMode::Live:
        IconLibrary::drawBigThermometer(display, 10 + offsetX, 19, SSD1306_WHITE);
        titleStr = "Live Monitor";
        break;
    case ProfileMode::Environment:
        IconLibrary::drawHouseThermometer(display, 8 + offsetX, 20, SSD1306_WHITE);
        titleStr = "Environment";
        break;
    case ProfileMode::Liquid:
        IconLibrary::drawDropletThermometer(display, 10 + offsetX, 20, SSD1306_WHITE);
        titleStr = "Liquid Monitor";
        break;
    case ProfileMode::Fermentation:
        IconLibrary::drawFermenterBottle(display, 9 + offsetX, 20, SSD1306_WHITE);
        titleStr = "Fermentation";
        break;
    default:
        IconLibrary::drawBigThermometer(display, 10 + offsetX, 19, SSD1306_WHITE);
        titleStr = "Temperature";
        break;
    }

    // Check if an alert is sending / recent (within 4s of triggering)
    bool alertSendingAnim = state.isThresholdAlert && ((millis() - state.alertSentAnimMs) < 4000UL);
    bool alertPulse = state.isThresholdAlert && (((millis() / 350) % 2) != 0);

    // Title Text or Animated Alert Indicator
    display.setTextSize(1);
    if (alertSendingAnim && alertPulse) {
        display.setTextColor(SSD1306_BLACK, SSD1306_WHITE);
        display.setCursor(38 + offsetX, 19);
        display.print("! ALERT SENT !");
        display.setTextColor(SSD1306_WHITE);
    } else {
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(38 + offsetX, 19);
        display.print(titleStr);
    }

    // Large Temperature Readout (e.g., 28.5 °C)
    char tempBuf[12];
    formatTemperature(state.temperature, state.preferredUnit, tempBuf, sizeof(tempBuf));
    display.setTextSize(2);
    display.setCursor(38 + offsetX, 29);
    display.print(tempBuf);

    // Degree Circle & Unit
    int16_t degX = 38 + offsetX + strlen(tempBuf) * 12 + 2;
    display.drawCircle(degX, 31, 1, SSD1306_WHITE);
    display.setTextSize(2);
    display.setCursor(degX + 5, 29);
    display.print(state.preferredUnit == TempUnit::Fahrenheit ? "F" : "C");

    // Pulsing Exclamation Mark during alert
    if (state.isThresholdAlert && alertPulse) {
        display.fillRoundRect(114 + offsetX, 29, 10, 15, 2, SSD1306_WHITE);
        display.setTextColor(SSD1306_BLACK);
        display.setTextSize(1);
        display.setCursor(117 + offsetX, 33);
        display.print("!");
        display.setTextColor(SSD1306_WHITE);
    }

    // Status State Determination (Normal / Warning / Critical)
    const char* statusStr = "Normal";
    if (state.temperature >= state.highThreshold + 5.0f) {
        statusStr = "Critical";
    } else if (state.temperature > state.highThreshold || state.temperature < state.lowThreshold) {
        statusStr = "Warning";
    }

    // Bottom Capsule Pill with Animated Alert Inversion
    if (state.isThresholdAlert && alertPulse) {
        // Inverted flashing capsule during threshold alert
        display.fillRoundRect(8 + offsetX, 48, 112, 14, 3, SSD1306_WHITE);
        display.setTextSize(1);
        display.setTextColor(SSD1306_BLACK);
        display.setCursor(16 + offsetX, 51);
        display.print(state.temperature > state.highThreshold ? "! HIGH" : "! LOW");
        display.drawFastVLine(66 + offsetX, 50, 10, SSD1306_BLACK);
        display.setCursor(72 + offsetX, 51);
        display.print(timeBuf);
        display.setTextColor(SSD1306_WHITE);
    } else {
        IconLibrary::drawBottomPill(display, 8 + offsetX, 48, 112, 14, statusStr, timeBuf, SSD1306_WHITE);
    }
}

void ScreenManager::drawModeSelection(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int selectionIndex) noexcept {
    const struct {
        const char* label;
        void (*drawIcon)(Adafruit_SSD1306&, int16_t, int16_t, uint16_t);
    } items[4] = {
        {"Live Monitor", IconLibrary::drawSmallBars},
        {"Environment",  IconLibrary::drawSmallHouse},
        {"Liquid",       IconLibrary::drawSmallDroplet},
        {"Fermentation", IconLibrary::drawSmallFlask}
    };

    int activeIdx = (selectionIndex % 4);
    int currentActiveProfile = static_cast<int>(state.activeProfile);

    for (int i = 0; i < 4; i++) {
        int16_t rowY = 18 + i * 11;
        bool selected = (i == activeIdx);
        bool isCurrentProfile = (i == currentActiveProfile);

        if (selected) {
            // Rounded selection highlight pill (inverted)
            display.fillRoundRect(6 + offsetX, rowY - 1, 116, 11, 2, SSD1306_WHITE);
        }

        uint16_t color = selected ? SSD1306_BLACK : SSD1306_WHITE;

        items[i].drawIcon(display, 8 + offsetX, rowY + 1, color);

        display.setTextSize(1);
        display.setTextColor(color);
        display.setCursor(20 + offsetX, rowY + 1);
        display.print(items[i].label);

        if (isCurrentProfile) {
            // Active checkmark indicator dot
            display.fillCircle(108 + offsetX, rowY + 4, 2, color);
        }
        IconLibrary::drawChevronRight(display, 116 + offsetX, rowY + 2, color);
    }
}

void ScreenManager::drawSettings(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept {
    const struct {
        const char* label;
        void (*drawIcon)(Adafruit_SSD1306&, int16_t, int16_t, uint16_t);
    } items[4] = {
        {"Wi-Fi Settings", IconLibrary::drawSmallWifi},
        {"Bluetooth",     IconLibrary::drawSmallBluetooth},
        {"Preferences",   IconLibrary::drawSmallGear},
        {"Device Info",   IconLibrary::drawSmallInfo}
    };

    int activeIdx = (settingsIndex % 4);

    for (int i = 0; i < 4; i++) {
        int16_t rowY = 18 + i * 11;
        bool selected = (i == activeIdx);

        if (selected) {
            // Rounded selection highlight pill (inverted)
            display.fillRoundRect(6 + offsetX, rowY - 1, 116, 11, 2, SSD1306_WHITE);
        }

        uint16_t color = selected ? SSD1306_BLACK : SSD1306_WHITE;

        items[i].drawIcon(display, 8 + offsetX, rowY + 1, color);

        display.setTextSize(1);
        display.setTextColor(color);
        display.setCursor(22 + offsetX, rowY + 1);
        display.print(items[i].label);

        IconLibrary::drawChevronRight(display, 116 + offsetX, rowY + 2, color);
    }
}

void ScreenManager::drawPreferences(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int prefIndex) noexcept {
    int activeIdx = (prefIndex % 4);

    char unitVal[8];
    snprintf(unitVal, sizeof(unitVal), "[ %s ]", (state.preferredUnit == TempUnit::Fahrenheit) ? "F" : "C");

    char highVal[12];
    snprintf(highVal, sizeof(highVal), "[ %.0fC ]", state.highThreshold);

    char lowVal[12];
    snprintf(lowVal, sizeof(lowVal), "[ %.0fC ]", state.lowThreshold);

    const char* buzzerVal = state.buzzerEnabled ? "[ ON ]" : "[ OFF ]";

    const struct {
        const char* label;
        const char* val;
    } rows[4] = {
        {"Unit",     unitVal},
        {"Alert Hi", highVal},
        {"Alert Lo", lowVal},
        {"Buzzer",   buzzerVal}
    };

    for (int i = 0; i < 4; i++) {
        int16_t rowY = 18 + i * 11;
        bool selected = (i == activeIdx);

        if (selected) {
            display.fillRoundRect(4 + offsetX, rowY - 1, 120, 11, 2, SSD1306_WHITE);
        }

        uint16_t color = selected ? SSD1306_BLACK : SSD1306_WHITE;
        display.setTextSize(1);
        display.setTextColor(color);
        display.setCursor(8 + offsetX, rowY + 1);
        display.print(rows[i].label);

        // Right aligned setting value
        int16_t valLen = strlen(rows[i].val);
        int16_t valX = 120 + offsetX - (valLen * 6);
        display.setCursor(valX, rowY + 1);
        display.print(rows[i].val);
    }
}

void ScreenManager::drawDeviceInfo(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    // Microchip Icon shifted left for optimal responsive clearance
    IconLibrary::drawMicrochip(display, 4 + offsetX, 20, SSD1306_WHITE);

    // Device Metadata with clean margins and visible uptime
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    display.setCursor(28 + offsetX, 20);
    display.printf("Device : %s", state.deviceName);

    display.setCursor(28 + offsetX, 32);
    display.printf("Firmw. : %s", state.firmwareVersion);

    char upBuf[16];
    state.getFormattedUptime(upBuf, sizeof(upBuf));
    display.setCursor(28 + offsetX, 44);
    display.printf("Uptime : %s", upBuf);
}

void ScreenManager::drawWifiStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    // Large Wi-Fi radar icon
    IconLibrary::drawBigWifi(display, 10 + offsetX, 24, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    if (state.wifiConnected) {
        display.setCursor(46 + offsetX, 27);
        display.print("Connected");

        display.setCursor(46 + offsetX, 40);
        String ipStr = WiFi.localIP().toString();
        if (ipStr.length() > 0 && ipStr != "0.0.0.0") {
            display.print(ipStr);
        } else {
            display.print("192.168.1.10");
        }
    } else {
        display.setCursor(46 + offsetX, 27);
        display.print("Connecting...");

        // Animated loader dots: ● ● ○
        int step = (millis() / 400) % 3;
        for (int d = 0; d < 3; d++) {
            int dx = 50 + offsetX + d * 10;
            if (d == step) {
                display.drawCircle(dx, 44, 2, SSD1306_WHITE);
            } else {
                display.fillCircle(dx, 44, 2, SSD1306_WHITE);
            }
        }
    }
}

void ScreenManager::drawBluetoothStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    // Large Bluetooth rune icon
    IconLibrary::drawBigBluetooth(display, 14 + offsetX, 22, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    display.setCursor(48 + offsetX, 24);
    display.print("Bluetooth");

    if (state.bluetoothConnected) {
        display.setCursor(48 + offsetX, 36);
        display.print("Connected");
    } else {
        display.setCursor(48 + offsetX, 34);
        display.print("Pairing...");

        // Animated loader dots: ● ● ○
        int step = (millis() / 400) % 3;
        for (int d = 0; d < 3; d++) {
            int dx = 50 + offsetX + d * 10;
            if (d == step) {
                display.drawCircle(dx, 47, 2, SSD1306_WHITE);
            } else {
                display.fillCircle(dx, 47, 2, SSD1306_WHITE);
            }
        }
    }
}

} // namespace Flux
