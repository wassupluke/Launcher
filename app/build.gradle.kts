plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "de.jrpie.android.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.jrpie.android.launcher"
        minSdk = 21
        targetSdk = 35
        versionCode = 46
        versionName = "0.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("default") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "USE_ACCESSIBILITY_SERVICE", "true")
        }
        create("accrescent") {
            dimension = "distribution"
            applicationIdSuffix = ".accrescent"
            versionNameSuffix = "+accrescent"
            buildConfigField("boolean", "USE_ACCESSIBILITY_SERVICE", "false")
        }
    }

    sourceSets {
        this.getByName("accrescent") {
            this.java.srcDir("src/accrescent")
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
        buildConfig = true
        compose = true
        dataBinding = true
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation(libs.kotlin.stdlib.jdk7)

    // implementation 'androidx.activity:activity-ktx:1.8.0'
    implementation(libs.androidx.activity.ktx)

    // implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation(libs.androidx.appcompat)

    // implementation 'androidx.core:core-ktx:1.15.0'
    implementation(libs.androidx.core.ktx)

    // implementation 'androidx.constraintlayout:constraintlayout:2.2.0'
    implementation(libs.androidx.constraintlayout)

    // implementation 'androidx.gridlayout:gridlayout:1.0.0'
    implementation(libs.androidx.gridlayout)

    // implementation 'androidx.palette:palette-ktx:1.0.0'
    implementation(libs.androidx.palette.ktx)

    // implementation 'androidx.recyclerview:recyclerview:1.4.0'
    implementation(libs.androidx.recyclerview)

    // implementation 'androidx.preference:preference-ktx:1.2.1'
    implementation(libs.androidx.preference.ktx)

    // implementation 'com.google.android.material:material:1.12.0'
    implementation(libs.google.material)

    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation(libs.kotlinx.serialization.json)

    // implementation "eu.jonahbauer:android-preference-annotations:1.1.2"
    implementation(libs.jonahbauer.android.preference.annotations)

    // implementation 'androidx.activity:activity:1.10.1'
    implementation(libs.androidx.activity)

    // annotationProcessor "eu.jonahbauer:android-preference-annotations:1.1.2"
    // If you are using KSP, use ksp(...) instead of kapt(...) or annotationProcessor(...)
    // Otherwise, if you still need kapt:
    // kapt(libs.jonahbauer.android.preference.annotations)
    // Or if you need annotationProcessor for Java libraries:
    kapt(libs.jonahbauer.android.preference.annotations)

    // testImplementation 'junit:junit:4.13.2'
    testImplementation(libs.junit)

    // androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation(libs.androidx.test.ext.junit)

    // androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}