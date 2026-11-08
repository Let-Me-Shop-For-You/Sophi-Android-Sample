import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Tenant API keys live only in the gitignored local.properties (see local.properties.example);
// a missing file must not break the build, it just leaves every tenant without a key.
val props = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

fun key(name: String): String = "\"${props.getProperty(name, "")}\""

android {
    namespace = "com.sophi.sample"
    compileSdk = 36
    sourceSets.getByName("main").kotlin.srcDir("../shared/src/main/java")

    defaultConfig {
        applicationId = "com.sophi.sample"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SOPHI_ENV", key("SOPHI_ENV"))
        buildConfigField("String", "VAKKO_API_KEY", key("VAKKO_API_KEY"))
        buildConfigField("String", "MNS_API_KEY", key("MNS_API_KEY"))
        buildConfigField("String", "KIKO_API_KEY", key("KIKO_API_KEY"))
        buildConfigField("String", "BOYNER_API_KEY", key("BOYNER_API_KEY"))
        buildConfigField("String", "BEYMEN_API_KEY", key("BEYMEN_API_KEY"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)


    // Sophi Android SDK
    implementation(libs.sophi.android)
}