import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// ─── Load local.properties (gitignored — never committed) ────────────────────
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

fun localProp(key: String, fallback: String = "MISSING_$key"): String =
    localProps.getProperty(key, fallback)

android {
    namespace = "me.madhushan.reflect"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "me.madhushan.reflect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── Facebook credentials injected from local.properties ──────────────
        // These become BuildConfig.FACEBOOK_APP_ID / BuildConfig.FACEBOOK_CLIENT_TOKEN
        // and are also exposed as string resources for the AndroidManifest meta-data.
        val fbAppId       = localProp("FACEBOOK_APP_ID")
        val fbClientToken = localProp("FACEBOOK_CLIENT_TOKEN")

        buildConfigField("String", "FACEBOOK_APP_ID",      "\"$fbAppId\"")
        buildConfigField("String", "FACEBOOK_CLIENT_TOKEN", "\"$fbClientToken\"")

        // resValues make the values available as @string/... in AndroidManifest.xml
        resValue("string", "facebook_app_id",          fbAppId)
        resValue("string", "facebook_client_token",    fbClientToken)
        resValue("string", "fb_login_protocol_scheme", "fb$fbAppId")

        // ── Google Sign-In Web Client ID injected from local.properties ──────
        val googleWebClientId = localProp("GOOGLE_WEB_CLIENT_ID")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        resValue("string", "default_web_client_id", googleWebClientId)
    }

    buildFeatures {
        buildConfig = true
        resValues  = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    androidResources {
        noCompress += "tflite"
    }
}

// After assembleDebug, copy the APK and rename it to REFLECT.apk
tasks.register<Copy>("buildReflectApk") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("*.apk")
    into(layout.buildDirectory.dir("outputs/apk/release_share"))
    rename { "REFLECT.apk" }
    doLast {
        println("✅ REFLECT.apk saved to: ${layout.buildDirectory.get()}/outputs/apk/release_share/REFLECT.apk")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    // ViewPager2 for onboarding
    implementation(libs.viewpager2)
    // Google Sign-In via Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
    // Glide — image loading for Google profile photo
    implementation(libs.glide)
    // TensorFlow Lite — on-device AI mood detection
    // v2.4.0 bundles everything in one jar (no -api split), so no duplicate namespace conflict
    implementation("org.tensorflow:tensorflow-lite:2.4.0")
    // Facebook Login SDK
    implementation(libs.facebook.android.sdk)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}