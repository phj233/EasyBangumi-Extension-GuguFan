
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val keystoreProperties = Properties().apply {
    // 尝试从文件加载密钥库信息
    try {
        load(file("src/keystore.properties").reader())
    } catch (e: Exception) {
        // 如果文件不存在，则使用环境变量
        setProperty("storeFile", System.getenv("SIGNING_KEY"))
        setProperty("storePassword", System.getenv("KEY_STORE_PWD"))
        setProperty("keyAlias", System.getenv("KEY_ALIAS"))
        setProperty("keyPassword", System.getenv("KEY_PWD"))
    }
}

android {
    namespace = "top.phj233.easybangumi_extension_gugufan"
    compileSdk = 34

//    signingConfigs {
//        create("release"){
//            storeFile = file(keystoreProperties.getProperty("storeFile"))
//            storePassword = keystoreProperties.getProperty("storePassword")
//            keyAlias = keystoreProperties.getProperty("keyAlias")
//            keyPassword = keystoreProperties.getProperty("keyPassword")
//        }
//    }
    defaultConfig {
        applicationId = "top.phj233.easybangumi_extension_gugufan"
        minSdk = 24
        targetSdk = 34
        versionCode = 11
        versionName = "1.3.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        archivesName.set("纯纯看番咕咕番插件_${versionName}")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    dependenciesInfo{
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    compileOnly("io.github.easybangumiorg:extension-api:1.11-SNAPSHOT")
    implementation("org.jsoup:jsoup:1.18.1")
}
repositories {
}
