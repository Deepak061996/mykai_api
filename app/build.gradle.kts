plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.mykaimeal.planner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mykaimeal.planner"
        minSdk = 24
        targetSdk = 34

        ////for yesitlabs account
    /*    versionCode = 3
        versionName = "1.2"*/

        ///for my-kai app development
        versionCode = 4
        versionName = "1.3"

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

    buildFeatures {
        buildConfig = true
    }

    android {
        buildTypes {
            all {
                ///old URl
                /*buildConfigField("String", "BASE_URL", "\"https://myka.tgastaging.com/api/\"")
                buildConfigField("String", "IMAGE_BASE_URL", "\"https://myka.tgastaging.com\"")*/

                val url="\"https://admin.getmykai.com/api/\""
                val imageUrl="\"https://admin.getmykai.com\""

                buildConfigField("String", "BASE_URL", url)
                buildConfigField("String", "IMAGE_BASE_URL", imageUrl)


                

            }
        }


    }


    buildFeatures {
        viewBinding = true
        dataBinding= true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")
    implementation("com.google.firebase:firebase-common-ktx:21.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-inappmessaging-display:21.0.1")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.google.mlkit:image-labeling-custom-common:17.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    /// for gif use
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.17")

    //viewpager
    implementation("androidx.viewpager2:viewpager2:1.1.0")

//    implementation ("com.google.mlkit:image-labeling:17.0.7")


//    ////hfhffhf
//    implementation ("org.jsoup:jsoup:1.16.1")

 /*   ////for app link creating
    implementation ("com.leocardz:link-preview:1.3.3@aar")*/

    ///appsflyer sdk for deeplinking
    implementation("com.appsflyer:af-android-sdk:6.12.1")  // Check for the latest version on AppsFlyer's documentation.
    implementation("com.appsflyer:adrevenue:6.9.0")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation("com.google.code.gson:gson:2.10.1")

    //Retrofit for api
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")

    //Dimen
    implementation("com.intuit.ssp:ssp-android:1.0.5")
    implementation("com.intuit.sdp:sdp-android:1.0.6")

    ///for in app subscriptions
    val billing_version = "6.0.1"
    implementation("com.android.billingclient:billing-ktx:$billing_version")

    //otpView
    implementation("com.github.aabhasr1:OtpView:v1.1.2-ktx")

    //circularImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //bar chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

//    implementation("com.google.android.gms:play-services-maps:18.2.0")
    //image picker
    implementation("com.github.Dhaval2404:ImagePicker:v2.1")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    //google Firebase
    implementation("com.google.android.libraries.places:places:3.2.0")
    implementation("com.google.firebase:firebase-messaging:23.2.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

/*    ///appsFlyer sdk for deep linking
    implementation("com.appsflyer:af-android-sdk:6.11.0")*/

    ///app lottie for android
    implementation ("com.airbnb.android:lottie:3.4.0")

    //power spinner
    implementation("com.github.skydoves:powerspinner:1.2.7")

    ///for recycler view layout fix
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt ("com.google.dagger:hilt-android-compiler:2.47")
    kapt ("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.3.1")
    // firebase crashlytics
    implementation("com.google.firebase:firebase-crashlytics:18.2.9")
    implementation("com.google.firebase:firebase-analytics:20.1.2")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.activity:activity-ktx:1.3.1")
//    // progress bar circle
//    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    //Stripe SDK
    implementation ("com.stripe:stripe-android:16.3.0")
    implementation ("com.github.dewinjm:monthyear-picker:1.0.2")
    implementation ("com.github.jaiselrahman:FilePicker:1.3.2")
    //cameraX
    implementation ("androidx.camera:camera-core:1.4.1")
    implementation ("androidx.camera:camera-camera2:1.4.1")
    implementation ("androidx.camera:camera-lifecycle:1.4.1")
    implementation ("androidx.camera:camera-video:1.4.1")
    implementation ("androidx.camera:camera-view:1.4.1")
    implementation ("androidx.camera:camera-extensions:1.4.1")
    implementation ("com.google.guava:guava:32.0.1-jre")
    implementation ("com.hbb20:ccp:2.6.0")
    implementation ("com.github.chthai64:SwipeRevealLayout:1.4.0")

    implementation ("androidx.work:work-runtime-ktx:2.8.1")

    ///for phone number
    implementation ("com.googlecode.libphonenumber:libphonenumber:8.13.32") // or latest

    // Google Pay API
    implementation ("com.google.android.gms:play-services-wallet:19.3.0") // Required for Google Pay
}