import java.util.Properties
import java.io.File

fun getLocalProperty(key: String, rootDir: File): String {
    val props = Properties()
    val file = File(rootDir, "local.properties")
    if (file.exists()) props.load(file.inputStream())
    return props.getProperty(key) ?: ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.massangermin"
    compileSdk = 36

    val key = getLocalProperty("supabaseKey", rootDir)
    val url = getLocalProperty("supabaseUrl", rootDir)

    defaultConfig {
        applicationId = "com.example.massangermin"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_KEY", "\"$key\"")
        buildConfigField("String", "SUPABASE_URL", "\"$url\"")
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
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")


    // Firebase
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.protolite.well.known.types)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Supabase v3
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.websockets)
    implementation("io.ktor:ktor-client-okhttp:3.3.0")

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}