plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "org.lmarek"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.apache.lucene:lucene-core:8.8.2")

    testImplementation("io.strikt:strikt-core:0.31.0")
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        apiVersion = "1.5"
        languageVersion = "1.5"
        javaParameters = true
        jvmTarget = "11"
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set(project.name)
    manifest {
        attributes["Main-Class"] = "ApplicationKt"
    }
}