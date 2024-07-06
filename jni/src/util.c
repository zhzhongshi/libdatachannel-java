#include "util.h"
#include <jni-c-to-java.h>
#include <malloc.h>

jstring get_dynamic_string(JNIEnv *env, get_dynamic_string_func func, int handle) {
    int size = wrap_error(env, func(handle, NULL, -1));
    char* memory = malloc(size);
    wrap_error(env, func(handle, memory, size));
    return (*env)->NewStringUTF(env, memory);
}

jint wrap_error(JNIEnv* env, int result) {
    if (result > 0) {
        return result;
    }
    switch (result) {
        case RTC_ERR_SUCCESS:
            return 0;
        case RTC_ERR_INVALID:
            throw_tel_schich_libdatachannel_exception_InvalidException(env);
            return EXCEPTION_THROWN;
        case RTC_ERR_FAILURE:
            throw_tel_schich_libdatachannel_exception_FailureException(env);
            return EXCEPTION_THROWN;
        case RTC_ERR_NOT_AVAIL:
            throw_tel_schich_libdatachannel_exception_NotAvailableException(env);
            return EXCEPTION_THROWN;
        case RTC_ERR_TOO_SMALL:
            throw_tel_schich_libdatachannel_exception_TooSmallException(env);
            return EXCEPTION_THROWN;
        default:
            throw_tel_schich_libdatachannel_exception_UnknownFailureException(env, result);
            return EXCEPTION_THROWN;
    }
}