package dev.stateholder.dispatcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

/**
 * Returns a remembered callback that dispatches the given [action].
 *
 * Use this to create stable `onClick` handlers that won't cause recomposition.
 *
 * Example:
 *
 * ```
 * Button(onClick = dispatcher.rememberRelay(CounterAction.Increment)) {
 *     Text("+")
 * }
 * ```
 *
 * @param action The action to dispatch when the callback is invoked.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action> Dispatcher<Action>.rememberRelay(action: Action): () -> Unit =
    remember(action) { relay(action) }

/**
 * Returns a remembered callback that constructs and dispatches an action from one parameter.
 *
 * @param action A function reference that creates an Action from one argument.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1> Dispatcher<Action>.rememberRelayOf(
    action: KFunction1<T1, Action>,
): (T1) -> Unit = remember(action) { { t1 -> dispatch(action(t1)) } }

/**
 * Returns a remembered callback that constructs and dispatches an action from two parameters.
 *
 * @param action A function reference that creates an Action from two arguments.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1, T2> Dispatcher<Action>.rememberRelayOf(
    action: KFunction2<T1, T2, Action>,
): (T1, T2) -> Unit = remember(action) { { t1, t2 -> dispatch(action(t1, t2)) } }

/**
 * Returns a remembered callback that constructs and dispatches an action from three parameters.
 *
 * @param action A function that creates an Action from three arguments.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1, T2, T3> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3) -> Action,
): (T1, T2, T3) -> Unit = remember(action) { { t1, t2, t3 -> dispatch(action(t1, t2, t3)) } }

/**
 * Returns a remembered callback that constructs and dispatches an action from four parameters.
 *
 * @param action A function that creates an Action from four arguments.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1, T2, T3, T4> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4) -> Action,
): (T1, T2, T3, T4) -> Unit =
    remember(action) { { t1, t2, t3, t4 -> dispatch(action(t1, t2, t3, t4)) } }

/**
 * Returns a remembered callback that constructs and dispatches an action from five parameters.
 *
 * @param action A function that creates an Action from five arguments.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1, T2, T3, T4, T5> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4, T5) -> Action,
): (T1, T2, T3, T4, T5) -> Unit =
    remember(action) { { t1, t2, t3, t4, t5 -> dispatch(action(t1, t2, t3, t4, t5)) } }

/**
 * Returns a remembered callback that constructs and dispatches an action from six parameters.
 *
 * @param action A function that creates an Action from six arguments.
 * @return A stable callback function.
 */
@Stable
@Composable
public fun <Action, T1, T2, T3, T4, T5, T6> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4, T5, T6) -> Action,
): (T1, T2, T3, T4, T5, T6) -> Unit =
    remember(action) { { t1, t2, t3, t4, t5, t6 -> dispatch(action(t1, t2, t3, t4, t5, t6)) } }

/**
 * Creates and remembers a [Dispatcher] across recompositions.
 *
 * Example:
 *
 * ```
 * @Composable
 * fun CounterScreen() {
 *     var count by remember { mutableIntStateOf(0) }
 *
 *     val dispatcher = rememberDispatcher<CounterAction> { action ->
 *         when (action) {
 *             CounterAction.Increment -> count++
 *             CounterAction.Decrement -> count--
 *         }
 *     }
 *
 *     CounterContent(count, dispatcher)
 * }
 * ```
 *
 * @param block The handler invoked when an action is dispatched.
 * @return A remembered [Dispatcher] instance.
 */
@Stable
@Composable
public fun <Action> rememberDispatcher(block: (Action) -> Unit): Dispatcher<Action> = remember {
    Dispatcher(block)
}

/**
 * Creates and remembers a debouncing [Dispatcher] across recompositions.
 *
 * @param debounce The time window in milliseconds during which duplicate actions are ignored.
 * @param exclude A predicate to bypass debouncing for certain actions.
 * @param block The handler invoked when an action is dispatched.
 * @return A remembered [Dispatcher] instance with debouncing.
 * @see DebounceDispatcher
 */
@Stable
@Composable
public fun <Action> rememberDispatcher(
    debounce: Long,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = remember(debounce, exclude, block) { Dispatcher(debounce, exclude, block) }

/**
 * Creates and remembers a [DebounceDispatcher] across recompositions.
 *
 * This is an alias for [rememberDispatcher] with debouncing that provides a more
 * explicit name when debouncing is the primary concern.
 *
 * @param debounce The time window in milliseconds during which duplicate actions are ignored.
 *   Defaults to 100ms.
 * @param exclude A predicate to bypass debouncing for certain actions.
 * @param block The handler invoked when an action is dispatched.
 * @return A remembered [Dispatcher] instance with debouncing.
 * @see DebounceDispatcher
 */
@Stable
@Composable
public fun <Action> rememberDebounceDispatcher(
    debounce: Long = 100,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = rememberDispatcher(debounce, exclude, block)