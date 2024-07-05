#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include <malloc.h>
#include <errno.h>
#include "jni-c-to-java.h"

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcCreatePeerConnection(JNIEnv *env, jclass clazz,
                                                                            jbyteArray iceServers, jstring proxyServer,
                                                                            jstring bindAddress, jint certificateType,
                                                                            jint iceTransportPolicy,
                                                                            jboolean enableIceTcp,
                                                                            jboolean enableIceUdpMux,
                                                                            jboolean disableAutoNegotiation,
                                                                            jboolean forceMediaTransport,
                                                                            jshort portRangeBegin, jshort portRangeEnd,
                                                                            jint mtu, jint maxMessageSize) {
    rtcConfiguration config = {
            .certificateType = certificateType,
            .iceTransportPolicy = iceTransportPolicy,
            .enableIceTcp = enableIceTcp,
            .enableIceUdpMux = enableIceUdpMux,
            .disableAutoNegotiation = disableAutoNegotiation,
            .forceMediaTransport = forceMediaTransport,
            .portRangeBegin = portRangeBegin,
            .portRangeEnd = portRangeEnd,
            .mtu = mtu,
            .maxMessageSize = maxMessageSize,
    };
    if (iceServers != NULL) {
        config.iceServers = (*env)->GetPrimitiveArrayCritical(env, iceServers, NULL);
    }
    if (proxyServer != NULL) {
        config.proxyServer = (*env)->GetStringUTFChars(env, proxyServer, NULL);
    }
    if (bindAddress != NULL) {
        config.bindAddress = (*env)->GetStringUTFChars(env, bindAddress, NULL);
    }
    jint result = (jint) rtcCreatePeerConnection(&config);

    if (iceServers != NULL) {
        (*env)->ReleasePrimitiveArrayCritical(env, iceServers, config.iceServers, 0);
    }
    if (proxyServer != NULL) {
        (*env)->ReleaseStringUTFChars(env, proxyServer, NULL);
    }
    if (bindAddress != NULL) {
        (*env)->ReleaseStringUTFChars(env, bindAddress, NULL);
    }

    return result;
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcClosePeerConnection(JNIEnv *env, jclass clazz, jint peerHandle) {
    return rtcClosePeerConnection(peerHandle);
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcDeletePeerConnection(JNIEnv *env, jclass clazz,
                                                                            jint peerHandle) {
    return rtcDeletePeerConnection(peerHandle);
}


typedef int(RTC_API *get_dynamic_string_func)(int peerHandle, char* buffer, int size);

jstring get_dynamic_string(JNIEnv *env, get_dynamic_string_func func, int peerHandle) {
    int size = func(peerHandle, NULL, -1);
    if (size < 0) {
        throw_tel_schich_libdatachannel_NativeOperationException(env, size);
        return NULL;
    }
    char* memory = malloc(size);
    int result = func(peerHandle, memory, size);
    if (result < 0) {
        throw_tel_schich_libdatachannel_NativeOperationException(env, size);
        return NULL;
    }

    return (*env)->NewStringUTF(env, memory);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetLocalDescription(JNIEnv *env, jclass clazz, jint peerHandle, jstring type) {
    const char* c_type = (*env)->GetStringUTFChars(env, type, NULL);
    if (c_type == NULL) {
        throw_tel_schich_libdatachannel_NativeOperationException(env, errno);
        return -1;
    }
    int result = rtcSetLocalDescription(peerHandle, c_type);
    (*env)->ReleaseStringUTFChars(env, type, c_type);
    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalDescription(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetLocalDescription, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalDescriptionType(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetLocalDescriptionType, peerHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetRemoteDescription(JNIEnv *env, jclass clazz, jint peerHandle, jstring sdp, jstring type) {
    const char* c_sdp = (*env)->GetStringUTFChars(env, sdp, NULL);
    if (c_sdp == NULL) {
        throw_tel_schich_libdatachannel_NativeOperationException(env, errno);
        return -1;
    }
    const char* c_type = (*env)->GetStringUTFChars(env, type, NULL);
    if (c_type == NULL) {
        (*env)->ReleaseStringUTFChars(env, sdp, c_sdp);
        throw_tel_schich_libdatachannel_NativeOperationException(env, errno);
        return -1;
    }
    int result = rtcSetRemoteDescription(peerHandle, c_sdp, c_type);
    (*env)->ReleaseStringUTFChars(env, sdp, c_sdp);
    (*env)->ReleaseStringUTFChars(env, type, c_type);
    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteDescription(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetRemoteDescription, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteDescriptionType(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetRemoteDescriptionType, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetLocalAddress, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetRemoteAddress, peerHandle);
}
