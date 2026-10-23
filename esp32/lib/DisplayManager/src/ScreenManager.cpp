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
    // startAngle = 135 deg (bottom-left), sweep = 270 deg over top to 45 deg (bottom-right)
    const float startAngle = 135.0f;
    const float sweepTotal = 270.0f;
    const int steps = 20;

    // 1. Inactive Background Track & Radial Ticks
    for (int i = 0; i <= steps; i++) {
        float angleDeg = startAngle + (sweepTotal * i / steps);
        float rad = angleDeg * 0.0174532925f;
        int16_t xOuter = cx + (int16_t)(cos(rad) * (radius + 2));
        int16_t yOuter = cy + (int16_t)(sin(rad) * (radius + 2));
        int16_t xInner = cx + (int16_t)(cos(rad) * (radius - 2));
        int16_t yInner = cy + (int16_t)(sin(rad) * (radius - 2));

        if (i % 4 == 0) {
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

        // Thumb knob indicator at arc endpoint
        float thumbRad = (startAngle + activeSweep) * 0.0174532925f;
        int16_t tx = cx + (int16_t)(cos(thumbRad) * radius);
        int16_t ty = cy + (int16_t)(sin(thumbRad) * radius);
        display.fillCircle(tx, ty, 2, SSD1306_WHITE);
    }

    // Central pivot marker
    display.fillCircle(cx, cy, 1, SSD1306_WHITE);
}

void drawSparkline(Adafruit_SSD1306& display, int16_t x, int16_t y, int16_t w, int16_t h,
                   const DeviceState& state) noexcept {
    display.drawRect(x, y, w, h, SSD1306_WHITE);

    if (state.historyCount < 2) {
        display.setTextSize(1);
        display.setCursor(x + 10, y + h / 2 - 3);
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
    // Gauge center (20, 31), radius 13
    float progress = 0.0f;
    if (state.sensorConnected && state.temperature > -50.0f) {
        progress = (state.temperature - 16.0f) / (40.0f - 16.0f);
        if (progress < 0.0f) progress = 0.0f;
        if (progress > 1.0f) progress = 1.0f;
    }
    drawArcGauge(display, 20 + offsetX, 31, 13, progress);

    // 2. Right Side: Title, Temperature, and Status
    display.setTextSize(1);
    display.setCursor(44 + offsetX, 13);
    display.print("DASHBOARD");

    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(44 + offsetX, 23);
    display.print(tBuf);

    // Small degree symbol and unit
    display.setTextSize(1);
    display.drawCircle(44 + 50 + offsetX, 24, 1, SSD1306_WHITE);
    display.setCursor(44 + 54 + offsetX, 23);
    display.print(getUnitSymbol(state.preferredUnit));

    // Status Pill
    if (!state.sensorConnected) {
        StatusWidget::draw(display, 44 + offsetX, 39, "DISCONNECT", true);
    } else if (state.isThresholdAlert) {
        StatusWidget::draw(display, 44 + offsetX, 39, "ALERT: HIGH", true);
    } else {
        StatusWidget::draw(display, 44 + offsetX, 39, "STABLE", false);
    }
}

void ScreenManager::drawLiveMonitor(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header Row
    display.setCursor(4 + offsetX, 13);
    display.print("LIVE MONITOR");

    // Realtime sparkline chart (X: 4 to 80, Y: 23 to 49)
    drawSparkline(display, 4 + offsetX, 23, 76, 26, state);

    // Right Column: Hi / Lo / Live
    char maxBuf[8], minBuf[8], curBuf[8];
    formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
    formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));
    formatTemperature(state.temperature, state.preferredUnit, curBuf, sizeof(curBuf));

    display.setCursor(84 + offsetX, 13);
    display.printf("%s%s", curBuf, getUnitSymbol(state.preferredUnit));

    display.setCursor(84 + offsetX, 25);
    display.printf("H:%s", maxBuf);

    display.setCursor(84 + offsetX, 34);
    display.printf("L:%s", minBuf);

    display.setCursor(84 + offsetX, 43);
    display.print("RT:REC");
}

void ScreenManager::drawEnvironment(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(8 + offsetX, 13);
    display.print("ENVIRONMENT [AIR]");

    // Large Reading
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(8 + offsetX, 23);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(8 + 50 + offsetX, 24, 1, SSD1306_WHITE);
    display.setCursor(8 + 54 + offsetX, 23);
    display.print(getUnitSymbol(state.preferredUnit));

    // Comfort Status Badge
    bool inBand = (state.temperature >= state.lowThreshold && state.temperature <= state.highThreshold);
    if (inBand) {
        StatusWidget::draw(display, 74 + offsetX, 23, "IN BAND", false);
    } else {
        StatusWidget::draw(display, 74 + offsetX, 23, "OUT BAND", true);
    }

    // Ambient Comfort Range Slider (10C to 40C)
    display.drawRect(8 + offsetX, 41, 112, 6, SSD1306_WHITE);
    // Low and High target markers
    int16_t lowMark = 8 + offsetX + (int16_t)((state.lowThreshold - 10.0f) * 112.0f / 30.0f);
    int16_t highMark = 8 + offsetX + (int16_t)((state.highThreshold - 10.0f) * 112.0f / 30.0f);
    if (lowMark >= 8 + offsetX && lowMark <= 120 + offsetX) {
        display.drawFastVLine(lowMark, 39, 10, SSD1306_WHITE);
    }
    if (highMark >= 8 + offsetX && highMark <= 120 + offsetX) {
        display.drawFastVLine(highMark, 39, 10, SSD1306_WHITE);
    }

    // Current temp pin indicator
    if (state.sensorConnected && state.temperature > -50.0f) {
        int16_t curMark = 8 + offsetX + (int16_t)((state.temperature - 10.0f) * 112.0f / 30.0f);
        if (curMark < 8 + offsetX) curMark = 8 + offsetX;
        if (curMark > 118 + offsetX) curMark = 118 + offsetX;
        display.fillRect(curMark - 1, 40, 3, 8, SSD1306_WHITE);
    }
}

void ScreenManager::drawLiquid(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextColor(SSD1306_WHITE);

    // Droplet Icon on left
    IconLibrary::drawDroplet(display, 8 + offsetX, 16, 14, 20, true, SSD1306_WHITE);
    // Subtle liquid surface ripple line
    display.drawLine(4 + offsetX, 41, 26 + offsetX, 41, SSD1306_WHITE);

    // Header
    display.setTextSize(1);
    display.setCursor(34 + offsetX, 13);
    display.print("LIQUID PROBE");

    // Reading
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(34 + offsetX, 23);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(34 + 50 + offsetX, 24, 1, SSD1306_WHITE);
    display.setCursor(34 + 54 + offsetX, 23);
    display.print(getUnitSymbol(state.preferredUnit));

    // Submersion & Safety Alerts
    if (!state.sensorConnected) {
        StatusWidget::draw(display, 34 + offsetX, 39, "DISCONNECTED", true);
    } else if (state.temperature >= 95.0f) {
        StatusWidget::draw(display, 34 + offsetX, 39, "BOILING ALERT!", true);
    } else if (state.temperature <= 1.0f) {
        StatusWidget::draw(display, 34 + offsetX, 39, "FREEZING WARN!", true);
    } else {
        display.setCursor(34 + offsetX, 40);
        display.print("Submerged: Normal");
    }
}

void ScreenManager::drawFermentation(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(8 + offsetX, 13);
    display.print("FERMENTATION");

    // Big Temp
    char tBuf[16];
    formatTemperature(state.temperature, state.preferredUnit, tBuf, sizeof(tBuf));
    display.setTextSize(2);
    display.setCursor(8 + offsetX, 23);
    display.print(tBuf);

    display.setTextSize(1);
    display.drawCircle(8 + 50 + offsetX, 24, 1, SSD1306_WHITE);
    display.setCursor(8 + 54 + offsetX, 23);
    display.print(getUnitSymbol(state.preferredUnit));

    // Target 18-24C Yeast Band
    bool inYeastBand = (state.temperature >= 18.0f && state.temperature <= 24.0f);
    if (inYeastBand) {
        StatusWidget::draw(display, 72 + offsetX, 23, "IN 18-24C", false);
    } else {
        StatusWidget::draw(display, 72 + offsetX, 23, "OUT BAND!", true);
    }

    // Active Fermentation Batch Duration
    unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
    unsigned long h = elapsedSec / 3600UL;
    unsigned long m = (elapsedSec % 3600UL) / 60UL;
    display.setCursor(8 + offsetX, 40);
    display.printf("Batch: %02luh %02lum | Yeast OK", h, m);
}

void ScreenManager::drawStatisticsTimer(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(6 + offsetX, 13);
    display.print("STATS & TIMER");
    display.setCursor(84 + offsetX, 13);
    display.print("[OK:RST]");

    // Monotonic session timer
    unsigned long elapsedSec = (millis() - state.sessionStartMs) / 1000UL;
    unsigned long h = elapsedSec / 3600UL;
    unsigned long m = (elapsedSec % 3600UL) / 60UL;
    unsigned long s = elapsedSec % 60UL;

    char minBuf[8], maxBuf[8], avgBuf[8];
    formatTemperature(state.minTemperature, state.preferredUnit, minBuf, sizeof(minBuf));
    formatTemperature(state.maxTemperature, state.preferredUnit, maxBuf, sizeof(maxBuf));
    formatTemperature(state.avgTemperature, state.preferredUnit, avgBuf, sizeof(avgBuf));

    display.setCursor(6 + offsetX, 23);
    display.printf("T: %02lu:%02lu:%02lu  N:%u", h, m, s, state.sampleCount);

    display.setCursor(6 + offsetX, 32);
    display.printf("Min:%s  Avg:%s", minBuf, avgBuf);

    display.setCursor(6 + offsetX, 41);
    display.printf("Max:%s %s", maxBuf, getUnitSymbol(state.preferredUnit));
}

void ScreenManager::drawSystemStatus(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(6 + offsetX, 13);
    display.print("SYSTEM STATUS");
    display.setCursor(84 + offsetX, 13);
    display.print("[ONLINE]");

    // Row 1: BLE & RSSI
    display.setCursor(6 + offsetX, 23);
    display.printf("BLE: %s (%ddBm)", 
                   state.bluetoothConnected ? "Connected" : "Advertising", 
                   state.signalStrength);

    // Row 2: Battery & Charging
    display.setCursor(6 + offsetX, 32);
    display.printf("Bat: %.2fV (%d%%) %s", 
                   state.batteryVoltage, 
                   state.batteryPercentage,
                   state.charging ? "[CHG]" : "");

    // Row 3: Uptime & Free Heap
    unsigned long uptimeMin = millis() / 60000UL;
    uint32_t freeHeapKb = ESP.getFreeHeap() / 1024U;
    display.setCursor(6 + offsetX, 41);
    display.printf("Up: %lum | Heap: %luK", uptimeMin, freeHeapKb);
}

void ScreenManager::drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state, int settingsIndex) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    display.setCursor(6 + offsetX, 13);
    display.print("SETTINGS [PRESS OK]");

    const char* options[4] = {
        (state.preferredUnit == TempUnit::Celsius) ? "Unit: [ Celsius ]" : "Unit: [ Fahrenheit ]",
        (state.activeProfile == ProfileMode::Live) ? "Mode: [ Live Monitor ]" :
        (state.activeProfile == ProfileMode::Environment) ? "Mode: [ Environment ]" :
        (state.activeProfile == ProfileMode::Liquid) ? "Mode: [ Liquid Probe ]" : "Mode: [ Fermentation ]",
        "Target: [ 18C - 28C ]",
        "Action: [ Reset Stats ]"
    };

    for (int i = 0; i < 3; i++) {
        int idx = (settingsIndex % 4);
        int y = 23 + (i * 9);
        if (i == 0) {
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
