#pragma once
#include <cmath>
#include <cstdint>
#include <functional>

// ─────────────────────────────────────────────
//  OrientationFilter
//  Complementary filter fusing accel + gyro.
//  Gives stable pitch/roll even with sensor noise.
// ─────────────────────────────────────────────
class OrientationFilter {
public:
    float pitch = 0.0f;  // forward/back tilt  (degrees)
    float roll  = 0.0f;  // left/right tilt    (degrees)

    // Call once per sensor batch with delta-time in seconds
    void update(float ax, float ay, float az,
                float gx, float gy, float gz,
                float dt)
    {
        // Accel-derived angles (noisy but no drift)
        float accelPitch = std::atan2(ay, std::sqrt(ax*ax + az*az)) * RAD2DEG;
        float accelRoll  = std::atan2(-ax, az)                      * RAD2DEG;

        // Integrate gyro (fast, but drifts over time)
        pitch = ALPHA * (pitch + gx * dt * RAD2DEG) + (1.0f - ALPHA) * accelPitch;
        roll  = ALPHA * (roll  + gy * dt * RAD2DEG) + (1.0f - ALPHA) * accelRoll;
    }

    // Classify how the user is holding the phone
    enum class CarryMode { HAND, POCKET, BAG, UNKNOWN };
    CarryMode carryMode() const {
        float absPitch = std::fabs(pitch);
        float absRoll  = std::fabs(roll);
        if (absPitch < 30.0f && absRoll < 30.0f)  return CarryMode::HAND;
        if (absPitch > 60.0f)                      return CarryMode::POCKET;
        if (absRoll  > 60.0f)                      return CarryMode::BAG;
        return CarryMode::UNKNOWN;
    }

private:
    static constexpr float ALPHA   = 0.98f;
    static constexpr float RAD2DEG = 57.2957795f;
};


// ─────────────────────────────────────────────
//  StepDetector
//  Peak-detection pedometer on raw accelerometer.
//  Adapts threshold based on carry mode.
// ─────────────────────────────────────────────
class StepDetector {
public:
    using StepCallback = std::function<void(int totalSteps)>;

    explicit StepDetector(StepCallback cb = nullptr)
        : onStep(std::move(cb)) {}

    // Feed one accelerometer sample (m/s² or G — keep consistent)
    void onAccelSample(float ax, float ay, float az,
                       OrientationFilter::CarryMode mode)
    {
        float mag = std::sqrt(ax*ax + ay*ay + az*az);

        // Low-pass filter to smooth out jitter
        smoothed = LOW_PASS * mag + (1.0f - LOW_PASS) * smoothed;

        float delta = smoothed - lastSmoothed;
        lastSmoothed = smoothed;

        float threshold = thresholdFor(mode);

        // Rising edge crosses threshold → step
        if (delta > threshold && !inStep) {
            inStep = true;
            ++stepCount;
            if (onStep) onStep(stepCount);
        } else if (delta < -threshold * 0.5f) {
            inStep = false;   // reset after the trough
        }
    }

    int  getStepCount() const { return stepCount; }
    void reset()              { stepCount = 0; inStep = false; smoothed = 0; lastSmoothed = 0; }

private:
    int   stepCount    = 0;
    bool  inStep       = false;
    float smoothed     = 0.0f;
    float lastSmoothed = 0.0f;

    static constexpr float LOW_PASS = 0.1f;

    StepCallback onStep;

    // Tune sensitivity per carry mode
    static float thresholdFor(OrientationFilter::CarryMode m) {
        switch (m) {
            case OrientationFilter::CarryMode::HAND:    return 1.5f;
            case OrientationFilter::CarryMode::POCKET:  return 2.2f;
            case OrientationFilter::CarryMode::BAG:     return 3.0f;
            default:                                    return 2.0f;
        }
    }
};
