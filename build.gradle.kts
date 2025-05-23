plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.sun.mail:jakarta.mail:2.0.1")
}

tasks.test {
    useJUnitPlatform()
}