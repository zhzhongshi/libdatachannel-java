#include <rtc/rtc.h>
#include <jni.h>
#include "global_jvm.h"
#include "native_callback.h"

static JavaVM* global_JVM;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    global_JVM = jvm;
    rtcInitLogger(RTC_LOG_VERBOSE, &logger_callback);
    rtcPreload();
    return 0;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved)
{
    global_JVM = NULL;
    rtcCleanup();
}