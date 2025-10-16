plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.rudy.lcoolougasolina"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rudy.lcoolougasolina"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Adicione o vetor de drawables para o Compose
        vectorDrawables {
            useSupportLibrary = true
        }
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
    }

    // Correção: Este bloco foi removido. A configuração agora é feita pelo plugin.
    // composeCompiler{
    //
    // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v1120)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4") // Use libs.androidx.lifecycle.runtime.ktx se estiver no TOML
    implementation(libs.material.v1100)

    // Compose BOM (Bill of Materials) - Gerencia as versões das bibliotecas do Compose
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))

    // Dependências do Compose
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material") // Você pode usar material3 se preferir
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)

    // Ferramentas de debug para o Compose
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.test:monitor:1.7.1") // Adicionado para tooling

    // (opcional) ViewModel + SavedState para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2") // Use libs.androidx.lifecycle.viewmodel.compose se estiver no TOML

    // Testes
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.00")) // BOM para testes de UI
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    //
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    // Coroutines / Lifecycle
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // DataStore Preferences
        implementation("androidx.datastore:datastore-preferences:1.1.0")

// Gson para (de)serializar lista de postos
    implementation("com.google.code.gson:gson:2.10.1")

// Compose (caso ainda não tenha)
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

// lifecycle / viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")


}
