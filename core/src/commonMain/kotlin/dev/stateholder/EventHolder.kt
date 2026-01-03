package dev.stateholder

import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.StateFlow

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
 * class MyViewModel(
 *     private val container: StateContainer<MyState>,
 * ) : EventHolder<MyEvent> by container.asEventHolder() {
 *
 *     fun doSomething() {
 *         container.emit(MyEvent.ShowToast("Hello!"))
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
public interface EventHolder<Event> {
    /**
     * A [StateFlow] containing the current list of unhandled events.
     */
    public val events: StateFlow<PersistentList<Event>>

    /**
     * Marks the given [event] as handled, removing it from the [events] list.
     *
     * Call this after processing an event to prevent it from being processed again.
     */
    public fun handle(event: Event)
}