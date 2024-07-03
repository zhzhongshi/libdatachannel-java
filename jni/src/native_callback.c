#include "native_callback.h"

void logger_callback(rtcLogLevel level, const char *message) {
    JNIEnv* env;
    (*global_JVM)->AttachCurrentThreadAsDaemon(global_JVM, (void **)&env, NULL);
    call_tel_schich_libdatachannel_LibDataChannelNativeCallback_log_cstr(env, level, (char*)message);
    (*global_JVM)->DetachCurrentThread(global_JVM);
}