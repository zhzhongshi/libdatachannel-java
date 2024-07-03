#ifndef LIBDATACHANNEL_JNI_NATIVE_CALLBACK_H
#define LIBDATACHANNEL_JNI_NATIVE_CALLBACK_H

#include "global_jvm.h"
#include <jni-c-to-java.h>
#include <rtc/rtc.h>

void RTC_API logger_callback(rtcLogLevel level, const char *message);


#endif //LIBDATACHANNEL_JNI_NATIVE_CALLBACK_H
