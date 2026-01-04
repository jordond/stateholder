package dev.stateholder

import dev.stateholder.internal.DefaultEventHolder
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for emitting one-time events to be handled by the UI layer.
 *
 * @param Event The type of events that can be emitted.
 */
public interface EventEmitter<Event> {
    /**
     * Emits a one-time [event] to be handled by the UI layer.
     *
     * The event is added to the queue and remains until [EventHolder.handle] is called.
     *
     * @param event The event to emit.
     */
    public fun emit(event: Event)

    /**
     * Emits multiple events at once.
     *
     * @param events The events to emit.
     */
    public fun emit(vararg events: Event)

    public fun emit(events: Iterable<Event>)
}

/**
 * Exposes an [events] flow and a [handle] function for managing one-time events.
 *
 * Events are stored in a [PersistentList] and emitted through a [StateFlow], allowing
 * consumers to observe and process events as they occur. Once an event has been
 * processed, call [handle] to remove it from the list.
 *
 * Example:
 *
 * ```
 * class MyViewModel : ViewModel(), EventHolder<MyEvent> by eventHolder() {
 *
 *     fun doSomething() {
 *         emit(MyEvent.ShowToast("Hello!"))
 *     }
 * }
 *
 * // In your UI layer using the HandleEvents composable:
 * HandleEvents(viewModel) { event ->
 *     when (event) {
 *         is MyEvent.ShowToast -> showToast(event.message)
 *     }
 * }
 *
 * // Or manually with LaunchedEffect:
 * val events by viewModel.events.collectAsState()
 * LaunchedEffect(events) {
 *     events.forEach { event ->
 *         when (event) {
 *             is MyEvent.ShowToast -> showToast(event.message)
 *         }
 *         viewModel.handle(event)
 *     }
 * }
 * ```
 */
public interface EventHolder<Event> : EventEmitter<Event> {
    /**
     * A [StateFlow] containing the current list of unhandled events.
     */
    public val events: StateFlow<PersistentList<Event>>

    /**
     * Marks the given [event] as handled, removing it from the [events] list.
     *
     * Call this after processing an event to prevent it from being processed again.
     *
     * Note: This uses reference equality first (`===`), then falls back to value equality (`==`).
     * This ensures correct behavior when handling duplicate events.
     */
    public fun handle(event: Event)

    /**
     * Marks all events as handled, clearing the [events] list.
     */
    public fun handleAll()
}

/**
 * Creates an [EventHolder] for use with delegation.
 *
 * This helper provides a complete implementation of [EventHolder] that can be
 * delegated to in your ViewModel or ScreenModel.
 *
 * Example:
 *
 * ```
 * class MyViewModel : ViewModel(), EventHolder<MyEvent> by eventHolder() {
 *
 *     fun doSomething() {
 *         emit(MyEvent.ShowToast("Hello!"))
 *     }
 * }
 * ```
 *
 * @param Event The type of events.
 * @return An [EventHolder] implementation that can be used with delegation.
 */
public fun <Event> eventHolder(): EventHolder<Event> = DefaultEventHolder()
