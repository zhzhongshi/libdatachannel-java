cmake_minimum_required(VERSION 3.20)
project(libdatachannel_jni C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 11)

find_package(Java 11 REQUIRED)

set(OPENSSL_USE_STATIC_LIBS FALSE CACHE BOOL "configure libdatachannel build")

option(PROJECT_VERSION "The version of the project  " "unspecified")

set(NO_WEBSOCKET ON CACHE BOOL "configure libdatachannel build")
set(NO_MEDIA ON CACHE BOOL "configure libdatachannel build")
set(NO_TESTS ON CACHE BOOL "configure libdatachannel build")
set(NO_EXAMPLES ON CACHE BOOL "configure libdatachannel build")
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

add_library(datachannel-java SHARED
        generated/jni-c-to-java.c
        src/init.c
        src/util.c
        src/native_channel.c
        src/native_peer.c
        src/native_track.c
        src/callback.c)
target_link_libraries(datachannel-java PRIVATE datachannel-static)
