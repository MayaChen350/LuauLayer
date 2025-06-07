plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

group = rootProject.group
version = rootProject.version

dependencies {
    ksp(project(":processor"))

    implementation(libs.luau)
    implementation(libs.okio)
    implementation(libs.bundles.prettylog)
    implementation(libs.bundles.kotlinx)
    implementation(files("libs/luau-natives-windows-x64-dev.jar"))

    api(project(":annotations"))
}

kotlin {
    jvmToolchain(21)

    sourceSets.main {
        kotlin {
            srcDir("src/main/kotlin")
            srcDir(layout.buildDirectory.dir("generated/ksp/main/kotlin"))
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xnon-local-break-continue", "-Xmulti-dollar-interpolation", "-Xcontext-parameters")
        //extraWarnings.set(true)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"

        options.compilerArgs.addAll(
            listOf(
                "--enable-preview",
                "-Xlint:all",
                "-Xdiags:verbose"
            )
        )
    }
}
