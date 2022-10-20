import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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
    compileSdk = 33
    defaultConfig {
        applicationId = "onlymash.flexbooru.ap"
        minSdk = 21
        targetSdk = 33
        versionCode = verCode
        versionName = "1.4.2"
        versionNameSuffix = ".c$verCode"
        resourceConfigurations += setOf("en", "zh-rCN", "zh-rHK", "zh-rTW", "ru-rRU", "pt-rBR", "es-rES", "nl-rNL")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.incremental" to "true")
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
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=androidx.paging.ExperimentalPagingApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
    compileOptions {
//        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kapt {
        useBuildCache = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "onlymash.flexbooru.ap"
}

dependencies { 
    val materialVersion = "1.8.0-alpha01"
    val lifecycleVersion = "2.6.0-alpha02"
    val navVersion = "2.6.0-alpha02"
    val workVersion = "2.8.0-beta01"
    val roomVersion = "2.5.0-beta01"
    val okhttpVersion = "5.0.0-alpha.10"
    val retrofitVersion = "2.9.0"
    val glideVersion = "4.14.2"
    val markwonVersion = "4.6.2"
    val kodeinVersion = "7.15.0"
    val coroutinesVersion = "1.6.4"
    val serializationVersion = "1.4.1"

//    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
    implementation("org.kodein.di:kodein-di-framework-android-core:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x-viewmodel-savedstate:$kodeinVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.annotation:annotation:1.5.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.6.0-alpha03")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0-rc01")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0-alpha01")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha04")
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
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.onlymash:subsampling-scale-image-view:3.10.3")
    implementation("com.google.android.apps.muzei:muzei-api:3.4.1")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:image-glide:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion")
    implementation("io.noties.markwon:ext-strikethrough:$markwonVersion")
    implementation("io.noties.markwon:linkify:$markwonVersion")
    implementation("me.saket:better-link-movement-method:2.2.0")
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.0")
    implementation("com.google.firebase:firebase-crashlytics:18.2.13")
    implementation("com.google.android.datatransport:transport-runtime:3.1.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    androidTestImplementation("androidx.test:runner:1.5.0-beta01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0-beta01")
}