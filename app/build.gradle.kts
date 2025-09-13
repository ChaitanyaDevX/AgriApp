plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // Firebase plugin
}

android {
    namespace = "com.example.agrimall" // <-- replace with your package name
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.agrimall" // <-- replace with your package name
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    // Firebase BoM (manages versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase core features
    implementation("com.google.firebase:firebase-database") // Realtime DB
    implementation("com.google.firebase:firebase-auth")     // Optional login
    implementation("com.google.firebase:firebase-analytics") // Optional analytics
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.razorpay:checkout:1.6.40")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.firebase:firebase-bom:32.2.2")
    implementation("com.google.firebase:firebase-storage")
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.google.firebase.firestore)
    implementation(libs.google.firebase.storage)// Optional analytics
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    annotationProcessor("com.razorpay:checkout:1.6.40")
}
