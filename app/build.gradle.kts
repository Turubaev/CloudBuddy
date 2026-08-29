plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.catandbunny.cloudbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.catandbunny.cloudbuddy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val backendUrl = providers.gradleProperty("CLOUDBUDDY_BACKEND_URL")
            .orElse("")
            .get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        buildConfigField("String", "CLOUDBUDDY_BACKEND_URL", "\"$backendUrl\"")

        val rustoreConsoleAppId = providers.gradleProperty("RUSTORE_CONSOLE_APP_ID")
            .orElse("")
            .get()
        val rustorePlusProductId = providers.gradleProperty("RUSTORE_PLUS_PRODUCT_ID")
            .orElse("cloudbuddy_plus_monthly")
            .get()
        buildConfigField("String", "RUSTORE_CONSOLE_APP_ID", "\"$rustoreConsoleAppId\"")
        buildConfigField("String", "RUSTORE_PLUS_PRODUCT_ID", "\"$rustorePlusProductId\"")
        resValue("string", "rustore_console_app_id", rustoreConsoleAppId.ifBlank { "not_configured" })
        resValue("string", "rustore_deeplink_scheme", "cloudbuddy-pay")
        manifestPlaceholders["rustoreDeeplinkScheme"] = "cloudbuddy-pay"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(platform(libs.rustore.bom))
    implementation(libs.rustore.pay)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
