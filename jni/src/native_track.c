#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include "util.h"

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcAddTrack(JNIEnv *env, jclass clazz, jint peerHandle, jstring sdp) {
    const char* chars = NULL;
    if (sdp != NULL) {
        chars = (*env)->GetStringUTFChars(env, sdp, NULL);
    }
    int result = rtcAddTrack(peerHandle, chars);
    if (sdp != NULL) {
        (*env)->ReleaseStringUTFChars(env, sdp, chars);
    }
    return result;
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcAddTrackEx(JNIEnv *env, jclass clazz, jint peerHandle, jint direction, jint codec) {
    rtcTrackInit init = {
            .direction = direction,
            .codec = codec,
    };
    return rtcAddTrackEx(peerHandle, &init);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackDescription(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackDescription, trackHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackDirection(JNIEnv *env, jclass clazz, jint trackHandle) {
    rtcDirection direction;
    wrap_error(env, rtcGetTrackDirection(trackHandle, &direction));
    return direction;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackMid(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackMid, trackHandle);
}
