#pragma once

#include <Arduino.h>

namespace NaTrack {

enum class ButtonId : uint8_t {
    Left,
    Ok,
    Right
};

enum class ButtonEvent : uint8_t {
    None,
    Pressed,
    Released,
    LongPressed,
    DoubleClicked
};

class ButtonListener {
public:
    virtual ~ButtonListener() = default;
    virtual void onButtonEvent(ButtonId button, ButtonEvent event) noexcept = 0;
};

class ButtonManager {
public:
    void begin() noexcept;
    void update() noexcept;

    void setListener(ButtonListener* listener) noexcept {
        listener_ = listener;
    }

private:
    struct ButtonState {
        uint8_t pin;
        ButtonId id;
        bool stablePressed;
        bool lastRawPressed;
        unsigned long lastDebounceAtMs;
        unsigned long pressedAtMs;
        bool longPressReported;
    };

    void processButton(ButtonState& btn) noexcept;

    ButtonState buttons_[3]{
        {16U, ButtonId::Left, false, false, 0UL, 0UL, false},
        {17U, ButtonId::Ok, false, false, 0UL, 0UL, false},
        {18U, ButtonId::Right, false, false, 0UL, 0UL, false}
    };

    ButtonListener* listener_ = nullptr;

    static constexpr unsigned long DebounceMs = 25UL;
    static constexpr unsigned long LongPressMs = 800UL;
};

} // namespace NaTrack
