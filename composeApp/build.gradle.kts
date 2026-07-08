import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val versionProps = Properties().apply {
    load(File(rootDir, "version.properties").inputStream())
}
val customVersionCode = versionProps.getProperty("VERSION_CODE") ?: "1"
val customVersionName = versionProps.getProperty("VERSION_NAME") ?: "1.0.0"

// Supabase secrets live in local.properties (git-ignored). Base URL is public; the anon
// key is safe to ship in the client (RLS enforces access) but we still keep it out of git.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val supabaseUrl: String = localProps.getProperty("supabase.url") ?: ""
val supabaseAnonKey: String = localProps.getProperty("supabase.anonKey") ?: ""

kotlin {
    compilerOptions {
        // Room + our platform shims use expect/actual classes (still flagged Beta).
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
            // Ktor (raw client)
            implementation(libs.bundles.ktor)
            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // Koin
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // DataStore (tokens + settings)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            // Room (local cart)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            // Lottie splash + QR receipt
            implementation(libs.compottie)
            implementation(libs.qrose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.shopverse.cmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.shopverse.cmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionName = customVersionName
        versionCode = customVersionCode.toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Supabase config surfaced to commonMain via BuildKonfig-style generated constants.
// We keep it simple: BuildConfig on Android, and a generated Kotlin file for all targets.
val generateSupabaseConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/supabase/kotlin")
    outputs.dir(outputDir)
    val url = supabaseUrl
    val key = supabaseAnonKey
    doLast {
        val pkgDir = outputDir.get().dir("com/shopverse/cmp/network/service").asFile
        pkgDir.mkdirs()
        File(pkgDir, "SupabaseSecrets.kt").writeText(
            """
            package com.shopverse.cmp.network.service

            // GENERATED — do not edit. Values come from local.properties at build time.
            object SupabaseSecrets {
                const val URL: String = "$url"
                const val ANON_KEY: String = "$key"
            }
            """.trimIndent() + "\n"
        )
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir(generateSupabaseConfig)
}

dependencies {
    ksp(libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
