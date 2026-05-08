<img width="463" height="761" alt="Screenshot 2026-05-08 at 3 36 54 PM" src="https://github.com/user-attachments/assets/019bf54e-8459-4421-873a-1c16b164aac4" />
<img width="385" height="773" alt="Screenshot 2026-05-08 at 3 37 22 PM" src="https://github.com/user-attachments/assets/3d356b82-da9d-4d91-8d8a-0b72c1666ed7" />
<img width="388" height="763" alt="Screenshot 2026-05-08 at 3 37 38 PM" src="https://github.com/user-attachments/assets/57b2c71d-346b-422d-86e2-3decc4ce8f57" />
Project Architecture
Java (UI layer)
    ↕  JNI calls every 200ms
C++ sensor_bridge.cpp  ←  ASensorManager events (50 Hz)
    ↓
StepDetector.h  +  OrientationFilter
The 3 C++ files
StepDetector.h — pure, portable C++, no Android dependencies:

OrientationFilter uses a complementary filter using gyro + accel to get stable pitch and roll without drift
StepDetector runs peak detection on the accelerometer magnitude with a low-pass smoother. 
adjusts its sensitivity threshold based on carry mode — pocket walking needs a higher threshold than hand-held

The accelerometer measures acceleration forces 
(linear movement) along three axes (X, Y, and Z), 
detecting how fast the phone is speeding up, slowing down, or its tilt.

The gyroscope measures angular velocity—how fast the phone is rotating or twisting around the three axes

sensor_bridge.cpp — the Android glue:

Uses ASensorManager + ALooper to receive sensor events on a background thread at ~50 Hz
Exports JNI functions (nativeInit, nativeGetSteps, etc.) that Java calls
Atomic global state (gStepCount, gPitch, etc.) is written by C++ and read by Java's poll loop

CMakeLists.txt — links against android and log system libraries, enables -ffast-math for a free speed boost on sensor math

workouts
- add, edit, delete

Workout.java - store Exercise Name, Reps, Sets, Weight
WorkoutActivity.java - provides a structured interface for logging weight-training exercises, 
including sets, repetitions, and load. 
It uses a RecyclerView to display workout entries in real time and persists data locally, 
allowing users to switch between features without losing progress.
WorkoutAdapter.java - acts as a bridge between the workout data model and the RecyclerView UI, 
binding each workout entry (exercise, sets, reps, weight) to a visual list item for efficient and dynamic rendering.
Project Features
1. Tracks step count using sensors in the phone
2. Track Workouts on a separate interface

Switch Between Features using bottom navigation ensuring no data is lost when you switch between

To open in Android Studio

Open the ActivityTracker/ folder as an existing project
Android Studio will detect the externalNativeBuild block and download the NDK automatically
Run on a real device — the emulator has a software accelerometer but it won't simulate steps well
