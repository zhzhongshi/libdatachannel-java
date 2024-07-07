#include <rtc/rtc.h>
#include <jni.h>
#include "global_jvm.h"
#include "jni-c-to-java.h"

static JavaVM* global_JVM;

void logger_callback(rtcLogLevel level, const char *message) {
    JNIEnv* env;
    (*global_JVM)->AttachCurrentThreadAsDaemon(global_JVM, (void **)&env, NULL);
    call_tel_schich_libdatachannel_LibDataChannel_log_cstr(env, level, (char*)message);
    (*global_JVM)->DetachCurrentThread(global_JVM);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    global_JVM = jvm;
    rtcInitLogger(RTC_LOG_VERBOSE, &logger_callback);
    rtcPreload();
    return JNI_VERSION_10;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved)
{
    global_JVM = NULL;
    rtcCleanup();
}