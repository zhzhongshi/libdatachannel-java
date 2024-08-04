plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("tel.schich:libdatachannel-java-arch-detect")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

application {
    mainClass = "tel.schich.libdatachannel.example.Main"
}
