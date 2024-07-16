plugins {
    id("tel.schich.libdatachannel.convention.common")
}

val nativeLibs by configurations.registering

dependencies {
    api(rootProject)
    nativeLibs(project(mapOf("path" to rootProject.path, "configuration" to "archDetectConfiguration")))
}

tasks.jar.configure {
    dependsOn(nativeLibs)
    for (jar in nativeLibs.get().resolvedConfiguration.resolvedArtifacts) {
        val classifier = jar.classifier ?: continue
        from(zipTree(jar.file)) {
            include("native/*.so")
            include("native/*.dll")
            into(classifier)
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        description = "${rootProject.description} The ${project.name} module bundles all architectures and allows runtime architecture detection."
    }
}