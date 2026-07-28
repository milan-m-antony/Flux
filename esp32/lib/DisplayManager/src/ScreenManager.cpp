#include "ScreenManager.hpp"

namespace NaTrack {

void ScreenManager::drawScreen(Adafruit_SSD1306& display, ScreenId screen, int16_t offsetX,
                              const DeviceState& state, int settingsIndex) noexcept {
    switch (screen) {
    case ScreenId::Home:
        drawHomeScreen(display, offsetX, state);
        break;
    case ScreenId::Sodium:
        drawSodiumScreen(display, offsetX, state);
        break;
    case ScreenId::Temperature:
        drawTemperatureScreen(display, offsetX, state);
        break;
    case ScreenId::Battery:
        drawBatteryScreen(display, offsetX, state);
        break;
    case ScreenId::Bluetooth:
        drawBluetoothScreen(display, offsetX, state);
        break;
    case ScreenId::Settings:
        drawSettingsScreen(display, offsetX, settingsIndex);
        break;
    case ScreenId::About:
        drawAboutScreen(display, offsetX, state);
        break;
    default:
        break;
    }
}

void ScreenManager::drawHomeScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextColor(SSD1306_WHITE);
    display.setTextSize(1);
    
    // Label "SODIUM" (centered)
    display.setCursor(46 + offsetX, 14);
    display.print("SODIUM");

    // Large placeholder value ---
    ValueWidget::drawLarge(display, 38 + offsetX, 23, "---", "mmol/L");

    // Status badge: "Waiting For Sensor"
    // "Waiting For Sensor" is 18 chars * 6 = 108px. Center = (128-108)/2 = 10.
    StatusWidget::draw(display, 10 + offsetX, 41, "Waiting For Sensor", true);
}

void ScreenManager::drawSodiumScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawDroplet(display, 12 + offsetX, 18, 16, 24, false, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(40 + offsetX, 14);
    display.print("Sodium Level");

    display.setCursor(40 + offsetX, 24);
    display.print("Value: ---");

    display.setCursor(40 + offsetX, 33);
    display.print("Status: Waiting");

    display.setCursor(40 + offsetX, 42);
    display.print("Trend: --");
}

void ScreenManager::drawTemperatureScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawThermometer(display, 12 + offsetX, 18, 24, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(40 + offsetX, 16);
    display.print("Temperature");

    char tempStr[16];
    dtostrf(state.temperature, 4, 1, tempStr);
    strcat(tempStr, " C");
    display.setTextSize(2);
    display.setCursor(40 + offsetX, 26);
    display.print(tempStr);

    display.setTextSize(1);
    display.setCursor(40 + offsetX, 43);
    display.print("Status: Stable");
}

void ScreenManager::drawBatteryScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawBattery(display, 10 + offsetX, 22, 24, 12, 0, false, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    
    display.setCursor(45 + offsetX, 14);
    display.print("Battery");
    
    display.setCursor(45 + offsetX, 23);
    display.print("Not Connected");

    display.setCursor(45 + offsetX, 32);
    display.print("Voltage: --");

    display.setCursor(45 + offsetX, 41);
    display.print("Charging: No");
}

void ScreenManager::drawBluetoothScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    IconLibrary::drawBluetooth(display, 12 + offsetX, 18, 24, false, true, SSD1306_WHITE);

    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    
    display.setCursor(40 + offsetX, 14);
    display.print("Bluetooth");

    display.setCursor(40 + offsetX, 23);
    display.print("Disconnected");

    display.setCursor(40 + offsetX, 32);
    display.print("Name: ");
    display.print(state.bluetoothName);

    display.setCursor(40 + offsetX, 41);
    display.print("RSSI: --");
}

void ScreenManager::drawSettingsScreen(Adafruit_SSD1306& display, int16_t offsetX, int settingsIndex) noexcept {
    const char* menuItems[] = {
        "Brightness",
        "Sleep Settings",
        "Calibration",
        "Bluetooth Pair",
        "Factory Reset"
    };

    int startIndex = 0;
    if (settingsIndex >= 2) {
        startIndex = settingsIndex - 1;
        if (startIndex + 2 >= 5) {
            startIndex = 2;
        }
    }

    for (int i = 0; i < 3; i++) {
        int itemIdx = startIndex + i;
        int16_t yPos = 14 + i * 12;
        bool isSelected = (itemIdx == settingsIndex);

        if (isSelected) {
            display.fillRect(2 + offsetX, yPos - 1, 124, 11, SSD1306_WHITE);
            display.setTextColor(SSD1306_BLACK);
        } else {
            display.setTextColor(SSD1306_WHITE);
        }

        display.setCursor(6 + offsetX, yPos);
        display.print(menuItems[itemIdx]);
    }
}

void ScreenManager::drawAboutScreen(Adafruit_SSD1306& display, int16_t offsetX, const DeviceState& state) noexcept {
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    
    display.setCursor(10 + offsetX, 14);
    display.print("NaTrack Prototype");

    display.setCursor(10 + offsetX, 24);
    display.print("HW: ESP32 DevKitV1");

    display.setCursor(10 + offsetX, 34);
    display.print("FW: ");
    display.print(state.firmwareVersion);

    display.setCursor(10 + offsetX, 43);
    display.print("Build: ");
    display.print(__DATE__);
}

} // namespace NaTrack
