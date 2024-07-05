
plugins {
    java
    id("tel.schich.dockcross") version "0.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java.toolchain {
    vendor = JvmVendorSpec.ADOPTIUM
    languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("net.java.dev.jna:jna:5.14.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    "tel.schich:jni-access-generator:1.1.3-SNAPSHOT".also {
        annotationProcessor(it)
        compileOnly(it)
    }
}

val jniGluePath = project.layout.projectDirectory.dir("jni/generated")
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Agenerate.jni.headers=true"))
    options.headerOutputDirectory = jniGluePath
}

tasks.test {
    useJUnitPlatform()
}
