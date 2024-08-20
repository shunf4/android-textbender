plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

if (file("sign.gradle").exists()) {
    apply {
      from("sign.gradle")
    }
}


android {
  namespace = "sh.eliza.textbender"
  compileSdk = 33
  buildToolsVersion = "33.0.2"

  defaultConfig {
    applicationId = "sh.eliza.textbender"
    minSdk = 26
    targetSdk = 33
    versionCode = 4
    versionName = "0.3.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

      
            if (file("sign.gradle").exists()) {
                // Local environment
                signingConfig = signingConfigs.getByName("release")
                // isDebuggable = true

                /* sign.gradle is like:
android {
    signingConfigs {
        release {
            keyAlias 'aliasxxx'
            keyPassword 'passwordxxx'
            storeFile file('../xxx.jks')
            storePassword 'passwordxxx'
        }
    }
}
GitHub config is like:
SIGNING_KEY:
`openssl base64 < some_signing_key.jks | tr -d '\n'`
ALIAS:
KEY_STORE_PASSWORD:
KEY_PASSWORD:
*/
            }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = "11" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.10.1")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.9.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
  implementation("androidx.preference:preference:1.2.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
