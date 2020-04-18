import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
    id("io.fabric")
}

val properties = Properties()
properties.load(project.rootProject.file(".gradle/keystore.properties").inputStream())
val byteOut = org.apache.commons.io.output.ByteArrayOutputStream()
project.exec {
    commandLine = "git rev-list HEAD --first-parent --count".split(" ")
    standardOutput = byteOut
}
val verCode = String(byteOut.toByteArray()).trim().toInt()

android {
    signingConfigs {
        create("release") {
            storeFile = file("../.gradle/keystore.jks")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASS")
            storePassword = properties.getProperty("STORE_PASS")
        }
    }
    compileSdkVersion(29)
    buildToolsVersion = "29.0.3"
    defaultConfig {
        applicationId = "onlymash.flexbooru.ap"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = verCode
        versionName = "1.2.1"
        versionNameSuffix = ".c$verCode"
        resConfigs(listOf("en", "zh-rCN", "ru-rRU", "zh-rHK", "pt-rBR"))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.incremental" to "true")
            }
        }
    }
    applicationVariants.all {
        outputs.map {
            it as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        }
            .forEach { output ->
                output.outputFileName = "flexbooru-ap_${defaultConfig.versionName}${defaultConfig.versionNameSuffix}.apk"
            }
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlinx.serialization.UnstableDefault",
            "-Xopt-in=kotlinx.serialization.ImplicitReflectionSerializer"
        )
    }
    compileOptions {
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kapt {
        useBuildCache = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies { 
    val materialVersion = "1.2.0-alpha06"
    val lifecycleVersion = "2.3.0-alpha01"
    val navVersion = "2.3.0-alpha05"
    val workVersion = "2.4.0-alpha02"
    val roomVersion = "2.2.5"
    val okhttpVersion = "4.5.0"
    val retrofitVersion = "2.8.1"
    val glideVersion = "4.11.0"
    val markwonVersion = "4.3.1"
    val kodeinVersion = "6.5.5"
    val coroutinesVersion = "1.3.5"
    val serializationVersion = "0.20.0"

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.5")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.1.0")
    implementation("org.kodein.di:kodein-di-erased-jvm:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodeinVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.appcompat:appcompat:1.2.0-beta01")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.fragment:fragment-ktx:1.3.0-alpha03")
    implementation("androidx.core:core-ktx:1.3.0-rc01")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha02")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0-rc01")
    implementation("androidx.cardview:cardview:1.0.0")
//    implementation("androidx.multidex:multidex:2.0.1"
    implementation("androidx.browser:browser:1.3.0-alpha01")
    implementation("androidx.drawerlayout:drawerlayout:1.1.0-beta01")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta4")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Lifecycles only (without ViewModel or LiveData)
    // implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.paging:paging-runtime-ktx:2.1.2")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.onlymash:subsampling-scale-image-view:3.10.3")
    implementation("com.google.android.apps.muzei:muzei-api:3.2.0")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okio:okio:2.5.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.5.0")
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:image-glide:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion")
    implementation("io.noties.markwon:ext-strikethrough:$markwonVersion")
    implementation("io.noties.markwon:linkify:$markwonVersion")
    implementation("me.saket:better-link-movement-method:2.2.0")
    implementation("com.google.firebase:firebase-core:17.3.0")
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    androidTestImplementation("androidx.test:runner:1.3.0-alpha05")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0-alpha05")
}