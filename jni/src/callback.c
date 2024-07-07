#include "callback.h"
#include "jni-c-to-java.h"
#include "util.h"
#include <rtc/rtc.h>
#include <malloc.h>
#include <errno.h>


struct jvm_callback* allocate_callback(JNIEnv* env, jobject callback) {
    struct jvm_callback* cb = malloc(sizeof(struct jvm_callback));
    if (cb == NULL) {
        throw_tel_schich_libdatachannel_exception_NativeOperationException(env, errno);
        return NULL;
    }
    (*env)->GetJavaVM(env, &cb->vm);
    cb->instance = (*env)->NewGlobalRef(env, callback);
    return cb;
}

void free_callback(JNIEnv* env, struct jvm_callback* callback) {
    (*env)->DeleteGlobalRef(env, callback->instance);
    free(callback);
}

#define DISPATCH_JNI(target, args...) \
    struct jvm_callback* cb = ptr; \
    JNIEnv* env;      \
    (*cb->vm)->AttachCurrentThreadAsDaemon(cb->vm, (void **)&env, NULL); \
    target(env, cb->instance, args);                  \
    (*cb->vm)->DetachCurrentThread(cb->vm)

#define SETUP_HANDLER(peer, api, target) wrap_error(env, api(peer, target))

void RTC_API handle_local_description(int pc, const char *sdp, const char *type, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onLocalDescription_cstr, sdp, type);
}

void RTC_API handle_local_candidate(int pc, const char *candidate, const char *mediaId, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onLocalCandidate_cstr, candidate, mediaId);
}

void RTC_API handle_state_change(int pc, rtcState state, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onStateChange, state);
}

void RTC_API handle_ice_state_change(int pc, rtcIceState state, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onIceStateChange, state);
}

void RTC_API handle_gathering_state_change(int pc, rtcGatheringState state, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onGatheringStateChange, state);
}

void RTC_API handle_signaling_state_change(int pc, rtcSignalingState state, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onGatheringStateChange, state);
}

void RTC_API handle_data_channel(int pc, int channelHandle, void *ptr) {
    rtcSetUserPointer(channelHandle, ptr);
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onDataChannel, channelHandle);
}

void RTC_API handle_track(int pc, int trackHandle, void *ptr) {
    rtcSetUserPointer(trackHandle, ptr);
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onTrack, trackHandle);
}

void RTC_API handle_channel_open(int channelHandle, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onChannelOpen, channelHandle);
}

void RTC_API handle_channel_closed(int channelHandle, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onChannelClosed, channelHandle);
}

void RTC_API handle_channel_error(int channelHandle, const char *error, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onChannelError_cstr, channelHandle, error);
}

void RTC_API handle_channel_message(int channelHandle, const char *message, int size, void *ptr) {

}

void RTC_API handle_channel_buffered_amount_low(int channelHandle, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onChannelBufferedAmountLow, channelHandle);
}

void RTC_API handle_channel_available(int channelHandle, void *ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onChannelAvailable, channelHandle);
}

JNIEXPORT void JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_setupPeerConnectionListener(JNIEnv *env, jclass clazz, jint peerHandle, jobject listener) {
    struct jvm_callback* jvm_callback = allocate_callback(env, listener);
    if (jvm_callback == NULL) {
        return;
    }
    rtcSetUserPointer(peerHandle, jvm_callback);
    SETUP_HANDLER(peerHandle, rtcSetLocalDescriptionCallback, handle_local_description);
    SETUP_HANDLER(peerHandle, rtcSetLocalCandidateCallback, handle_local_candidate);
    SETUP_HANDLER(peerHandle, rtcSetStateChangeCallback, handle_state_change);
    SETUP_HANDLER(peerHandle, rtcSetIceStateChangeCallback, handle_ice_state_change);
    SETUP_HANDLER(peerHandle, rtcSetGatheringStateChangeCallback, handle_gathering_state_change);
    SETUP_HANDLER(peerHandle, rtcSetSignalingStateChangeCallback, handle_signaling_state_change);
    SETUP_HANDLER(peerHandle, rtcSetDataChannelCallback, handle_data_channel);
    SETUP_HANDLER(peerHandle, rtcSetTrackCallback, handle_track);
    SETUP_HANDLER(peerHandle, rtcSetOpenCallback, handle_channel_open);
    SETUP_HANDLER(peerHandle, rtcSetClosedCallback, handle_channel_closed);
    SETUP_HANDLER(peerHandle, rtcSetErrorCallback, handle_channel_error);
    SETUP_HANDLER(peerHandle, rtcSetMessageCallback, handle_channel_message);
    SETUP_HANDLER(peerHandle, rtcSetBufferedAmountLowCallback, handle_channel_buffered_amount_low);
    SETUP_HANDLER(peerHandle, rtcSetAvailableCallback, handle_channel_available);
}