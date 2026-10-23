#include "ButtonManager.hpp"

namespace Flux {

void ButtonManager::begin() noexcept {
    for (auto& btn : buttons_) {
        pinMode(btn.pin, INPUT_PULLUP);
        const bool initialVal = (digitalRead(btn.pin) == LOW);
        btn.stablePressed = initialVal;
        btn.lastRawPressed = initialVal;
        btn.lastDebounceAtMs = millis();
        btn.pressedAtMs = btn.stablePressed ? millis() : 0UL;
        btn.longPressReported = false;
    }
}

void ButtonManager::processButton(ButtonState& btn) noexcept {
    const bool rawPressed = (digitalRead(btn.pin) == LOW);
    const unsigned long nowMs = millis();

    if (rawPressed != btn.lastRawPressed) {
        btn.lastRawPressed = rawPressed;
        btn.lastDebounceAtMs = nowMs;
    }

    if ((nowMs - btn.lastDebounceAtMs) >= DebounceMs) {
        if (rawPressed != btn.stablePressed) {
            btn.stablePressed = rawPressed;
            if (btn.stablePressed) {
                btn.pressedAtMs = nowMs;
                btn.longPressReported = false;
                if (listener_ != nullptr) {
                    listener_->onButtonEvent(btn.id, ButtonEvent::Pressed);
                }
            } else {
                if (listener_ != nullptr) {
                    listener_->onButtonEvent(btn.id, ButtonEvent::Released);
                }
                btn.pressedAtMs = 0UL;
                btn.longPressReported = false;
            }
        }
    }

    // Check for long press if button is stable-pressed and not reported yet
    if (btn.stablePressed && !btn.longPressReported && btn.pressedAtMs > 0UL) {
        if ((nowMs - btn.pressedAtMs) >= LongPressMs) {
            btn.longPressReported = true;
            if (listener_ != nullptr) {
                listener_->onButtonEvent(btn.id, ButtonEvent::LongPressed);
            }
        }
    }
}

void ButtonManager::update() noexcept {
    for (auto& btn : buttons_) {
        processButton(btn);
    }
}

} // namespace Flux
