# State Holder

![Maven Central](https://img.shields.io/maven-central/v/dev.stateholder/core)
[![Kotlin](https://img.shields.io/badge/kotlin-v2.3.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Build](https://github.com/jordond/state-holder/actions/workflows/ci.yml/badge.svg)](https://github.com/jordond/state-holder/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/jordond/state-holder)](http://www.apache.org/licenses/LICENSE-2.0)

[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-v1.10.0-blue)](https://github.com/JetBrains/compose-multiplatform)
![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/platform-jvm-6EDB8D.svg?style=flat)
![badge-apple](http://img.shields.io/badge/platform-ios%2Fmacos%2Fwatchos%2Ftvos-CDCDCD.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js-F7DF1E.svg?style=flat)
![badge-wasm](http://img.shields.io/badge/platform-wasm-624DE5.svg?style=flat)

A simple library for managing state in Kotlin Multiplatform projects using Kotlin Coroutines
and `StateFlow`.

You can view the KDocs at [docs.stateholder.dev](https://docs.stateholder.dev).

## Terminology

| Interface | Purpose |
|-----------|---------|
| `StateHolder` | Read-only state exposure via `StateFlow`. Pass this to UI layers. |
| `StateContainer` | Read/write state management. Use internally to `update` state. |
| `StateProvider` | Factory for initial state. Enables lazy init and DI. |
| `FlowStateProvider` | Provides initial state + a `Flow` of updates. For reactive data sources. |
| `ComposedStateProvider` | Combines initial state + composition logic. Wire multiple flows into state. |
| `StateComposer` | DSL for merging flows into state using `into` syntax. |
| `EventHolder` | One-time event queue for UI side effects (toasts, navigation). |
| `Dispatcher` | Single callback for all UI actions. Replaces multiple lambdas. |

## Table of Contents

- [Terminology](#terminology)
- [Motivation](#motivation)
- [Modules](#modules)
- [Setup](#setup)
    - [Multiplatform](#multiplatform)
    - [Android](#android)
    - [Version Catalog](#version-catalog)
  - [Platform Support](#platform-support)
- [Usage](#usage)
    - [Core Concepts](#core-concepts)
    - [Creating a StateContainer](#creating-a-statecontainer)
    - [Updating State](#updating-state)
    - [Consuming State](#consuming-state)
    - [Merging Flows](#merging-flows)
- [Dispatcher](#dispatcher)
    - [Basic Dispatcher](#basic-dispatcher)
    - [Debounce Dispatcher](#debounce-dispatcher)
    - [Relay Functions](#relay-functions)
- [Extensions](#extensions)
    - [ViewModel Extensions](#viewmodel-extensions)
    - [Voyager Extensions](#voyager-extensions)
    - [Compose Extensions](#compose-extensions)
- [Events](#events)
- [License](#license)

## Motivation

State Holder aims to make state management in Kotlin Multiplatform projects simpler. It provides
a straightforward API for creating and managing state with minimal boilerplate.

The library is un-opinionated and doesn't force any particular architecture. You can create
your `StateContainer` anywhere and use it however you want. This also means you're responsible for
scoping the state appropriately. On Android, you may want to pair this with a ViewModel for
lifecycle management and process death survival.

The `Dispatcher` module solves a common problem with Compose: instead of passing many callback
lambdas to your composables, you pass a single `Dispatcher<Action>` and define your actions as a
sealed interface. This keeps your composable signatures clean and makes it easy to add debouncing.

## Modules

| Module                 | Description                                                   |
|------------------------|---------------------------------------------------------------|
| `core`                 | Core state management with `StateContainer` and `StateHolder` |
| `dispatcher`           | Action dispatching with optional debouncing                   |
| `dispatcher-compose`   | Compose integration for dispatchers with `remember` helpers   |
| `extensions-compose`   | Compose extensions for collecting state and handling events   |
| `extensions-viewmodel` | AndroidX ViewModel base classes                               |
| `extensions-voyager`   | Voyager ScreenModel base classes                              |

## Setup

You can add this library to your project using Gradle.

### Multiplatform

Add the dependencies to the common source-set:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // Core state management
                implementation("dev.stateholder:core:2.0.0")

                // Dispatcher for action handling
                implementation("dev.stateholder:dispatcher:2.0.0")

                // Compose extensions (state collection, event handling)
                implementation("dev.stateholder:extensions-compose:2.0.0")

                // Compose dispatcher helpers
                implementation("dev.stateholder:dispatcher-compose:2.0.0")

                // ViewModel base classes (AndroidX ViewModel)
                implementation("dev.stateholder:extensions-viewmodel:2.0.0")

                // Voyager base classes
                implementation("dev.stateholder:extensions-voyager:2.0.0")
            }
        }
    }
}
```

### Version Catalog

```toml
[versions]
stateholder = "2.0.0"

[libraries]
stateholder-core = { module = "dev.stateholder:core", version.ref = "stateholder" }
stateholder-dispatcher = { module = "dev.stateholder:dispatcher", version.ref = "stateholder" }
stateholder-dispatcher-compose = { module = "dev.stateholder:dispatcher-compose", version.ref = "stateholder" }
stateholder-extensions-compose = { module = "dev.stateholder:extensions-compose", version.ref = "stateholder" }
stateholder-extensions-viewmodel = { module = "dev.stateholder:extensions-viewmodel", version.ref = "stateholder" }
stateholder-extensions-voyager = { module = "dev.stateholder:extensions-voyager", version.ref = "stateholder" }
```

### Platform Support

| Artifact               | Android | Desktop | iOS | macOS | tv/watchOS | Browser | JS (Node) |
|------------------------|:-------:|:-------:|:---:|:-----:|:----------:|:-------:|:---------:|
| `core`                 |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ✅     |
| `dispatcher`           |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ✅     |
| `dispatcher-compose`   |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ❌     |
| `extensions-compose`   |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ❌     |
| `extensions-viewmodel` |    ✅    |    ✅    |  ✅  |   ✅   |     ✅      |    ✅    |     ✅     |
| `extensions-voyager`   |    ✅    |    ✅    |  ✅  |   ✅   |     ❌      |    ✅    |     ❌     |

## Usage

### Core Concepts

The core module provides three main interfaces:

- **`StateHolder<State>`**: A read-only interface that exposes a `StateFlow<State>`. Use this for
  consumers that only need to observe state.
- **`StateContainer<State>`**: Extends `StateHolder` with methods to update state. This is what you
  use internally to manage state.
- **`StateProvider<State>`**: A factory interface for creating initial state values. Useful for
  lazy initialization or dependency injection.

### Creating a StateContainer

Create a state container with an initial state:

```kotlin
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
)

// Direct initialization
val container = stateContainer(CounterState())

// Using a provider (for lazy initialization)
val container = stateContainer(provideState { CounterState() })
```

### Updating State

Update state using the `update` function, which takes a lambda that receives the current state and
returns the new state. Updates are thread-safe.

```kotlin
val container = stateContainer(CounterState())

// Increment the count
container.update { state ->
    state.copy(count = state.count + 1)
}

// Set loading state
container.update { it.copy(isLoading = true) }
```

### Consuming State

The `StateContainer` exposes state through a `StateFlow<State>`:

```kotlin
val container = stateContainer(CounterState())

// Collect in a coroutine
scope.launch {
    container.state.collect { state ->
        println("Count: ${state.count}")
    }
}

// Access current value directly
val currentCount = container.state.value.count
```

When you only want to expose read-only access, convert to a `StateHolder`:

```kotlin
class CounterRepository {
    private val container = stateContainer(CounterState())

    // Expose read-only access
    val stateHolder: StateHolder<CounterState> = container.asStateHolder()

    fun increment() {
        container.update { it.copy(count = it.count + 1) }
    }
}
```

### Merging Flows

You can merge external flows into your state container. This is useful for combining data from
repositories or other sources:

```kotlin
val container = stateContainer(UserProfileState())

// Merge a flow into state
userRepository.userFlow.mergeWithState(container, scope) { state, user ->
    state.copy(user = user)
}

// Merge another StateHolder into state
settingsHolder.state.mergeWithState(container, scope) { state, settings ->
    state.copy(theme = settings.theme)
}
```

The `StateContainer` also has `merge` methods for merging flows, containers, or holders directly:

```kotlin
container.merge(userRepository.userFlow, scope) { state, user ->
    state.copy(user = user)
}
```

## Dispatcher

The dispatcher module provides a way to handle actions with optional debouncing. Instead of passing
multiple callback lambdas to your composables, you pass a single `Dispatcher<Action>`.

### Basic Dispatcher

```kotlin
// Define your actions
sealed interface CounterAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data class SetCount(val count: Int) : CounterAction
}

// Create a dispatcher
val dispatcher = Dispatcher<CounterAction> { action ->
    when (action) {
        is CounterAction.Increment -> container.update { it.copy(count = it.count + 1) }
        is CounterAction.Decrement -> container.update { it.copy(count = it.count - 1) }
        is CounterAction.SetCount -> container.update { it.copy(count = action.count) }
    }
}

// Dispatch actions
dispatcher.dispatch(CounterAction.Increment)
dispatcher(CounterAction.Decrement) // operator invoke
```

### Debounce Dispatcher

Prevent rapid repeated actions with debouncing:

```kotlin
val dispatcher = DebounceDispatcher<CounterAction>(
    debounce = 100, // milliseconds
    exclude = { action ->
        // Don't debounce these actions
        action is CounterAction.SetCount
    },
) { action ->
    when (action) {
        is CounterAction.Increment -> viewModel.increment()
        is CounterAction.Decrement -> viewModel.decrement()
        is CounterAction.SetCount -> viewModel.setCount(action.count)
    }
}
```

The debounce behavior:

- First action dispatches immediately
- Repeated identical actions within the debounce window are ignored
- Different actions dispatch immediately regardless of debounce timing

### Relay Functions

Relay functions create callbacks that dispatch specific actions. This is useful for button
click handlers:

```kotlin
Button(onClick = dispatcher.relay(CounterAction.Increment)) { Text("+") }
TextField(onValueChange = dispatcher.relayOf(CounterAction::SetCount))
```

### Compose Integration

Use `rememberDispatcher` and `rememberRelay` for stable references in Compose:

```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    val dispatcher = rememberDebounceDispatcher<CounterAction>(
        debounce = 100,
        exclude = { it is CounterAction.SetCount },
    ) { action ->
        when (action) {
            is CounterAction.Increment -> viewModel.increment()
            is CounterAction.Decrement -> viewModel.decrement()
            is CounterAction.SetCount -> viewModel.setCount(action.count)
        }
    }

    val state by viewModel.collectAsState()
    CounterContent(
        count = state.count,
        dispatcher = dispatcher,
    )
}

@Composable
fun CounterContent(count: Int, dispatcher: Dispatcher<CounterAction>) {
    Button(onClick = dispatcher.rememberRelay(CounterAction.Increment)) {
        Text("+")
    }

    TextField(
        value = count.toString(),
        onValueChange = dispatcher.rememberRelayOf(CounterAction::SetCount),
    )
}
```

## Extensions

### ViewModel Extensions

The `extensions-viewmodel` module provides base classes for AndroidX ViewModel:

```kotlin
class CounterViewModel(
    userRepository: UserRepository,
) : StateViewModel<CounterState>(CounterState()) {
    init {
        // Merge a flow into state using viewModelScope
        userRepository.userFlow.mergeState { state, user ->
            state.copy(userName = user.name)
        }
    }

    fun increment() {
        updateState { it.copy(count = it.count + 1) }
    }

    fun decrement() {
        updateState { it.copy(count = it.count - 1) }
    }
}
```

For ViewModels that need to emit one-time events (like showing a toast or navigating), use
`UiStateViewModel`:

```kotlin
class CounterViewModel : UiStateViewModel<CounterState, CounterEvent>(CounterState()) {

    sealed interface CounterEvent {
        data object ShowMaxReachedToast : CounterEvent
        data class Navigate(val route: String) : CounterEvent
    }

    fun increment() {
        updateState { it.copy(count = it.count + 1) }
        if (state.value.count >= 100) {
            emit(CounterEvent.ShowMaxReachedToast)
        }
    }
}
```

### Voyager Extensions

For Compose Multiplatform projects using Voyager, use the `extensions-voyager` module:

```kotlin
class CounterScreenModel : StateScreenModel<CounterState>(CounterState()) {
    fun increment() {
        updateState { it.copy(count = it.count + 1) }
    }
}

// With events
class CounterScreenModel : UiStateScreenModel<CounterState, CounterEvent>(CounterState()) {
    sealed interface CounterEvent {
        data object ShowToast : CounterEvent
    }

    fun triggerEvent() {
        emit(CounterEvent.ShowToast)
    }
}
```

Usage in a Voyager screen:

```kotlin
class CounterScreen : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { CounterScreenModel() }
        val state by model.collectAsState()

        CounterContent(
            count = state.count,
            onIncrement = model::increment,
        )
    }
}
```

### Compose Extensions

The `extensions-compose` module provides utilities for Compose:

#### Collecting State

```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    // Lifecycle-aware state collection
    val state by viewModel.collectAsState()

    Text("Count: ${state.count}")
}
```

#### Handling Events

For one-time events like showing toasts or navigating:

```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    HandleEvents(viewModel) { event ->
        when (event) {
            is CounterEvent.ShowMaxReachedToast -> {
                snackbarHostState.showSnackbar("Maximum reached!")
            }
            is CounterEvent.Navigate -> {
                // Handle navigation
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        // Content
    }
}
```

## Events

The `EventHolder` interface provides one-way event handling for UI side effects. Events are stored
in a `PersistentList` and removed after being handled.

This pattern is useful for:

- Showing snackbars or toasts
- Navigation
- One-time UI effects that shouldn't survive configuration changes

Both `UiStateViewModel` and `UiStateScreenModel` implement `EventHolder`:

```kotlin
class MyViewModel : UiStateViewModel<State, Event>(State()) {

    sealed interface Event {
        data object ShowSuccess : Event
        data class ShowError(val message: String) : Event
    }

    fun doSomething() {
        try {
            // Do work
            emit(Event.ShowSuccess)
        } catch (e: Exception) {
            emit(Event.ShowError(e.message ?: "Unknown error"))
        }
    }
}

// In Compose
@Composable
fun MyScreen(viewModel: MyViewModel) {
    HandleEvents(viewModel) { event ->
        when (event) {
            is Event.ShowSuccess -> showToast("Success!")
            is Event.ShowError -> showToast(event.message)
        }
    }
}
```

## License

See [LICENSE](LICENSE) for more information.
