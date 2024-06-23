plugins {
    id("java")
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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}