plugins {
    application
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("tel.schich:libdatachannel-java-arch-detect")
    implementation("ch.qos.logback:logback-classic:1.5.16")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

application {
    mainClass = "tel.schich.libdatachannel.example.Main"
}
