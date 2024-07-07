#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include <malloc.h>
#include <errno.h>
#include "jni-c-to-java.h"
#include "util.h"
#include "callback.h"

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
    struct jvm_callback* callback = rtcGetUserPointer(peerHandle);
    if (callback != NULL) {
        free_callback(env, callback);
    }

    return rtcDeletePeerConnection(peerHandle);
}


JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetLocalDescription(JNIEnv *env, jclass clazz, jint peerHandle, jstring type) {
    const char* c_type = (*env)->GetStringUTFChars(env, type, NULL);
    if (c_type == NULL) {
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return EXCEPTION_THROWN;
    }
    int result = wrap_error(env, rtcSetLocalDescription(peerHandle, c_type));
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
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return EXCEPTION_THROWN;
    }
    const char* c_type = (*env)->GetStringUTFChars(env, type, NULL);
    if (c_type == NULL) {
        (*env)->ReleaseStringUTFChars(env, sdp, c_sdp);
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return EXCEPTION_THROWN;
    }
    int result = wrap_error(env, rtcSetRemoteDescription(peerHandle, c_sdp, c_type));
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

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcAddRemoteCandidate(JNIEnv *env, jclass clazz, jint peerHandle, jstring candidate, jstring mediaId) {
    const char* c_candidate = NULL;
    if (candidate != NULL) {
        c_candidate = (*env)->GetStringUTFChars(env, candidate, NULL);
        if (c_candidate == NULL) {
            throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
            return EXCEPTION_THROWN;
        }
    }
    const char* c_mediaId = NULL;
    if (mediaId != NULL) {
        c_mediaId = (*env)->GetStringUTFChars(env, mediaId, NULL);
        if (c_mediaId == NULL) {
            if (c_candidate != NULL) {
                (*env)->ReleaseStringUTFChars(env, candidate, c_candidate);
            }
            throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
            return EXCEPTION_THROWN;
        }
    }

    int result = rtcAddRemoteCandidate(peerHandle, c_candidate, c_mediaId);
    if (c_candidate != NULL) {
        (*env)->ReleaseStringUTFChars(env, candidate, c_candidate);
    }
    if (c_mediaId != NULL) {
        (*env)->ReleaseStringUTFChars(env, mediaId, c_mediaId);
    }

    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetLocalAddress, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetRemoteAddress, peerHandle);
}

JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetSelectedCandidatePair(JNIEnv *env, jclass clazz, jint peerHandle) {
    int bufSize = 50;
    char *local = malloc(bufSize);
    if (local == NULL) {
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return NULL;
    }
    char *remote = malloc(bufSize);
    if (remote == NULL) {
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return NULL;
    }

    wrap_error(env, rtcGetSelectedCandidatePair(peerHandle, local, bufSize, remote, bufSize));
    return call_tel_schich_libdatachannel_CandidatePair_parse_cstr(env, local, remote);
}