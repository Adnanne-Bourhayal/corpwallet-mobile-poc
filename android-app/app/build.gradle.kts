import java.net.URI

plugins {
    id("com.android.application")
    kotlin("android")
}

val endpoint = providers.gradleProperty("walletApiUrl").orNull?.trimEnd('/')
endpoint?.let {
    val uri = URI(it)
    require(uri.scheme == "https" && uri.host != null && uri.userInfo == null &&
        uri.query == null && uri.fragment == null && uri.path.orEmpty().isEmpty()) {
        "walletApiUrl must be an HTTPS origin without credentials, path or query."
    }
}

android {
    namespace = "com.corpwallet.mobilepoc"
    compileSdk = 34
    buildFeatures { buildConfig = true }

    defaultConfig {
        applicationId = "com.corpwallet.mobilepoc"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE", "\"" + (endpoint ?: "http://127.0.0.1:8000") + "\"")
            buildConfigField("boolean", "R8_ENABLED", "false")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            buildConfigField("String", "API_BASE", "\"" + (endpoint ?: "") + "\"")
            buildConfigField("boolean", "R8_ENABLED", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
