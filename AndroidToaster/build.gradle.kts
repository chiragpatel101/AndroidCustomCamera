plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish") // Apply plugin

}
// Declare library details
val mGroupId = "com.androidtoaster"
val mArtifactId = "toastermessage"
val mVersionCode = 8
val mVersionName = "1.0.9"

val mLibraryName = "toastermessage"
val mLibraryDescription = "Simple Library for simple things!"

android {
    namespace = "com.androidtoaster"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
// Declare task for creation of android sources.
val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

// Make configuration for publishing artifact.
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = mGroupId
                artifactId = mArtifactId
                version = mVersionName

                from(components["release"])

                artifact(androidSourcesJar)

                pom {
                    name.set(mLibraryName)
                    description.set(mLibraryDescription)
                }
            }
        }

        // Update repository details and credentials.
//        repositories {
//            maven {
//                name = "GitHubPackages"
//                url = uri("https://github.com/chiragpatel101/AndroidToaster")
//                credentials {
//                    username = System.getenv("GPR_USER")
//                    password = System.getenv("GPR_KEY")
//                }
//            }
//        }
    }
}

// Assembling should be performed before publishing package
