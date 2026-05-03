plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.purplediary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.purplediary"
        minSdk = 35

        // ================================================================
        // PERBAIKAN PALING PENTING: Turunkan minSdk agar aplikasi bisa di-install.
        // ================================================================

        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Dependensi dari libs catalog
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Dependensi lain
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Dependensi duplikat sudah saya hapus untuk merapikan

    // Dependensi untuk testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

