#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include <malloc.h>
#include <errno.h>
#include "jni-c-to-java.h"
#include "util.h"

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

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetLocalAddress, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteAddress(JNIEnv *env, jclass clazz, jint peerHandle) {
    return get_dynamic_string(env, rtcGetRemoteAddress, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackDescription(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackDescription, trackHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetTrackMid(JNIEnv *env, jclass clazz, jint trackHandle) {
    return get_dynamic_string(env, rtcGetTrackMid, trackHandle);
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