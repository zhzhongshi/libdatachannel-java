#include <jni.h>
#include <rtc/rtc.h>

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_newRtcPeerConnection(JNIEnv *env, jclass clazz) {
    rtcConfiguration config = { 0 };
    return (jint) rtcCreatePeerConnection(&config);
}