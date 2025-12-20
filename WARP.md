# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Kotlin-based trading simulator project built with Gradle. The project uses Kotlin JVM version 2.2.21 and targets JVM 17.

## Build System

The project uses Gradle 9.0.0 with the Kotlin DSL for build configuration.

### Common Commands

#### Building the Project
```bash
./gradlew build           # Compile and test the entire project
./gradlew assemble        # Compile without running tests
./gradlew clean build     # Clean build from scratch
./gradlew classes         # Compile main sources only
```

#### Running Tests
```bash
./gradlew test            # Run all tests
./gradlew test --tests "TestClassName"  # Run specific test class
./gradlew test --tests "TestClassName.testMethodName"  # Run specific test method
./gradlew check           # Run all verification tasks including tests
```

#### Development Tasks
```bash
./gradlew compileKotlin   # Compile Kotlin sources only
./gradlew compileTestKotlin  # Compile test Kotlin sources
./gradlew clean           # Clean build directory
./gradlew tasks --all     # List all available tasks
```

#### Creating Distributions
```bash
./gradlew jar             # Create JAR file with compiled classes
./gradlew kotlinSourcesJar  # Create source JAR
```

## Project Structure

```
trading-simulator/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── Main.kt          # Main entry point
│   └── test/
│       └── kotlin/              # Test sources directory
├── build.gradle.kts             # Main build configuration
├── settings.gradle.kts          # Project settings
└── gradle.properties            # Gradle properties
```

## Code Architecture

### Package Structure
- **Base Package**: `com.obabichev.trading`
- **Main Entry Point**: `Main.kt` contains the application entry point

### Build Configuration Details
- **Group ID**: `com.obabichev.trading`
- **Version**: `1.0-SNAPSHOT`
- **JVM Target**: Java 17
- **Kotlin Code Style**: Official (as per gradle.properties)
- **Test Framework**: JUnit Platform

## Development Guidelines

### Kotlin Conventions
The project follows the official Kotlin code style as configured in `gradle.properties`.

### Testing Strategy
Tests should be placed in `src/test/kotlin` following the same package structure as the main sources. The project is configured to use JUnit Platform for test execution.

### Running the Application
To run the main application directly:
```bash
./gradlew run
```
Or compile and run manually:
```bash
./gradlew classes
kotlin -cp build/classes/kotlin/main com.obabichev.trading.MainKt
```