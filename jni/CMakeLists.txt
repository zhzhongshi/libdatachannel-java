cmake_minimum_required(VERSION 3.28)
project(libdatachannel_jni C CXX)

set(CMAKE_C_STANDARD 17)
set(CMAKE_CXX_STANDARD 17)

set(NO_WEBSOCKET ON)
set(NO_MEDIA ON)
set(NO_TESTS ON)
set(NO_EXAMPLES ON)
add_subdirectory(libdatachannel)

include_directories(
        libdatachannel/include
        "$ENV{JAVA_HOME}/include"
        "$ENV{JAVA_HOME}/include/linux"
        generated/jni)

add_library(libdatachannel_java SHARED
        generated/jni-c-to-java.c)
target_link_libraries(libdatachannel_java PRIVATE datachannel-static)
