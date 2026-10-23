# Flux — Hardware Specification & Wiring Guide

## 1. System Overview

**Flux** is a smart telemetry system powered by the ESP32 microcontroller, reading precise temperature data via a digital 1-Wire probe and displaying real-time metrics on an SSD1306 OLED screen with interactive physical button navigation, Bluetooth Low Energy (BLE) streaming, and cloud synchronization.

```
                      +-------------------+
                      |   5V Micro-USB    |
                      +---------+---------+
                                |
                   +------------v------------+
                   |  ESP32-WROOM-32 Dev Kit  |
                   +----+-------+-------+----+
                        |       |       |
       +----------------+       |       +----------------+
       |                        |                        |
+------v------+          +------v------+          +------v------+
|  SSD1306    |          |   DS18B20   |          |  3x Buttons |
|  128x64 OLED|          | Temp Probe  |          | Navigation  |
|  (I2C)      |          |  (1-Wire)   |          |  (Digital)  |
+-------------+          +-------------+          +-------------+
```

---

## 2. Bill of Materials (BOM)

| Component | Specification | Quantity | Interface / Protocol | Pin Connections |
| :--- | :--- | :---: | :--- | :--- |
| **Microcontroller** | ESP32-WROOM-32 (38-pin or 30-pin DevKit) | 1 | Dual-Core 240MHz, 4MB Flash | Host Controller |
| **Temperature Sensor** | Maxim/Dallas DS18B20 Waterproof Probe | 1 | 1-Wire Digital (12-bit) | GPIO 4 (DQ) |
| **Pull-up Resistor** | 4.7 kΩ (1/4 W) | 1 | Pull-up for 1-Wire Bus | Between VCC (3.3V) & GPIO 4 |
| **Display** | 0.96-inch SSD1306 OLED (128x64, 4-pin) | 1 | I2C (Address: `0x3C`) | GPIO 21 (SDA), GPIO 22 (SCL) |
| **Navigation Buttons** | Momentary Push Buttons (Tactile) | 3 | Digital Input (`INPUT_PULLUP`) | GPIO 16 (L), GPIO 17 (OK), GPIO 18 (R) |
| **Power Source** | Micro-USB Cable / 5V DC 1A Adapter | 1 | USB Power Bus | ESP32 V5 / VIN |

---

## 3. Verified GPIO Pin Map

| ESP32 GPIO | Direction | Peripheral | Description | Hardware Configuration |
| :--- | :--- | :--- | :--- | :--- |
| **GPIO 4** | Bidirectional | DS18B20 Probe | 1-Wire Data Line (`ONE_WIRE_BUS`) | Requires external 4.7kΩ pull-up to 3.3V |
| **GPIO 21** | Output / Input | SSD1306 OLED | I2C Serial Data (`I2C_SDA`) | Integrated internal/module pull-up |
| **GPIO 22** | Output | SSD1306 OLED | I2C Serial Clock (`I2C_SCL`) | Integrated internal/module pull-up |
| **GPIO 16** | Input | Button 1 (Left / Prev) | Menu navigation left / decrement | Configured as `INPUT_PULLUP` (Active LOW) |
| **GPIO 17** | Input | Button 2 (OK / Select) | Select, toggle, confirm, reset stats | Configured as `INPUT_PULLUP` (Active LOW) |
| **GPIO 18** | Input | Button 3 (Right / Next) | Menu navigation right / increment | Configured as `INPUT_PULLUP` (Active LOW) |
| **3.3V** | Power Output | All Peripherals | Regulated 3.3V DC rail | Supplies OLED, DS18B20, & Pull-up |
| **GND** | Ground Rail | All Peripherals | Common System Ground | Shared ground connection |

---

## 4. Peripheral Specifications & Wiring

### 4.1 Maxim DS18B20 Waterproof Temperature Probe

- **Sensor Type:** Digital Silicon Temperature Sensor with 64-bit unique ROM address.
- **Operating Temperature Range:** -55°C to +125°C (-67°F to +257°F).
- **Measurement Accuracy:** ±0.5°C over the range of -10°C to +85°C.
- **Resolution:** Programmable 9 to 12 bits (Flux configures 12-bit for 0.0625°C precision).
- **Conversion Time:** ~750ms at 12-bit (handled non-blockingly via asynchronous timer).
- **Wire Color Code:**
  - **Red:** VCC (Connect to ESP32 **3.3V**)
  - **Black:** GND (Connect to ESP32 **GND**)
  - **Yellow / White / Blue:** Data DQ (Connect to ESP32 **GPIO 4**)
  - **Critical Notice:** A **4.7kΩ pull-up resistor** MUST be placed between the **VCC (3.3V)** wire and the **Data (GPIO 4)** wire. Without this resistor, the bus floats, leading to disconnected errors (`DEVICE_DISCONNECTED_C = -127°C`).

### 4.2 SSD1306 0.96-inch Monochrome OLED (128×64)

- **Driver IC:** Solomon Systech SSD1306.
- **Display Resolution:** 128 horizontal pixels × 64 vertical pixels.
- **Interface:** 2-wire I2C (`Wire.h`).
- **I2C Address:** `0x3C` (default; selectable to `0x3D` via rear jumper resistor).
- **Wiring:**
  - `VCC` -> ESP32 **3.3V**
  - `GND` -> ESP32 **GND**
  - `SDA` -> ESP32 **GPIO 21**
  - `SCL` -> ESP32 **GPIO 22**
- **Refresh Frequency:** Non-blocking 1Hz update loop (`DisplayManager.update(currentTime)`).

### 4.3 Tactile Push Buttons (3-Button Scheme)

- **Mode:** Active LOW logic with internal ESP32 pull-up resistors enabled (`pinMode(pin, INPUT_PULLUP)`).
- **Wiring:**
  - One leg of each button is connected directly to the assigned GPIO (**16, 17, 18**).
  - The opposite leg connects directly to common **GND**.
  - When pressed, the GPIO reads `LOW` (`0`). When released, it floats high to `3.3V` (`1`).
- **Debouncing:** Software debouncing with a 50ms stable transition window, managed by `ButtonDebouncer`.

---

## 5. Power & Thermal Considerations

1. **Power Consumption:**
   - ESP32 active + BLE advertising / connected: ~90–140 mA @ 5V.
   - SSD1306 OLED (50% pixels lit): ~15–20 mA.
   - DS18B20 active conversion: ~1.5 mA (idle: < 1 µA).
   - Total System Consumption: ~120–160 mA @ 5V (< 1 Watt).
2. **Thermal Isolation:**
   - The ESP32 MCU generates modest heat (~35°C–42°C under sustained radio load).
   - Because the DS18B20 is a probe on an external 1-meter cable, measurements are thermally isolated from the ESP32 enclosure and PCB traces.

---

## 6. Hardware Boundaries & Limitations

- **Sensor Modality:** The system contains a **temperature sensor only**. It does NOT have sensors for relative humidity, atmospheric pressure, pH, dissolved oxygen, or specific gravity.
- **Liquid Immersion:** The stainless steel probe tube is waterproof and suitable for ambient air, water, coolant, and general liquid monitoring. Avoid direct contact with concentrated mineral acids, strong bases, or harsh organic solvents that attack 304/316 stainless steel or silicone seals.
