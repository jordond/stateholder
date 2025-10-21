# State Holder

![Maven Central](https://img.shields.io/maven-central/v/dev.stateholder/core)
[![Kotlin](https://img.shields.io/badge/kotlin-v2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Build](https://github.com/jordond/state-holder/actions/workflows/ci.yml/badge.svg)](https://github.com/jordond/state-holder/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/jordond/state-holder)](http://www.apache.org/licenses/LICENSE-2.0)

[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-v1.9.1-rc01-blue)](https://github.com/JetBrains/compose-multiplatform)
![badge-jvm](http://img.shields.io/badge/platform-jvm-6EDB8D.svg?style=flat)
![badge-apple](http://img.shields.io/badge/platform-ios%2Fmacos%2Fwatchos%2Ftvos-CDCDCD.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js-F7DF1E.svg?style=flat)

A simple library for managing state in Kotlin Multiplatform projects, using Kotlin Coroutines
and `StateFlow`.

You can view the KDocs at [docs.stateholder.dev](https://docs.stateholder.dev).

**Note**: This library is still in alpha, documentation is not finished.

## Table of Contents

- [Motivation](#motivation)
    - [Planned Features](#planned-features)
- [Setup](#setup)
    - [Multiplatform](#multiplatform)
    - [Android](#android)
    - [Version Catalog](#version-catalog)
- [Usage](#usage)
    - [Creating a StateHolder](#creating-a-stateholder)
    - [Updating State](#updating-state)
    - [Consuming State](#consuming-state)
    - [Android Extensions](#android-extensions)
- [License](#license)

## Motivation

State Holder is a library that aims to make state management in Kotlin Multiplatform projects
easier. It provides a simple API for creating and managing state with very little boilerplate. It
provides some extensions to make consuming the state easier (currently only Android).

This library is very un-opinionated and does not force you to use any particular architecture. You
can create your `StateHolder` anywhere you want. This also means you are responsible for scoping the
state, and on Android persisting the state across process death.

### Planned Features

Currently this library is in a very early stage. There are a few more features I wish to add such
as:

- [ ] Support for retaining state across process death on Android
- [x] Extensions for other platforms (iOS, JS, JVM, etc)
- [ ] Documentation

## Setup

You can add this library to your project using Gradle.

### Multiplatform

To add to a multiplatform project, add the dependency to the common source-set:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("dev.stateholder:core:1.0.1")

                // Optional Compose extensions
                implementation("dev.stateholder.extensions-compose:1.0.1")

                // Optional Voyager extensions
                implementation("dev.stateholder.extensions-voyager:1.0.1")

                // Optional Android-only extensions (not KMP friendly)
                implementation("dev.stateholder.extensions-android:1.0.1")
            }
        }
    }
}
```

### Platforms

| Artifact             | Android | Desktop | iOS | macOS | tv/watchOS | Browser | JS (Node) |
|----------------------|:-------:|:-------:|:---:|:-----:|:----------:|:-------:|:---------:|
| `core`               |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ✅     |
| `extensions-compose` |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ❌     |
| `extensions-voyager` |    ✅    |    ✅    |  ✅  |   ✅   |     ❌      |    ✅    |     ❌     |
| `extensions-android` |    ✅    |    ❌    |  ❌  |   ❌   |     ❌      |    ❌    |     ❌     |

### Android

For an Android only project, add the dependency to app level `build.gradle.kts`:

```kotlin
dependencies {
    // The core library
    implementation("dev.stateholder:core:1.0.1")

    // Optional Compose extensions
    implementation("dev.stateholder:extensions-compose:1.0.1")

    // Optional Voyager extensions
    implementation("dev.stateholder.extensions-voyager:1.0.1")

    // Optional Android extensions
    implementation("dev.stateholder:extensions-android:1.0.1")
}
```

### Version Catalog

```toml
[versions]
stateholder = "1.0.1"

[libraries]
stateholder-core = { module = "dev.stateholder:core", version.ref = "stateholder" }
stateholder-extensions-compose = { module = "dev.stateholder:extensions-compose", version.ref = "stateholder" }
stateholder-extensions-android = { module = "dev.stateholder:extensions-android", version.ref = "stateholder" }
stateholder-extensions-voyager = { module = "dev.stateholder:extensions-voyager", version.ref = "stateholder" }
```

## Usage

The `:core` artifact provides the following interfaces:

- `StateHolder`
    - Contains the state and provides methods for updating it.
- `StateOwner`
    - Exposes a `StateFlow<State>` to consumers.
- `StateProvider`
    - Wrapper around `Any` that provides state to a `StateHolder`.

### Creating a StateHolder

WIP

### Updating State

WIP

### Consuming State

WIP

### Android Extensions

WIP

## License

See [LICENSE](LICENSE) for more information.
