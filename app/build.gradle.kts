import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.ssajudn.tarsika"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ssajudn.hushkeep"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        checkDependencies = true
        warningsAsErrors = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ktlint {
    android.set(true)
    filter {
        include("**/src/**/*.kt")
        exclude("**/build/**")
    }
}

val ktlintSourceRuntime = configurations.create("ktlintSourceRuntime")

dependencies {
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.biometric)
    ksp(libs.androidx.room.compiler)
    add(ktlintSourceRuntime.name, "com.pinterest.ktlint:ktlint-cli:1.0.1")
}

tasks.register<JavaExec>("ktlintSourceCheck") {
    group = "verification"
    description = "Runs KtLint against Android Kotlin source sets."
    classpath(ktlintSourceRuntime)
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "--relative",
        "src/main/java/**/*.kt",
        "src/test/java/**/*.kt",
        "src/androidTest/java/**/*.kt",
    )
}

tasks.register<JavaExec>("ktlintSourceFormat") {
    group = "formatting"
    description = "Formats Android Kotlin source sets with KtLint."
    classpath(ktlintSourceRuntime)
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "--format",
        "src/main/java/**/*.kt",
        "src/test/java/**/*.kt",
        "src/androidTest/java/**/*.kt",
    )
}

tasks.named("check") {
    dependsOn("ktlintSourceCheck")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val coverageClassExcludes =
    listOf(
        "**/R.class",
        "**/R${'$'}*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/databinding/**",
        "**/androidx/**",
        "**/app/di/**",
        "**/core/ui/**",
        "**/feature/**/components/**",
        "**/feature/**/*Screen*.*",
        "**/feature/**/*Dialog*.*",
        "**/feature/**/*Content*.*",
        "**/feature/**/*Fields*.*",
        "**/feature/**/*Selector*.*",
        "**/feature/**/*Item*.*",
        "**/feature/**/*Row*.*",
        "**/navigation/**",
        "**/ui/theme/**",
        "**/AppContent.*",
        "**/AppMessageHost.*",
        "**/TarsikaApp.*",
        "**/TarsikaRoot.*",
        "**/TarsikaApplication.*",
        "**/MainActivity.*",
        "**/SessionLifecycleEffect.*",
    )

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val kotlinClasses =
        layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    val javaClasses =
        layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")

    classDirectories.setFrom(
        files(kotlinClasses, javaClasses).asFileTree.matching {
            exclude(coverageClassExcludes)
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")

    val kotlinClasses =
        layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    val javaClasses =
        layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")

    classDirectories.setFrom(
        files(kotlinClasses, javaClasses).asFileTree.matching {
            exclude(coverageClassExcludes)
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                // Temporary business-logic baseline; raise this as repository and worker tests grow.
                minimum = "0.04".toBigDecimal()
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
