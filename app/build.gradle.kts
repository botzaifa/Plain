import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val signingProperties = Properties().apply {
    val propertiesFile = File(
        System.getProperty("user.home"),
        ".config/plain/signing.properties"
    )
    require(propertiesFile.exists()) {
        "Missing signing properties: ${propertiesFile.absolutePath}"
    }
    FileInputStream(propertiesFile).use { load(it) }
}

android {
    namespace = "app.plain.launcher"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.plain.launcher"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                signingProperties.getProperty("storeFile")
                    ?: error("Missing storeFile")
            )
            storePassword = signingProperties.getProperty("storePassword")
                ?: error("Missing storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
                ?: error("Missing keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
                ?: error("Missing keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    lint {
        abortOnError = true
        fatal += listOf("NewApi", "InlinedApi")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
}
