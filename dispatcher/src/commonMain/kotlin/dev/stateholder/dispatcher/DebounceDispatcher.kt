package dev.stateholder.dispatcher

import androidx.compose.runtime.Stable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Creates a [Dispatcher] that debounces duplicate actions within a time window.
 *
 * When the same action is dispatched multiple times within the [debounce] window,
 * only the first dispatch is processed. This is useful for preventing rapid repeated
 * actions like button double-clicks.
 *
 * Example:
 *
 * ```
 * sealed interface FormAction {
 *     data object Submit : FormAction
 *     data class UpdateField(val value: String) : FormAction
 * }
 *
 * // Debounce submit to prevent double-submission, but allow field updates through
 * val dispatcher = DebounceDispatcher<FormAction>(
 *     debounce = 500,
 *     exclude = { it is FormAction.UpdateField },
 * ) { action ->
 *     when (action) {
 *         FormAction.Submit -> submitForm()
 *         is FormAction.UpdateField -> updateField(action.value)
 *     }
 * }
 * ```
 *
 * @param debounce The time window in milliseconds during which duplicate actions are ignored.
 *   Defaults to 100ms.
 * @param exclude A predicate to bypass debouncing for certain actions. Actions matching this
 *   predicate are always dispatched immediately. Defaults to excluding nothing.
 * @param block The handler invoked when an action is dispatched (and not debounced).
 * @return A [Dispatcher] that debounces duplicate actions.
 */
@OptIn(ExperimentalTime::class)
@Suppress("FunctionName")
@Stable
public fun <Action> DebounceDispatcher(
    debounce: Long = 100,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = object : Dispatcher<Action> {
    private val lookup: HashMap<Action, Long> = hashMapOf()

    override fun dispatch(action: Action) {
        if (exclude(action)) {
            return block(action)
        }

        val currentTime = Clock.System.now().toEpochMilliseconds()

        lookup.entries.removeAll { (_, timestamp) ->
            currentTime - timestamp > debounce
        }

        val lastTime = lookup[action]
        if (lastTime == null || currentTime - lastTime > debounce) {
            lookup[action] = currentTime
            block(action)
        }
    }
}

/**
 * Creates a [Dispatcher] with debouncing enabled.
 *
 * This is an alias for [DebounceDispatcher] that provides a more discoverable API when
 * you want to create a dispatcher with debouncing from the start.
 *
 * @param debounce The time window in milliseconds during which duplicate actions are ignored.
 * @param exclude A predicate to bypass debouncing for certain actions.
 * @param block The handler invoked when an action is dispatched.
 * @return A [Dispatcher] that debounces duplicate actions.
 * @see DebounceDispatcher
 */
@Stable
public fun <Action> Dispatcher(
    debounce: Long,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = DebounceDispatcher(debounce, exclude, block)