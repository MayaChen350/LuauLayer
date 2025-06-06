plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
}

group = "evo"
version = "1.0-SNAPSHOT"

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    repositories {
        mavenCentral()
        maven {
            name = "devOS"
            url = uri("https://mvn.devos.one/releases")
        }
    }
}