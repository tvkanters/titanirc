import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    id("org.hidetake.ssh") version "2.10.1"
}

group = "com.tvkdevelopment.titanirc"
version = "1.0"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("com.github.pircbotx:pircbotx:2.3.1")
    implementation("org.slf4j:slf4j-log4j12:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("dev.kord:kord-core:0.13.1")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

val jar by tasks.getting(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Main-Class"] = "com.tvkdevelopment.titanirc.Main"
    }
    destinationDirectory.set(project.file("exe"))
    archiveFileName.set("titanirc.jar")

    // Put dependencies in JAR to include things like the Kotlin stdlib
    from(sourceSets.getByName("main").output)

    dependsOn(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}

apply(from = "deploy.gradle")