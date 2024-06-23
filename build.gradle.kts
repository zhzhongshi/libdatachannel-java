//import com.ochafik.lang.jnaerator.JNAeratorConfig
//import org.anarres.gradle.plugin.jnaerator.JNAeratorTask

plugins {
    id("java")
//    id("dev.atsushieno.jnaerator") version "1.0.100"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java.toolchain {
    vendor = JvmVendorSpec.ADOPTIUM
    languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

//tasks.withType<JNAeratorTask>().configureEach {
//    libraryName = "datachannel"
//    packageName = "tel.schich.libdatachannel.generated"
//    setHeaderFiles("libdatachannel/include/rtc/rtc.h")
//    runtimeMode = JNAeratorConfig.Runtime.JNA
//    extraArgs("-v")
//}
