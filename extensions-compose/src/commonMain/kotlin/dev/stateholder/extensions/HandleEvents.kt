package dev.stateholder.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dev.stateholder.EventHolder
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Collects and processes events from an [EventHolder], automatically marking them as handled.
 *
 * This composable observes the [EventHolder.events] flow and invokes [onEvent] for each event.
 * After processing, events are automatically marked as handled via [EventHolder.handle].
 *
 * Example:
 *
 * ```
 * @Composable
 * fun MyScreen(viewModel: MyViewModel) {
 *     val snackbarHostState = remember { SnackbarHostState() }
 *
 *     HandleEvents(viewModel) { event ->
 *         when (event) {
 *             is MyEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
 *             is MyEvent.NavigateBack -> navigator.pop()
 *         }
 *     }
 *
 *     // ... rest of UI
 * }
 * ```
 *
 * @param holder The [EventHolder] to collect events from.
 * @param shouldHandle Optional predicate to filter which events should be handled.
 *   Events that don't pass this filter remain in the queue. Defaults to handling all events.
 * @param onEvent Suspend function invoked for each event. Called before the event is marked as handled.
 */
@Composable
public fun <Event> HandleEvents(
    holder: EventHolder<Event>,
    shouldHandle: (Event) -> Boolean = { true },
    onEvent: suspend (Event) -> Unit,
) {
    val events by holder.events.collectAsState()
    val handler =
        remember {
            { event: Event ->
                if (shouldHandle(event)) {
                    holder.handle(event)
                }
            }
        }

    HandleEvents(events, handler, onEvent)
}

/**
 * Processes a list of events, optionally marking them as handled.
 *
 * This overload accepts a standard [List] and delegates to the [PersistentList] variant.
 * Use this when you have a list of events from a source other than [EventHolder].
 *
 * @param events The list of events to process.
 * @param handle Optional callback invoked after [onEvent] to mark the event as handled.
 * @param onEvent Suspend function invoked for each event.
 */
@Composable
public fun <Event> HandleEvents(
    events: List<Event>,
    handle: ((Event) -> Unit)? = null,
    onEvent: suspend (Event) -> Unit,
) {
    HandleEvents(events.toPersistentList(), handle, onEvent)
}

/**
 * Processes a [PersistentList] of events within a [LaunchedEffect].
 *
 * This is the core implementation that iterates through each event, invokes [onEvent],
 * and optionally calls [handle] to mark the event as processed.
 *
 * Events are processed sequentially and tracked by referential identity to ensure
 * each event is only processed once, even when the list updates rapidly.
 *
 * @param events The persistent list of events to process.
 * @param handle Optional callback invoked after [onEvent] to mark the event as handled.
 * @param onEvent Suspend function invoked for each event.
 */
@Composable
public fun <Event> HandleEvents(
    events: PersistentList<Event>,
    handle: ((Event) -> Unit)? = null,
    onEvent: suspend (Event) -> Unit,
) {
    val seenEvents = remember { mutableSetOf<Any?>() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentHandle by rememberUpdatedState(handle)

    LaunchedEffect(events) {
        events.forEach { event ->
            if (!seenEvents.add(event)) return@forEach
            currentOnEvent(event)
            currentHandle?.invoke(event)
        }
        seenEvents.retainAll(events.toSet())
    }
}
