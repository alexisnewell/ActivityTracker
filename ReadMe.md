Project Architecture
Java (UI layer)
    ↕  JNI calls every 200ms
C++ sensor_bridge.cpp  ←  ASensorManager events (50 Hz)
    ↓
StepDetector.h  +  OrientationFilter
The 3 C++ files
StepDetector.h — pure, portable C++, no Android dependencies:

OrientationFilter uses a complementary filter (98% gyro / 2% accel) to get stable pitch and roll without drift
StepDetector runs peak detection on the accelerometer magnitude with a low-pass smoother to kill jitter. Crucially, it adjusts its sensitivity threshold based on carry mode — pocket walking needs a higher threshold than hand-held

sensor_bridge.cpp — the Android glue:

Uses ASensorManager + ALooper to receive sensor events on a background thread at ~50 Hz
Exports JNI functions (nativeInit, nativeGetSteps, etc.) that Java calls
Atomic global state (gStepCount, gPitch, etc.) is written by C++ and read by Java's poll loop

CMakeLists.txt — links against android and log system libraries, enables -ffast-math for a free speed boost on sensor math
To open in Android Studio

Open the StepTracker/ folder as an existing project
Android Studio will detect the externalNativeBuild block and download the NDK automatically
Run on a real device — the emulator has a software accelerometer but it won't simulate steps well