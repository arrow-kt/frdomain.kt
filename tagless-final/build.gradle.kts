plugins {
    kotlin("jvm")
}

group = "io.arrow-kt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val arrow_version = "0.10.5"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.arrow-kt:arrow-fx:$arrow_version")
    implementation("io.arrow-kt:arrow-syntax:$arrow_version")

    implementation("io.arrow-kt:arrow-mtl:$arrow_version")
    implementation("io.arrow-kt:arrow-mtl-data:$arrow_version")

    implementation("io.arrow-kt:arrow-fx-rx2:$arrow_version")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}