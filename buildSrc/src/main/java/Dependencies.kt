object Versions {
    const val compileSdk = 29
    const val minSdk = 21
    const val targetSdk = 29
    const val kotlin = "1.4.10"
    const val coroutines = "1.3.8"
    const val dokka = "0.10.1"
    const val androidXLifecycle = "2.3.0-alpha07"
    const val androidXFragment = "1.3.0-alpha08"
    const val androidXDataStore = "1.0.0-alpha01"
    const val androidXTest = "1.3.0"
    const val navigation = "2.3.0"
    const val okhttp3 = "4.9.0"
    const val dagger = "2.29.1"
    const val daggerHilt = "2.29-alpha"
    const val glide = "4.11.0"
    const val groupie = "2.8.1"
    const val flowBinding = "0.12.0"
    const val rxBinding = "3.1.0"
    const val protobuf = "3.13.0"
    const val leakcanary = "2.4"
    const val hyperion = "0.9.29"
    const val robolectric = "4.4"
    const val espresso = "3.3.0"
}

object Deps {

    object Plugins {
        const val android = "com.android.tools.build:gradle:4.0.1"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
        const val versions = "com.github.ben-manes:gradle-versions-plugin:0.33.0"
        const val kotlinxcodesync = "co.touchlab:kotlinxcodesync:0.2"
        const val googleservices = "com.google.gms:google-services:4.3.3"
        const val navSafeArgs =
            "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.3.0"
        const val dexcount = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:2.0.0-RC1"
        const val jacoco = "org.jacoco:org.jacoco.core:0.8.6"
    }

    object Modules {
        const val app = ":app"
        const val data = ":data"
    }

    object Kotlin {
        object Stdlib {
            const val jdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
            const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        }

        object Coroutines {
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
            const val rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"
        }
    }

    object Androidx {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.1"
        const val sqlite = "androidx.sqlite:sqlite:2.0.1"
        const val browser = "androidx.browser:browser:1.3.0-alpha05"
        const val viewpager = "androidx.viewpager2:viewpager2:1.1.0-alpha01"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0-alpha05"
        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val preference = "androidx.preference:preference:1.1.1"

        const val ktx = "androidx.core:core-ktx:1.3.1"
        const val fragment = "androidx.fragment:fragment:${Versions.androidXFragment}"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.androidXFragment}"

        object Lifecycle {
            const val runtime = "androidx.lifecycle:lifecycle-runtime:${Versions.androidXLifecycle}"
            const val process = "androidx.lifecycle:lifecycle-process:${Versions.androidXLifecycle}"
            const val commonJava8 =
                "androidx.lifecycle:lifecycle-common-java8:${Versions.androidXLifecycle}"
        }

        object Navigation {
            const val fragment =
                "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
            const val ui = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
        }

        object DataStore {
            const val core = "androidx.datastore:datastore-core:${Versions.androidXDataStore}"
        }

        const val viewmodel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidXLifecycle}"

        const val webkit = "androidx.webkit:webkit:1.3.0"

        const val multidex = "androidx.multidex:multidex:2.0.1"

        object Test {
            const val core = "androidx.test:core-ktx:${Versions.androidXTest}"
            const val runner = "androidx.test:runner:${Versions.androidXTest}"
            const val rules = "androidx.test:rules:1.1.0"
            const val ext = "androidx.test.ext:junit:1.1.2"
            const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
            const val espressoContrib =
                "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
        }
    }

    const val material = "com.google.android.material:material:1.0.0"

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:25.10.0"
        const val messaging = "com.google.firebase:firebase-messaging"
        const val crashlytics = "com.google.firebase:firebase-crashlytics"
        const val analytics = "com.google.firebase:firebase-analytics"
    }

    object Dagger {
        const val core = "com.google.dagger:dagger:${Versions.dagger}"
        const val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
        const val hilt = "com.google.dagger:hilt-android:${Versions.daggerHilt}"
        const val hiltCompiler = "com.google.dagger:hilt-android-compiler:${Versions.daggerHilt}"
        const val hiltTesting = "com.google.dagger:hilt-android-testing:${Versions.daggerHilt}"
    }

    object Glide {
        const val core = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val okhttp = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
        const val recyclerview =
            "com.github.bumptech.glide:recyclerview-integration:${Versions.glide}"
        const val compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    }

    object RxJava2 {
        const val core = "io.reactivex.rxjava2:rxandroid:2.1.1"
        const val android = "io.reactivex.rxjava2:rxjava:2.2.19"
        const val kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
        const val proguard = "com.artemzin.rxjava:proguard-rules:1.3.3.0"
    }

    object RxBinding {
        const val core = "com.jakewharton.rxbinding3:rxbinding-core:${Versions.rxBinding}"
        const val appcompat =
            "com.jakewharton.rxbinding3:rxbinding-appcompat:${Versions.rxBinding}"
        const val recyclerview =
            "com.jakewharton.rxbinding3:rxbinding-recyclerview:${Versions.rxBinding}"
    }

    object LeakCanary {
        const val core = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"
        const val objectWatcher =
            "com.squareup.leakcanary:leakcanary-object-watcher-android:${Versions.leakcanary}"
        const val plumber = "com.squareup.leakcanary:plumber-android:${Versions.leakcanary}"
    }

    object OkHttp3 {
        const val core = "com.squareup.okhttp3:okhttp:${Versions.okhttp3}"
        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp3}"
    }

    object Protobuf {
        const val core = "com.google.protobuf:protobuf-javalite:${Versions.protobuf}"
        const val protoc = "com.google.protobuf:protoc:${Versions.protobuf}"
    }

    const val adapterDelegates = "com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:4.3.0"

    const val threetenabp = "com.jakewharton.threetenabp:threetenabp:1.2.4"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val licenseAdapter = "net.yslibrary.licenseadapter:licenseadapter:3.1.0"

    const val twitterText = "com.twitter.twittertext:twitter-text:3.1.0"


    // test
    const val junit = "junit:junit:4.13"
    const val robolectric = "org.robolectric:robolectric:4.4"
    const val mockk = "io.mockk:mockk:1.10.0"
    const val truth = "com.google.truth:truth:1.0.1"
}