cmake_minimum_required(VERSION 3.28)
project(libdatachannel_jni C CXX)

set(CMAKE_C_STANDARD 17)
set(CMAKE_CXX_STANDARD 17)

find_package(Java 11 REQUIRED)

option(PROJECT_VERSION "The version of the project  " "unspecified")
option(IS_RELEASE "Whether this is a release build" OFF)

set(NO_WEBSOCKET ON)
set(NO_MEDIA ON)
set(NO_TESTS ON)
set(NO_EXAMPLES ON)
add_subdirectory(libdatachannel)

include_directories(
        libdatachannel/include
        "$ENV{JAVA_HOME}/include"
        "$ENV{JAVA_HOME}/include/linux"
        generated)

add_compile_options(-Werror -fPIC -D "PROJECT_VERSION=${PROJECT_VERSION}")
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_compile_options(-O3 -flto)
else()
    add_compile_options(-g3 -Og)
endif()

add_link_options(-z noexecstack -fvisibility=hidden)

add_library(libdatachannel_java SHARED
        generated/jni-c-to-java.c
        src/native_callback.c
        src/init.c
        src/native.c)
target_link_libraries(libdatachannel_java PRIVATE datachannel-static)
