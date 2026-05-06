#include <jni.h>
#include <android/log.h>
#include <android/sensor.h>
#include <memory>
#include <chrono>
#include <cmath>
#include "StepDetector.h"

#define LOG_TAG "activitytracker"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ─── Sensor infrastructure ───────────────────────────────────────────────────

static ASensorManager*    sSensorManager = nullptr;
static ASensorEventQueue* sEventQueue    = nullptr;
static ALooper*           sLooper        = nullptr;

static const ASensor* sAccelerometer = nullptr;
static const ASensor* sGyroscope     = nullptr;

// ─── C++ engine instances ─────────────────────────────────────────────────────

static OrientationFilter sOrientation;
static StepDetector      sDetector;

static int     gStepCount      = 0;
static float   gPitch          = 0.0f;
static float   gRoll           = 0.0f;
static int     gCarryMode      = 0;
static int64_t gLastTimestampNs = 0;

// ─── Sensor event handler ────────────────────────────────────────────────────

static int sensorCallback(int fd, int events, void* data) {
    ASensorEvent event;

    while (ASensorEventQueue_getEvents(sEventQueue, &event, 1) > 0) {
        int64_t nowNs = event.timestamp;
        float dt = (gLastTimestampNs == 0) ? 0.0f : (nowNs - gLastTimestampNs) * 1e-9f;
        gLastTimestampNs = nowNs;

        if (event.type == ASENSOR_TYPE_ACCELEROMETER) {
            float ax = event.acceleration.x / 9.80665f;
            float ay = event.acceleration.y / 9.80665f;
            float az = event.acceleration.z / 9.80665f;

            auto mode = sOrientation.carryMode();
            sDetector.onAccelSample(ax, ay, az, mode);

            gStepCount = sDetector.getStepCount();
            gCarryMode = static_cast<int>(mode);

        } else if (event.type == ASENSOR_TYPE_GYROSCOPE && dt > 0.0f) {
            sOrientation.update(0.0f, 0.0f, 0.0f,
                                event.vector.x, event.vector.y, event.vector.z,
                                dt);
            gPitch = sOrientation.pitch;
            gRoll  = sOrientation.roll;
        }
    }
    return 1;
}

// ─── JNI exported functions ───────────────────────────────────────────────────

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_activitytracker_MainActivity_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("nativeInit");

    sLooper = ALooper_forThread();
    if (!sLooper) {
        sLooper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    }

    sSensorManager = ASensorManager_getInstanceForPackage("com.example.activitytracker");
    if (!sSensorManager) {
        sSensorManager = ASensorManager_getInstance();
    }

    sAccelerometer = ASensorManager_getDefaultSensor(sSensorManager, ASENSOR_TYPE_ACCELEROMETER);
    sGyroscope     = ASensorManager_getDefaultSensor(sSensorManager, ASENSOR_TYPE_GYROSCOPE);

    if (!sAccelerometer) {
        LOGE("No accelerometer found!");
        return JNI_FALSE;
    }

    sEventQueue = ASensorManager_createEventQueue(sSensorManager, sLooper,
                                                  ALOOPER_POLL_CALLBACK,
                                                  sensorCallback, nullptr);

    ASensorEventQueue_enableSensor(sEventQueue, sAccelerometer);
    ASensorEventQueue_setEventRate(sEventQueue, sAccelerometer, 20000);

    if (sGyroscope) {
        ASensorEventQueue_enableSensor(sEventQueue, sGyroscope);
        ASensorEventQueue_setEventRate(sEventQueue, sGyroscope, 20000);
    }

    sDetector = StepDetector([](int steps) {
        LOGI("Step detected — total: %d", steps);
    });

    LOGI("Sensors initialised");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_example_activitytracker_MainActivity_nativeShutdown(JNIEnv* env, jobject thiz) {
    if (sEventQueue) {
        if (sAccelerometer) ASensorEventQueue_disableSensor(sEventQueue, sAccelerometer);
        if (sGyroscope)     ASensorEventQueue_disableSensor(sEventQueue, sGyroscope);
        ASensorManager_destroyEventQueue(sSensorManager, sEventQueue);
        sEventQueue = nullptr;
    }
    LOGI("Sensors shut down");
}

JNIEXPORT jint JNICALL
Java_com_example_activitytracker_MainActivity_nativeGetSteps(JNIEnv* env, jobject thiz) {
    return static_cast<jint>(gStepCount);
}

JNIEXPORT void JNICALL
Java_com_example_activitytracker_MainActivity_nativeResetSteps(JNIEnv* env, jobject thiz) {
    sDetector.reset();
    gStepCount = 0;
}

JNIEXPORT jfloat JNICALL
Java_com_example_activitytracker_MainActivity_nativeGetPitch(JNIEnv* env, jobject thiz) {
    return gPitch;
}

JNIEXPORT jfloat JNICALL
Java_com_example_activitytracker_MainActivity_nativeGetRoll(JNIEnv* env, jobject thiz) {
    return gRoll;
}

JNIEXPORT jint JNICALL
Java_com_example_activitytracker_MainActivity_nativeGetCarryMode(JNIEnv* env, jobject thiz) {
    return static_cast<jint>(gCarryMode);
}

} // extern "C"
