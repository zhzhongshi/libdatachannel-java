#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include "util.h"

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackDescription(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackDescription, trackHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackMid(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackMid, trackHandle);
}
