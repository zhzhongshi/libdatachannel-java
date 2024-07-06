#ifndef LIBDATACHANNEL_JNI_UTIL_H
#define LIBDATACHANNEL_JNI_UTIL_H

#include <rtc/rtc.h>
#include <jni-c-to-java.h>

#define EXCEPTION_THROWN -999

typedef int(RTC_API *get_dynamic_string_func)(int peerHandle, char* buffer, int size);

jstring get_dynamic_string(JNIEnv *env, get_dynamic_string_func func, int handle);

jint wrap_error(JNIEnv* env, int result);

#endif //LIBDATACHANNEL_JNI_UTIL_H
