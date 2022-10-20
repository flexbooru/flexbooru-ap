plugins {
    id("com.github.ben-manes.versions") version("0.43.0")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        val kotlinVersion = "1.7.20"
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("serialization", kotlinVersion))
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
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