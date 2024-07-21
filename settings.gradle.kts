rootProject.name = "libdatachannel-java"

pluginManagement {
    includeBuild("conventions")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("arch-detect")