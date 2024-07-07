import org.gradle.configurationcache.extensions.capitalized
import tel.schich.dockcross.execute.DockerRunner
import tel.schich.dockcross.execute.NonContainerRunner
import tel.schich.dockcross.tasks.DockcrossRunTask

plugins {
    java
    id("tel.schich.dockcross") version "0.2.0"
    `maven-publish`
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
}

val jniPath = project.layout.projectDirectory.dir("jni")
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Agenerate.jni.headers=true"))
    options.headerOutputDirectory = jniPath.dir("generated")
}

tasks.test {
    useJUnitPlatform()
}

val nativeGroup = "native"
val ci = System.getenv("CI") != null
val buildReleaseBinaries = project.findProperty("libdatachannel.build-release-binaries")
    ?.toString()
    ?.ifEmpty { null }
    ?.toBooleanStrictOrNull()
    ?: !project.version.toString().endsWith("-SNAPSHOT")

fun DockcrossRunTask.baseConfigure(outputTo: Directory, args: List<String> = emptyList()) {
    group = nativeGroup

    dockcrossTag = "20240529-0dade71"
    inputs.dir(jniPath)

    dependsOn(tasks.compileJava)

    javaHome = javaToolchains.launcherFor(java.toolchain).map { it.metadata.installationPath }
    output = outputTo.dir("native")
//    val conanDir = "conan"
//    extraEnv.put("CONAN_HOME", SubstitutingString("\${OUTPUT_DIR}/$conanDir/home"))

    val relativePathToProject = output.get().asFile.toPath().relativize(jniPath.asFile.toPath()).toString()
    val projectVersionOption = "-DPROJECT_VERSION=${project.version}"
    val releaseOption = "-DIS_RELEASE=${if (buildReleaseBinaries) "1" else "0"}"
    script = listOf(
//        listOf("conan", "profile", "detect", "-f"),
//        listOf("conan", "install", relativePathToProject, "--output-folder=$conanDir", "--build=missing"),
        listOf("cmake", relativePathToProject, projectVersionOption, releaseOption) + args,
        listOf("make", "-j${project.gradle.startParameter.maxWorkerCount}"),
    )

    if (ci) {
        runner(DockerRunner())
    }
}

fun Jar.baseConfigure(compileTask: TaskProvider<DockcrossRunTask>, buildOutputDir: Directory) {
    group = nativeGroup

    dependsOn(compileTask)

    from(buildOutputDir) {
        include("native/*.so")
        include("native/*.dll")
    }
}

val dockcrossOutputDir: Directory = project.layout.buildDirectory.get().dir("dockcross")
val nativeForHostOutputDir: Directory = dockcrossOutputDir.dir("host")
val compileNativeForHost by tasks.registering(DockcrossRunTask::class) {
    baseConfigure(nativeForHostOutputDir)
    unsafeWritableMountSource = true
    image = "host"
    runner(NonContainerRunner)
}

val packageNativeForHost by tasks.registering(Jar::class) {
    baseConfigure(compileNativeForHost, nativeForHostOutputDir)
    archiveClassifier = "host"
}

data class BuildTarget(
    val image: String,
    val classifier: String,
    val env: Map<String, String> = emptyMap(),
    val args: List<String> = emptyList(),
)

val targets = listOf(
    BuildTarget(image = "linux-x86_64-full", classifier = "x86_64"),
    BuildTarget(image = "linux-arm64-full", classifier = "aarch64"),
//    BuildTarget(image = "windows-shared-x64", classifier = "windows-x86_64", args = listOf("-DUSE_GNUTLS=1")),
)

val packageNativeAll by tasks.registering(DefaultTask::class) {
    group = nativeGroup
}


for (target in targets) {
    val outputDir: Directory = dockcrossOutputDir.dir(target.classifier)
    val taskSuffix = target.classifier.split("[_-]".toRegex()).joinToString(separator = "") { it.capitalized() }
    val compileNative = tasks.register("compileNativeFor$taskSuffix", DockcrossRunTask::class) {
        baseConfigure(outputDir, target.args)
        unsafeWritableMountSource = true
        image = target.image

        if (ci) {
            runner(DockerRunner())
        }
    }

    val packageNative = tasks.register("packageNativeFor$taskSuffix", Jar::class) {
        baseConfigure(compileNative, outputDir)
        archiveClassifier = target.classifier
    }

    publishing.publications.withType<MavenPublication>().configureEach {
        artifact(packageNative)
    }

    packageNativeAll.configure {
        dependsOn(packageNative)
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    "tel.schich:jni-access-generator:1.1.5-SNAPSHOT".also {
        annotationProcessor(it)
        compileOnly(it)
    }

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("net.java.dev.jna:jna:5.14.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(files(packageNativeForHost))
}