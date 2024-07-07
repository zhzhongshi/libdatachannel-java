#include <jni.h>
#include <rtc/rtc.h>
#include <jni-java-to-c.h>
#include <malloc.h>
#include "jni-c-to-java.h"
#include "util.h"

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetMaxDataChannelStream(JNIEnv *env, jclass clazz, jint peerHandle) {
    return rtcGetMaxDataChannelStream(peerHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteMaxMessageSize(JNIEnv *env, jclass clazz, jint peerHandle) {
    return rtcGetRemoteMaxMessageSize(peerHandle);
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
                    .maxPacketLifeTime = maxPacketLifeTime,
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
    rtcSetUserPointer(result, rtcGetUserPointer(peerHandle));

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

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcDeleteDataChannel(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcDeleteDataChannel(channelHandle);
}


JNIEXPORT jboolean JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcIsClosed(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcIsClosed(channelHandle);
}

JNIEXPORT jboolean JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcIsOpen(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcIsOpen(channelHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcMaxMessageSize(JNIEnv *env, jclass clazz, jint channelHandle) {
    return rtcMaxMessageSize(channelHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetBufferedAmountLowThreshold(JNIEnv *env, jclass clazz, jint channelHandle, jint amount) {
            return rtcSetBufferedAmountLowThreshold(channelHandle, amount);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSendMessage(JNIEnv *env, jclass clazz, jint channelHandle, jobject data, jint offset, jint length) {
    if (data == NULL) {
        return RTC_ERR_SUCCESS;
    }
    char* buffer = (*env)->GetDirectBufferAddress(env, data);
    if (buffer == NULL) {
        return RTC_ERR_SUCCESS;
    }
    char* buffer_offset = buffer + offset;
    return rtcSendMessage(channelHandle, buffer_offset, length);
}

JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcReceiveMessage(JNIEnv *env, jclass clazz, jint channelHandle) {
    int size = 0;
    wrap_error(env, rtcReceiveMessage(channelHandle, NULL, &size));
    if (size == 0) {
        return NULL;
    }
    void* buffer = malloc(size);
    int result = rtcReceiveMessage(channelHandle, buffer, &size);
    if (result == RTC_ERR_NOT_AVAIL) {
        return NULL;
    }
    wrap_error(env, result);

    return (*env)->NewDirectByteBuffer(env, buffer, size);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcReceiveMessageInto(JNIEnv *env, jclass clazz, jint channelHandle, jobject buffer, jint offset, jint capacity) {
    int size = capacity;
    if (buffer == NULL) {
        return 0;
    }
    void* base = (*env)->GetDirectBufferAddress(env, buffer);
    if (base == NULL) {
        return 0;
    }
    void* data = base + offset;

    int result = rtcReceiveMessage(channelHandle, data, &size);
    if (result == RTC_ERR_NOT_AVAIL) {
        return 0;
    }
    if (result == RTC_ERR_TOO_SMALL) {
        return -size;
    }
    wrap_error(env, result);
    return size;
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