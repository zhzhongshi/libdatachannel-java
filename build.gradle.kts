import org.gradle.configurationcache.extensions.capitalized
import tel.schich.dockcross.execute.DockerRunner
import tel.schich.dockcross.execute.NonContainerRunner
import tel.schich.dockcross.execute.SubstitutingString
import tel.schich.dockcross.tasks.DockcrossRunTask

plugins {
    id("tel.schich.libdatachannel.convention.common")
    id("tel.schich.dockcross") version "0.2.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

description = "${project.name} is a binding to the libdatachannel that feels native to Java developers."

val archDetectConfiguration by configurations.registering {
    isCanBeConsumed = true
}

val jniPath = project.layout.projectDirectory.dir("jni")
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Agenerate.jni.headers=true"))
    options.headerOutputDirectory = jniPath.dir("generated")
}

val nativeGroup = "native"
val ci = System.getenv("CI") != null
val buildReleaseBinaries = project.findProperty("libdatachannel.build-release-binaries")
    ?.toString()
    ?.ifEmpty { null }
    ?.toBooleanStrictOrNull()
    ?: !project.version.toString().endsWith("-SNAPSHOT")

fun DockcrossRunTask.baseConfigure(outputTo: Directory, args: List<String> = emptyList(), conanProfile: String? = null) {
    group = nativeGroup

    dockcrossTag = "20240529-0dade71"
    inputs.dir(jniPath)

    dependsOn(tasks.compileJava)

    javaHome = javaToolchains.launcherFor(java.toolchain).map { it.metadata.installationPath }
    output = outputTo.dir("native")
    val conanDir = "conan"
    extraEnv.put("CONAN_HOME", SubstitutingString("\${OUTPUT_DIR}/$conanDir/home"))
    // OpenSSL's makefile constructs broken compiler paths due to CROSS_COMPILE
    extraEnv.put("CROSS_COMPILE", "")

    val relativePathToProject = output.get().asFile.toPath().relativize(jniPath.asFile.toPath()).toString()
    val projectVersionOption = "-DPROJECT_VERSION=${project.version}"
    val releaseOption = "-DIS_RELEASE=${if (buildReleaseBinaries) "1" else "0"}"
    val conanHostProfileOption = conanProfile?.let {  "--profile:host=/work/jni/$it.ini"}
    script = listOf(
        listOf("conan", "profile", "detect", "-f"),
        listOfNotNull("conan", "install", relativePathToProject, "--output-folder=$conanDir", conanHostProfileOption, "--build=missing"),
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
    BuildTarget(image = "linux-x64", classifier = "x86_64"),
    BuildTarget(image = "linux-arm64", classifier = "aarch64"),
//    BuildTarget(image = "windows-shared-x64", classifier = "windows-x86_64", args = listOf("-DUSE_GNUTLS=1")),
)

val packageNativeAll by tasks.registering(DefaultTask::class) {
    group = nativeGroup
}


for (target in targets) {
    val outputDir: Directory = dockcrossOutputDir.dir(target.classifier)
    val taskSuffix = target.classifier.split("[_-]".toRegex()).joinToString(separator = "") { it.capitalized() }
    val compileNative = tasks.register("compileNativeFor$taskSuffix", DockcrossRunTask::class) {
        baseConfigure(outputDir, target.args, target.image)
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

    artifacts.add(archDetectConfiguration.name, packageNative)
}

dependencies {
    "tel.schich:jni-access-generator:1.1.6".also {
        annotationProcessor(it)
        compileOnly(it)
    }

    testImplementation(files(packageNativeForHost))
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        description = "${project.description}"
    }
}

nexusPublishing {
    this.repositories {
        sonatype()
    }
}

val mavenCentralDeploy by tasks.registering(DefaultTask::class) {
    group = "publishing"
    val isSnapshot = project.version.toString().endsWith("-SNAPSHOT")

    val publishTasks = allprojects
        .flatMap { it.tasks.withType<PublishToMavenRepository>() }
        .filter { it.repository.name == "sonatype" }
    dependsOn(publishTasks)
    if (!isSnapshot) {
        dependsOn(tasks.closeAndReleaseStagingRepositories)
    }
}