plugins {
    kotlin("jvm") version "2.2.21"
    id("com.obabichev.kodama") version "0.3.0"
    id("org.flywaydb.flyway") version "10.21.0"
    application
}

group = "com.obabichev.trading"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kodama
    implementation("com.obabichev.kodama:kodama-core:0.3.0")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // Flyway
    implementation("org.flywaydb:flyway-core:10.21.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.21.0")

    // Logging (required by Kodama)
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")

    // Testing
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")  // Required for Kodama Entity Layer
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.obabichev.trading.MainKt")
}

// Task to generate sample data
tasks.register<JavaExec>("generateData") {
    group = "data"
    description = "Generate sample market data"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.obabichev.trading.data.SampleDataGeneratorKt")
}