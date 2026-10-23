#include "ScreenManager.hpp"
#include "IconLibrary.hpp"

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

const char* getUnitSymbol(TempUnit unit) noexcept {
    return (unit == TempUnit::Fahrenheit) ? "F" : "C";
}

} // namespace

void ScreenManager::drawScreen(Adafruit_SSD1306& display, ScreenId screen, int16_t offsetX,
                              const DeviceState& state, int settingsIndex) noexcept {
    switch (screen) {
    case ScreenId::Dashboard:
        drawDashboard(display, offsetX, state);
        break;
    case ScreenId::LiveMonitor:
        drawLiveMonitor(display, offsetX, state);
        break;
    case ScreenId::Environment:
        drawEnvironment(display, offsetX, state);
        break;
    case ScreenId::Liquid:
        drawLiquid(display, offsetX, state);
        break;
    case ScreenId::Fermentation:
        drawFermentation(display, offsetX, state);
        break;
    case ScreenId::StatisticsTimer:
        drawStatisticsTimer(display, offsetX, state);
        break;
    case ScreenId::SystemStatus:
        drawSystemStatus(display, offsetX, state);
        break;
    case ScreenId::Settings:
        drawSettingsScreen(display, offsetX, state, settingsIndex);
        break;
    default:
        break;
    }
}

void ScreenManager::drawDashboard(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextColor(SSD1306_WHITE);
    display.setTextSize(1);
    
    // Header title for screen
    display.setCursor(38 + offsetX, 13);
    display.print("DASHBOARD");

    // Format current temperature
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    char uBuf[8];
    snprintf(uBuf, sizeof(uBuf), " %s", getUnitSymbol(state.preferredUnit));

    // Large main temperature reading
    ValueWidget::drawLarge(display, 28 + offsetX, 23, tBuf, uBuf);

    // Profile badge + Status
    const char* profileTag = "[LIVE]";
    if (state.activeProfile == ProfileMode::Environment) profileTag = "[ENV]";
    else if (state.activeProfile == ProfileMode::Liquid) profileTag = "[LIQ]";
    else if (state.activeProfile == ProfileMode::Fermentation) profileTag = "[FERM]";

    display.setTextSize(1);
    display.setCursor(8 + offsetX, 40);
    display.print(profileTag);

    // Status badge: Online, Disconnected, or Threshold Alert
    if (!state.sensorConnected) {
        StatusWidget::draw(display, 52 + offsetX, 39, "DISCONNECTED", true);
    } else if (state.isThresholdAlert) {
        StatusWidget::draw(display, 56 + offsetX, 39, "ALERT: LIMIT", true);
    } else {
        StatusWidget::draw(display, 66 + offsetX, 39, "ONLINE", false);
    }
}

void ScreenManager::drawLiveMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawThermometer(display, 8 + offsetX, 16, 26, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(34 + offsetX, 13);
    display.print("LIVE MONITOR");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    char uBuf[8];
    snprintf(uBuf, sizeof(uBuf), " %s", getUnitSymbol(state.preferredUnit));

    display.setTextSize(2);
    display.setCursor(34 + offsetX, 23);
    display.print(tBuf);
    display.setTextSize(1);
    display.print(uBuf);

    // Sensor & Range details
    display.setTextSize(1);
    display.setCursor(34 + offsetX, 40);
    if (!state.sensorConnected) {
        display.print("Probe: DISCONNECTED");
    } else {
        char minBuf[10];
        char maxBuf[10];
        formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));
        formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
        display.printf("L:%s H:%s", minBuf, maxBuf);
    }
}

void ScreenManager::drawEnvironment(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(20 + offsetX, 13);
    display.print("ENVIRONMENT (AIR)");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));

    display.setTextSize(2);
    display.setCursor(28 + offsetX, 23);
    display.print(tBuf);
    display.setTextSize(1);
    display.printf(" %s", getUnitSymbol(state.preferredUnit));

    display.setCursor(8 + offsetX, 40);
    display.printf("Range: %.0f-%.0f %s", 
                   state.preferredUnit == TempUnit::Fahrenheit ? (state.lowThreshold * 9/5 + 32) : state.lowThreshold,
                   state.preferredUnit == TempUnit::Fahrenheit ? (state.highThreshold * 9/5 + 32) : state.highThreshold,
                   getUnitSymbol(state.preferredUnit));
}

void ScreenManager::drawLiquid(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawDroplet(display, 8 + offsetX, 17, 16, 24, false, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(32 + offsetX, 13);
    display.print("LIQUID PROBE");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));

    display.setTextSize(2);
    display.setCursor(32 + offsetX, 23);
    display.print(tBuf);
    display.setTextSize(1);
    display.printf(" %s", getUnitSymbol(state.preferredUnit));

    display.setCursor(32 + offsetX, 40);
    if (!state.sensorConnected) {
        display.print("PROBE DISCONNECTED");
    } else {
        display.print("Submerged: Normal");
    }
}

void ScreenManager::drawFermentation(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(24 + offsetX, 13);
    display.print("FERMENTATION");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));

    display.setTextSize(2);
    display.setCursor(24 + offsetX, 23);
    display.print(tBuf);
    display.setTextSize(1);
    display.printf(" %s", getUnitSymbol(state.preferredUnit));

    display.setCursor(8 + offsetX, 40);
    if (state.isThresholdAlert) {
        display.print("WARN: OUT OF BAND!");
    } else {
        unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
        unsigned long h = elapsedSec / 3600UL;
        unsigned long m = (elapsedSec % 3600UL) / 60UL;
        display.printf("Band: 18-24C | %02luh%02lum", h, m);
    }
}

void ScreenManager::drawStatisticsTimer(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(18 + offsetX, 13);
    display.print("STATS & TIMER");

    // Monotonic session timer (HH:MM:SS)
    unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
    unsigned long h = elapsedSec / 3600UL;
    unsigned long m = (elapsedSec % 3600UL) / 60UL;
    unsigned long s = elapsedSec % 60UL;

    char minBuf[10];
    char maxBuf[10];
    char avgBuf[10];
    formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));
    formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
    formatTemperature(state.avgTemperature, state.preferredUnit, avgBuf, sizeof(avgBuf));

    display.setCursor(8 + offsetX, 23);
    display.printf("T: %02lu:%02lu:%02lu N:%u", h, m, s, state.sampleCount);

    display.setCursor(8 + offsetX, 32);
    display.printf("Min: %s %s", minBuf, getUnitSymbol(state.preferredUnit));

    display.setCursor(8 + offsetX, 41);
    display.printf("Max: %s Avg: %s", maxBuf, avgBuf);
}

void ScreenManager::drawSystemStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(20 + offsetX, 13);
    display.print("SYSTEM STATUS");

    // Row 1: BLE & Sensor
    display.setCursor(6 + offsetX, 23);
    display.printf("BLE: %s", state.bluetoothConnected ? "Connected" : "Advertising");

    // Row 2: Cloud Sync & Pending records
    display.setCursor(6 + offsetX, 32);
    display.printf("Cloud: %s | Pnd: %u", state.supabaseSyncState, state.pendingOfflineRecords);

    // Row 3: Uptime & Heap
    unsigned long uptimeMin = millis() / 60000UL;
    uint32_t freeHeapKb = ESP.getFreeHeap() / 1024U;
    display.setCursor(6 + offsetX, 41);
    display.printf("Up: %lum | Heap: %luK", uptimeMin, freeHeapKb);
}

void ScreenManager::drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(38 + offsetX, 13);
    display.print("SETTINGS");

    const char* options[4] = {
        (state.preferredUnit == TempUnit::Celsius) ? "Unit: [ Celsius ]" : "Unit: [ Fahrenheit ]",
        (state.activeProfile == ProfileMode::Live) ? "Mode: [ Live ]" :
        (state.activeProfile == ProfileMode::Environment) ? "Mode: [ Environ ]" :
        (state.activeProfile == ProfileMode::Liquid) ? "Mode: [ Liquid ]" : "Mode: [ Ferment ]",
        "Action: [ Reset Stats ]",
        "Target: [ 18-28 C ]"
    };

    for (int i = 0; i < 3; i++) {
        int idx = (settingsIndex % 4);
        int y = 23 + (i * 9);
        if (i == 0) {
            // Highlight selected item
            display.fillRect(4 + offsetX, y - 1, 120, 9, SSD1306_WHITE);
            display.setTextColor(SSD1306_BLACK);
            display.setCursor(6 + offsetX, y);
            display.print(options[idx]);
            display.setTextColor(SSD1306_WHITE);
        } else {
            int nextIdx = (idx + i) % 4;
            display.setCursor(6 + offsetX, y);
            display.print(options[nextIdx]);
        }
    }
}

} // namespace Flux
