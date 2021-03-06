plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "org.lmarek"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // kotlin
    val kotlinVersion = "1.5.10"
    val coroutinesVersion = "1.5.0"
    implementation(kotlin("stdlib-jdk8:$kotlinVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // cli-specific
    val picoCliVersion = "4.6.1"
    implementation("info.picocli:picocli:$picoCliVersion")
    kapt("info.picocli:picocli-codegen:$picoCliVersion")

    // search engine
    val luceneVersion = "8.8.2"
    implementation("org.apache.lucene:lucene-core:$luceneVersion")
    implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")

    // dependency injection
    implementation("org.koin:koin-core:2.2.2")

    // tests
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("io.strikt:strikt-core:0.31.0")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        apiVersion = "1.5"
        languageVersion = "1.5"
        javaParameters = true
        jvmTarget = "1.8"
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set(project.name)
    manifest {
        attributes["Main-Class"] = "org.lmarek.memory.refresher.MainKt"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}