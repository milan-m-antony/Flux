#include "ScreenManager.hpp"
#include "IconLibrary.hpp"
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

const char* getUnitSymbol(TempUnit unit) noexcept {
    return (unit == TempUnit::Fahrenheit) ? "F" : "C";
}

void drawArcGauge(Adafruit_SSD1306& display, int16_t cx, int16_t cy, int16_t radius, float progress) noexcept {
    // 270 degree sweep from 135 deg to 45 deg
    const float startAngle = 135.0f;
    const float sweepTotal = 270.0f;
    const int steps = 18;

    // 1. Inactive Background Track & Radial Ticks
    for (int i = 0; i <= steps; i++) {
        float angleDeg = startAngle + (sweepTotal * i / steps);
        float rad = angleDeg * 0.0174532925f;
        int16_t xOuter = cx + (int16_t)(cos(rad) * (radius + 2));
        int16_t yOuter = cy + (int16_t)(sin(rad) * (radius + 2));
        int16_t xInner = cx + (int16_t)(cos(rad) * (radius - 2));
        int16_t yInner = cy + (int16_t)(sin(rad) * (radius - 2));

        if (i % 3 == 0) {
            display.drawLine(xInner, yInner, xOuter, yOuter, SSD1306_WHITE);
        } else {
            int16_t xMid = cx + (int16_t)(cos(rad) * radius);
            int16_t yMid = cy + (int16_t)(sin(rad) * radius);
            display.drawPixel(xMid, yMid, SSD1306_WHITE);
        }
    }

    // 2. Active Arc Track
    if (progress > 0.02f) {
        if (progress > 1.0f) progress = 1.0f;
        float activeSweep = sweepTotal * progress;
        int activeSteps = (int)(steps * progress);
        if (activeSteps < 1) activeSteps = 1;

        int16_t prevX = cx + (int16_t)(cos(startAngle * 0.0174532925f) * radius);
        int16_t prevY = cy + (int16_t)(sin(startAngle * 0.0174532925f) * radius);

        for (int i = 1; i <= activeSteps; i++) {
            float angleDeg = startAngle + (activeSweep * i / activeSteps);
            float rad = angleDeg * 0.0174532925f;
            int16_t currX = cx + (int16_t)(cos(rad) * radius);
            int16_t currY = cy + (int16_t)(sin(rad) * radius);
            display.drawLine(prevX, prevY, currX, currY, SSD1306_WHITE);

            int16_t inX = cx + (int16_t)(cos(rad) * (radius - 1));
            int16_t inY = cy + (int16_t)(sin(rad) * (radius - 1));
            display.drawPixel(inX, inY, SSD1306_WHITE);

            prevX = currX;
            prevY = currY;
        }

        // Thumb indicator at tip
        float thumbRad = (startAngle + activeSweep) * 0.0174532925f;
        int16_t tx = cx + (int16_t)(cos(thumbRad) * radius);
        int16_t ty = cy + (int16_t)(sin(thumbRad) * radius);
        display.fillCircle(tx, ty, 2, SSD1306_WHITE);
    }

    // Center pivot
    display.fillCircle(cx, cy, 1, SSD1306_WHITE);
}

void drawSparkline(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h,
                   const DeviceState& state) noexcept {
    display.drawRect(x, y, w, h, SSD1306_WHITE);

    if (state.historyCount < 2) {
        display.setTextSize(1);
        display.setCursor(x + 8, y + h / 2 - 3);
        display.print("SAMPLING...");
        return;
    }

    float minH = state.minTemperature;
    float maxH = state.maxTemperature;
    if (minH < -50.0f) minH = state.temperature;
    if (maxH < -50.0f) maxH = state.temperature;
    float range = maxH - minH;
    if (range < 0.5f) range = 0.5f;

    size_t count = state.historyCount;
    size_t startIdx = (count < DeviceState::HistoryBufferSize) ? 0 : state.historyIndex;
    int16_t prevPx = -1;
    int16_t prevPy = -1;

    for (size_t i = 0; i < count; i++) {
        size_t ringIdx = (startIdx + i) % DeviceState::HistoryBufferSize;
        float val = state.history[ringIdx];
        int16_t px = x + 2 + (int16_t)((i * (w - 4)) / (count - 1));
        int16_t py = (y + h - 3) - (int16_t)(((val - minH) * (h - 6)) / range);

        if (prevPx >= 0) {
            display.drawLine(prevPx, prevPy, px, py, SSD1306_WHITE);
        }
        prevPx = px;
        prevPy = py;

        if (i == count - 1) {
            display.fillRect(px - 1, py - 1, 3, 3, SSD1306_WHITE);
        }
    }
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

    // 1. Mini Circular Arc Gauge (Matching mobile app's circular gauge)
    float progress = 0.0f;
    if (state.sensorConnected && state.temperature > -50.0f) {
        progress = (state.temperature - 16.0f) / (40.0f - 16.0f);
        if (progress < 0.0f) progress = 0.0f;
        if (progress > 1.0f) progress = 1.0f;
    }
    drawArcGauge(display, 24 + offsetX, 31, 12, progress);

    // 2. Right Side: Title, Temperature, and Status with comfortable spacing
    display.setTextSize(1);
    display.setCursor(48 + offsetX, 14);
    display.print("DASHBOARD");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(48 + offsetX, 25);
    display.print(tBuf);

    // Degree symbol and unit
    display.setTextSize(1);
    display.drawCircle(48 + 50 + offsetX, 26, 1, SSD1306_WHITE);
    display.setCursor(48 + 54 + offsetX, 25);
    display.print(getUnitSymbol(state.preferredUnit));

    // Status Indicator with clean dot
    display.setTextSize(1);
    display.fillCircle(50 + offsetX, 44, 2, SSD1306_WHITE);
    display.setCursor(56 + offsetX, 41);
    if (!state.sensorConnected) {
        display.print("NO PROBE");
    } else if (state.isThresholdAlert) {
        display.print("HIGH ALERT");
    } else {
        display.print("STABLE");
    }
}

void ScreenManager::drawLiveMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header Row: Title on left, current reading on right
    display.setCursor(4 + offsetX, 14);
    display.print("LIVE MONITOR");

    char curBuf[8];
    formatTemperature(state.temperature, state.preferredUnit, curBuf, sizeof(curBuf));
    display.setCursor(82 + offsetX, 14);
    display.printf("%s %s", curBuf, getUnitSymbol(state.preferredUnit));

    // Realtime sparkline chart (W=76, H=24)
    drawSparkline(display, 4 + offsetX, 24, 76, 24, state);

    // Right Column: Spacious 2 rows for High and Low peaks
    char maxBuf[8], minBuf[8];
    formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
    formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));

    display.setCursor(86 + offsetX, 27);
    display.printf("HI:%s", maxBuf);

    display.setCursor(86 + offsetX, 39);
    display.printf("LO:%s", minBuf);
}

void ScreenManager::drawEnvironment(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(8 + offsetX, 14);
    display.print("ENVIRONMENT [AIR]");

    // Large Reading
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(8 + offsetX, 25);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(8 + 50 + offsetX, 26, 1, SSD1306_WHITE);
    display.setCursor(8 + 54 + offsetX, 25);
    display.print(getUnitSymbol(state.preferredUnit));

    // Target range and status - comfortable breathing room
    display.setCursor(8 + offsetX, 42);
    display.print("Target: 18-28 C");

    bool inBand = (state.temperature >= state.lowThreshold && state.temperature <= state.highThreshold);
    display.setCursor(102 + offsetX, 42);
    display.print(inBand ? "[OK]" : "[WARN]");
}

void ScreenManager::drawLiquid(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextColor(SSD1306_WHITE);

    // Droplet Icon on left
    IconLibrary::drawDroplet(display, 10 + offsetX, 20, 12, 18, true, SSD1306_WHITE);

    // Header
    display.setTextSize(1);
    display.setCursor(32 + offsetX, 14);
    display.print("LIQUID PROBE");

    // Reading
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(32 + offsetX, 25);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(32 + 50 + offsetX, 26, 1, SSD1306_WHITE);
    display.setCursor(32 + 54 + offsetX, 25);
    display.print(getUnitSymbol(state.preferredUnit));

    // Submersion & Status
    display.setCursor(32 + offsetX, 42);
    if (!state.sensorConnected) {
        display.print("Probe Disconnected");
    } else if (state.temperature >= 95.0f) {
        display.print("ALERT: Boiling!");
    } else if (state.temperature <= 1.0f) {
        display.print("WARN: Freezing!");
    } else {
        display.print("Submerged: Normal");
    }
}

void ScreenManager::drawFermentation(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(8 + offsetX, 14);
    display.print("FERMENTATION PROFILE");

    // Big Temp
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(8 + offsetX, 25);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(8 + 50 + offsetX, 26, 1, SSD1306_WHITE);
    display.setCursor(8 + 54 + offsetX, 25);
    display.print(getUnitSymbol(state.preferredUnit));

    // Target 18-24C Yeast Band
    bool inYeastBand = (state.temperature >= 18.0f && state.temperature <= 24.0f);
    display.setCursor(68 + offsetX, 28);
    display.print(inYeastBand ? "[18-24C OK]" : "[OUT BAND!]");

    // Active Fermentation Batch Duration
    unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
    unsigned long h = elapsedSec / 3600UL;
    unsigned long m = (elapsedSec % 3600UL) / 60UL;
    display.setCursor(8 + offsetX, 42);
    display.printf("Batch Time: %02luh %02lum", h, m);
}

void ScreenManager::drawStatisticsTimer(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Monotonic session timer duration
    unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
    unsigned long h = elapsedSec / 3600UL;
    unsigned long m = (elapsedSec % 3600UL) / 60UL;
    unsigned long s = elapsedSec % 60UL;

    char minBuf[8], maxBuf[8], avgBuf[8];
    formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));
    formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
    formatTemperature(state.avgTemperature, state.preferredUnit, avgBuf, sizeof(avgBuf));

    // Row 1: Session Timer & Sample count
    display.setCursor(6 + offsetX, 14);
    display.printf("TIMER: %02lu:%02lu:%02lu (N:%u)", h, m, s, state.sampleCount);

    // Row 2: Min & Max
    display.setCursor(6 + offsetX, 27);
    display.printf("MIN:%s  MAX:%s %s", minBuf, maxBuf, getUnitSymbol(state.preferredUnit));

    // Row 3: Avg & Reset hint
    display.setCursor(6 + offsetX, 40);
    display.printf("AVG:%s %s  [OK:RST]", avgBuf, getUnitSymbol(state.preferredUnit));
}

void ScreenManager::drawSystemStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Row 1: BLE & RSSI
    display.setCursor(6 + offsetX, 14);
    display.printf("BLE: %s (%ddBm)", 
                   state.bluetoothConnected ? "CONNECTED" : "ADVERTISING", 
                   state.signalStrength);

    // Row 2: WiFi & Free Heap
    uint32_t freeHeapKb = ESP.getFreeHeap() / 1024U;
    display.setCursor(6 + offsetX, 27);
    display.printf("WIFI: %s | HEAP: %luK", 
                   state.wifiConnected ? "CONNECTED" : "READY", 
                   freeHeapKb);

    // Row 3: Device Uptime
    unsigned long uptimeMin = millis() / 60000UL;
    unsigned long upH = uptimeMin / 60UL;
    unsigned long upM = uptimeMin % 60UL;
    display.setCursor(6 + offsetX, 40);
    display.printf("UPTIME: %02luh %02lum", upH, upM);
}

void ScreenManager::drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(6 + offsetX, 14);
    display.print("SETTINGS  [PRESS OK]");

    const char* options[4] = {
        (state.preferredUnit == TempUnit::Celsius) ? "Unit: [ Celsius ]" : "Unit: [ Fahrenheit ]",
        (state.activeProfile == ProfileMode::Live) ? "Mode: [ Live Monitor ]" :
        (state.activeProfile == ProfileMode::Environment) ? "Mode: [ Environment ]" :
        (state.activeProfile == ProfileMode::Liquid) ? "Mode: [ Liquid Probe ]" : "Mode: [ Fermentation ]",
        "Target: [ 18C - 28C ]",
        "Action: [ Reset Stats ]"
    };

    int curIdx = (settingsIndex % 4);
    int nextIdx = (curIdx + 1) % 4;

    // Row 1: Selected Item with cursor
    display.setCursor(6 + offsetX, 27);
    display.printf("> %s", options[curIdx]);

    // Row 2: Next Item
    display.setCursor(6 + offsetX, 40);
    display.printf("  %s", options[nextIdx]);
}

} // namespace Flux
