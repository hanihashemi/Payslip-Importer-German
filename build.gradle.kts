plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.pdfbox:pdfbox:2.0.28")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}