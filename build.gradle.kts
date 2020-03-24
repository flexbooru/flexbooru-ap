apply(plugin = "com.github.ben-manes.versions")

buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        val kotlinVersion = "1.3.71"
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("serialization", kotlinVersion))
        classpath("com.android.tools.build:gradle:4.0.0-beta03")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.28.0")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("io.fabric.tools:gradle:1.31.2")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
    }
    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class.java) {
            options.compilerArgs.plusAssign(listOf("-Xlint:deprecation"))
        }
    }
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}
