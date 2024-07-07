#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include "jni-c-to-java.h"
#include "util.h"

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcCreateDataChannelEx(JNIEnv *env, jclass clazz, jint peerHandle,
                                                                           jstring label, jboolean unordered,
                                                                           jboolean unreliable, jlong maxPacketLifeTime,
                                                                           jint maxRetransmits, jstring protocol,
                                                                           jboolean negotiated, jint stream,
                                                                           jboolean manualStream) {
    rtcDataChannelInit init = {
            .reliability = {
                    .unordered = unordered,
                    .unreliable = unreliable,
                    .maxPacketLifeTime = maxRetransmits,
                    .maxRetransmits = maxRetransmits,
            },
            .protocol = NULL,
            .negotiated = negotiated,
            .stream = stream,
            .manualStream = manualStream,
    };
    const char* c_label = NULL;
    if (label != NULL) {
        c_label = (*env)->GetStringUTFChars(env, label, NULL);
    }
    if (protocol != NULL) {
        init.protocol = (*env)->GetStringUTFChars(env, protocol, NULL);
    }

    jint result = wrap_error(env, rtcCreateDataChannelEx(peerHandle, c_label, &init));

    if (c_label != NULL) {
        (*env)->ReleaseStringUTFChars(env, label, c_label);
    }
    if (init.protocol != NULL) {
        (*env)->ReleaseStringUTFChars(env, protocol, init.protocol);
    }

    return result;
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcClose(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcClose(channelHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcDelete(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcDelete(channelHandle);
}

JNIEXPORT jboolean JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcIsClosed(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcIsClosed(channelHandle);
}

JNIEXPORT jboolean JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcIsOpen(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcIsOpen(channelHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetAvailableAmount(JNIEnv *env, jclass clazz, jint channelHandle) {
    return wrap_error(env, rtcGetAvailableAmount(channelHandle));
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetBufferedAmount(JNIEnv *env, jclass clazz, jint channelHandle) {
    return wrap_error(env, rtcGetBufferedAmount(channelHandle));
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetDataChannelStream(JNIEnv *env, jclass clazz, jint channelHandle) {
    return wrap_error(env, rtcGetDataChannelStream(channelHandle));
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetDataChannelLabel(JNIEnv *env, jclass clazz, jint channelHandle) {
    return get_dynamic_string(env, rtcGetDataChannelLabel, channelHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetDataChannelProtocol(JNIEnv *env, jclass clazz, jint channelHandle) {
    return get_dynamic_string(env, rtcGetDataChannelProtocol, channelHandle);
}

JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetDataChannelReliability(JNIEnv *env, jclass clazz, jint channelHandle) {
    rtcReliability reliability;
    wrap_error(env, rtcGetDataChannelReliability(channelHandle, &reliability));
    return create_tel_schich_libdatachannel_DataChannelReliability(env, reliability.unordered, reliability.unreliable, reliability.maxPacketLifeTime, (jint) reliability.maxRetransmits);
}