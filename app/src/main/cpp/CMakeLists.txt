cmake_minimum_required(VERSION 3.22.1)
project("activitytracker")

# ── C++ standard ──────────────────────────────────────────────────────────────
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Compiler hardening flags ──────────────────────────────────────────────────
add_compile_options(
    -Wall
    -Wextra
    -O2
    -ffast-math          # safe for sensor math; gives ~10-15% speed boost
)

# ── Our native library ────────────────────────────────────────────────────────
add_library(
        activitytracker          # becomes libsteptracker.so
    SHARED
    sensor_bridge.cpp    # JNI entry points + sensor loop
    # StepDetector.h is header-only — no .cpp needed
)

# ── Android system libraries ──────────────────────────────────────────────────
# android   : ALooper, ASensorManager, etc.
# log       : __android_log_print
find_library(log-lib  log)
find_library(android-lib android)

target_link_libraries(
        activitytracker
    ${android-lib}
    ${log-lib}
)

# ── Include dirs ──────────────────────────────────────────────────────────────
target_include_directories(activitytracker PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
