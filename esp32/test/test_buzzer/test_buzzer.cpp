#include <unity.h>
#include <Arduino.h>
#include "DeviceState.hpp"
#include "BuzzerManager.hpp"
#include "FluxConfig.hpp"

using namespace Flux;

static DeviceState testState;
static BuzzerManager buzzer(Config::BuzzerPin);

void setUp(void) {
    testState = DeviceState{};
    testState.lowThreshold = 18.0f;
    testState.highThreshold = 28.0f;
    testState.buzzerEnabled = true;
    testState.sensorConnected = true;
    testState.isThresholdAlert = false;
    testState.temperature = 22.0f;
    testState.sensorStatus = "Normal";
    buzzer.begin(&testState);
}

void tearDown(void) {
    buzzer.silence();
}

// Case 1: Boot with a normal temperature -> no sound
void test_boot_with_normal_temperature_no_sound(void) {
    testState.temperature = 22.0f;
    testState.isThresholdAlert = false;
    testState.sensorConnected = true;
    testState.buzzerEnabled = true;

    buzzer.begin(&testState);
    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 2: Normal valid temperature -> no sound
void test_normal_valid_temperature_no_sound(void) {
    testState.temperature = 24.5f;
    testState.isThresholdAlert = false;
    testState.sensorConnected = true;

    for (int i = 0; i < 5; i++) {
        buzzer.update();
        delay(10);
    }

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 3: Temperature crosses the high threshold -> alarm sounds
void test_temperature_crosses_high_threshold_alarm_sounds(void) {
    testState.temperature = 31.0f; // > highThreshold (28.0C)
    testState.isThresholdAlert = true;
    testState.sensorStatus = "Threshold Alert";

    buzzer.update();

    TEST_ASSERT_TRUE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::AlertBeep), static_cast<uint8_t>(buzzer.getPattern()));
    TEST_ASSERT_EQUAL(0, buzzer.getPatternStep());
}

// Case 4: Temperature crosses the low threshold -> alarm sounds
void test_temperature_crosses_low_threshold_alarm_sounds(void) {
    testState.temperature = 14.0f; // < lowThreshold (18.0C)
    testState.isThresholdAlert = true;
    testState.sensorStatus = "Threshold Alert";

    buzzer.update();

    TEST_ASSERT_TRUE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::AlertBeep), static_cast<uint8_t>(buzzer.getPattern()));
    TEST_ASSERT_EQUAL(0, buzzer.getPatternStep());
}

// Case 5: Temperature returns to valid normal range -> alarm stops immediately
void test_temperature_returns_to_normal_alarm_stops_immediately(void) {
    // 1. Alarm starts
    testState.temperature = 32.0f;
    testState.isThresholdAlert = true;
    buzzer.update();
    TEST_ASSERT_TRUE(buzzer.isSounding());

    // 2. Normal temperature restored
    testState.temperature = 23.0f;
    testState.isThresholdAlert = false;
    testState.sensorStatus = "Normal";
    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
    TEST_ASSERT_EQUAL(0, buzzer.getPatternStep());
}

// Case 6: Sensor disconnects during an active alarm -> no stale repeating alarm
void test_sensor_disconnects_during_alarm_no_stale_alarm(void) {
    // 1. Alarm active
    testState.temperature = 35.0f;
    testState.isThresholdAlert = true;
    buzzer.update();
    TEST_ASSERT_TRUE(buzzer.isSounding());

    // 2. Sensor disconnect event
    testState.sensorConnected = false;
    testState.isThresholdAlert = false;
    testState.sensorStatus = "Disconnected";
    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 7: Invalid sensor readings -> no false threshold alarm
void test_invalid_sensor_readings_no_false_threshold_alarm(void) {
    // Invalid reading: read fault, disconnected, or error code
    testState.sensorConnected = false;
    testState.isThresholdAlert = false;
    testState.sensorStatus = "Read Fault";
    testState.temperature = -127.0f; // DEVICE_DISCONNECTED_C

    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 8: Buzzer is toggled OFF during an alarm -> immediate silence
void test_buzzer_toggled_off_during_alarm_immediate_silence(void) {
    // 1. Alarm active
    testState.temperature = 33.0f;
    testState.isThresholdAlert = true;
    buzzer.update();
    TEST_ASSERT_TRUE(buzzer.isSounding());

    // 2. User/BLE disables buzzer
    testState.buzzerEnabled = false;
    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 9: Buzzer is toggled ON during normal conditions -> remains silent
void test_buzzer_toggled_on_during_normal_conditions_remains_silent(void) {
    testState.temperature = 22.0f;
    testState.isThresholdAlert = false;
    testState.buzzerEnabled = false;
    buzzer.update();
    TEST_ASSERT_FALSE(buzzer.isSounding());

    // Enable buzzer
    testState.buzzerEnabled = true;
    testState.requestBuzzerChirp = true; // Even if legacy chirp requested, it must be cleared without sound
    buzzer.update();

    TEST_ASSERT_FALSE(testState.requestBuzzerChirp);
    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 10: BLE disconnects while temperature is normal -> no sound
void test_ble_disconnects_while_temperature_normal_no_sound(void) {
    testState.temperature = 22.0f;
    testState.isThresholdAlert = false;
    testState.bluetoothConnected = true;
    buzzer.update();
    TEST_ASSERT_FALSE(buzzer.isSounding());

    // Disconnect BLE
    testState.bluetoothConnected = false;
    buzzer.update();

    TEST_ASSERT_FALSE(buzzer.isSounding());
    TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
}

// Case 11: Repeated threshold transitions do not leave buzzer stuck ON
void test_repeated_threshold_transitions_do_not_stick(void) {
    for (int cycle = 0; cycle < 10; cycle++) {
        // High alert
        testState.temperature = 30.0f;
        testState.isThresholdAlert = true;
        buzzer.update();
        TEST_ASSERT_TRUE(buzzer.isSounding());

        // Return to normal
        testState.temperature = 22.0f;
        testState.isThresholdAlert = false;
        buzzer.update();
        TEST_ASSERT_FALSE(buzzer.isSounding());
        TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));

        // Low alert
        testState.temperature = 15.0f;
        testState.isThresholdAlert = true;
        buzzer.update();
        TEST_ASSERT_TRUE(buzzer.isSounding());

        // Return to normal
        testState.temperature = 22.0f;
        testState.isThresholdAlert = false;
        buzzer.update();
        TEST_ASSERT_FALSE(buzzer.isSounding());
        TEST_ASSERT_EQUAL(static_cast<uint8_t>(BuzzerPattern::Off), static_cast<uint8_t>(buzzer.getPattern()));
    }
}

void setup() {
    delay(2000); // Allow hardware USB CDC to enumerate
    UNITY_BEGIN();

    RUN_TEST(test_boot_with_normal_temperature_no_sound);
    RUN_TEST(test_normal_valid_temperature_no_sound);
    RUN_TEST(test_temperature_crosses_high_threshold_alarm_sounds);
    RUN_TEST(test_temperature_crosses_low_threshold_alarm_sounds);
    RUN_TEST(test_temperature_returns_to_normal_alarm_stops_immediately);
    RUN_TEST(test_sensor_disconnects_during_alarm_no_stale_alarm);
    RUN_TEST(test_invalid_sensor_readings_no_false_threshold_alarm);
    RUN_TEST(test_buzzer_toggled_off_during_alarm_immediate_silence);
    RUN_TEST(test_buzzer_toggled_on_during_normal_conditions_remains_silent);
    RUN_TEST(test_ble_disconnects_while_temperature_normal_no_sound);
    RUN_TEST(test_repeated_threshold_transitions_do_not_stick);

    UNITY_END();
}

void loop() {
    // Empty
}
