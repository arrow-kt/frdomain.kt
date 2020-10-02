plugins {
    kotlin("jvm")
}

group = "io.arrow-kt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val arrow_version = "0.11.0"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
    implementation("io.arrow-kt:arrow-syntax:$arrow_version")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}