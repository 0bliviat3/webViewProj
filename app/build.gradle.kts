import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.devtool.webview"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.devtool.webview"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Debug → 항상 테스트 광고
            val testAppId = "ca-app-pub-3940256099942544~3347511713"
            val testOpenId = "ca-app-pub-3940256099942544/9257395921"
            buildConfigField("String", "AD_ID", "\"$testAppId\"")
            buildConfigField("String", "AD_OPEN_ID", "\"$testOpenId\"")
            manifestPlaceholders["AD_ID"] = testAppId

        }
        release {
            manifestPlaceholders += mapOf()
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val isProduction = localProperties.getProperty("IS_PRODUCTION") ?: "false"
            var appId = "ca-app-pub-3940256099942544~3347511713"
            var openingAdId = "ca-app-pub-3940256099942544/9257395921"
            if (isProduction == "true") {
                // 값읽기... 없다면 test ID
                appId = localProperties.getProperty("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
                openingAdId = localProperties.getProperty("ADMOB_OPEN_ID") ?: "ca-app-pub-3940256099942544/9257395921"
            }

            buildConfigField("String", "AD_ID", "\"$appId\"")
            buildConfigField("String", "AD_OPEN_ID", "\"$openingAdId\"")
            signingConfig = signingConfigs.getByName("debug")

            manifestPlaceholders["AD_ID"] = appId
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("com.google.android.gms:play-services-ads:24.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}