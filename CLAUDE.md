# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Trading simulator built with Kotlin and Gradle. Uses Kotlin JVM 2.2.21, targeting Java 17.

**Package:** `com.obabichev.trading`
**Version:** `1.0-SNAPSHOT`

## Build Commands

```bash
# Build and test
./gradlew build                # Full build with tests
./gradlew test                 # Run all tests
./gradlew test --tests "ClassName.methodName"  # Run specific test

# Development
./gradlew assemble             # Compile without tests
./gradlew clean build          # Clean build
./gradlew classes              # Compile main sources only
./gradlew compileKotlin        # Compile Kotlin sources
./gradlew compileTestKotlin    # Compile test sources

# Verification
./gradlew check                # Run all checks including tests
```

## Project Structure

```
src/
├── main/kotlin/              # Main source code
│   └── Main.kt               # Application entry point
└── test/kotlin/              # Test sources (JUnit Platform)
```

## Code Standards

- Kotlin code style: Official (configured in gradle.properties)
- Test framework: JUnit Platform
- Tests should mirror main source package structure in `src/test/kotlin/`
