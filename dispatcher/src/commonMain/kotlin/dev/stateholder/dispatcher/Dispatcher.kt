package dev.stateholder.dispatcher

import androidx.compose.runtime.Immutable
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

/**
 * A functional interface for dispatching actions in a unidirectional data flow pattern.
 *
 * Dispatchers provide a clean way to send actions from the UI layer to your state management
 * logic. They can be passed to composables as stable references, avoiding unnecessary
 * recompositions.
 *
 * Example:
 *
 * ```
 * sealed interface CounterAction {
 *     data object Increment : CounterAction
 *     data object Decrement : CounterAction
 *     data class SetValue(val value: Int) : CounterAction
 * }
 *
 * class CounterViewModel : StateViewModel<Int>(0) {
 *     val dispatcher = Dispatcher<CounterAction> { action ->
 *         when (action) {
 *             CounterAction.Increment -> updateState { it + 1 }
 *             CounterAction.Decrement -> updateState { it - 1 }
 *             is CounterAction.SetValue -> updateState { action.value }
 *         }
 *     }
 * }
 *
 * @Composable
 * fun CounterScreen(dispatcher: Dispatcher<CounterAction>) {
 *     Button(onClick = dispatcher.relay(CounterAction.Increment)) {
 *         Text("+")
 *     }
 * }
 * ```
 *
 * @param Action The type of actions this dispatcher handles.
 */
@Immutable
public fun interface Dispatcher<Action> {
    /**
     * Dispatches an [action] to be processed.
     */
    public fun dispatch(action: Action)

    /**
     * Operator function allowing `dispatcher(action)` syntax as shorthand for [dispatch].
     */
    public operator fun invoke(action: Action) {
        dispatch(action)
    }

    /**
     * Creates a callback that dispatches the given [action] when invoked.
     *
     * Useful for passing to `onClick` and similar event handlers.
     *
     * @param action The action to dispatch when the returned callback is invoked.
     * @return A no-argument function that dispatches the action.
     */
    public fun relay(action: Action): () -> Unit = { dispatch(action) }

    /**
     * Creates a callback that constructs and dispatches an action from one parameter.
     *
     * @param action A function reference that creates an Action from one argument.
     * @return A function that takes one argument, constructs an action, and dispatches it.
     */
    public fun <T1> relayOf(action: KFunction1<T1, Action>): (T1) -> Unit =
        { t1 -> dispatch(action(t1)) }

    /**
     * Creates a callback that constructs and dispatches an action from two parameters.
     *
     * @param action A function reference that creates an Action from two arguments.
     * @return A function that takes two arguments, constructs an action, and dispatches it.
     */
    public fun <T1, T2> relayOf(action: KFunction2<T1, T2, Action>): (T1, T2) -> Unit =
        { t1, t2 -> dispatch(action(t1, t2)) }

    /**
     * Creates a callback that constructs and dispatches an action from three parameters.
     *
     * @param action A function that creates an Action from three arguments.
     * @return A function that takes three arguments, constructs an action, and dispatches it.
     */
    public fun <T1, T2, T3> relayOf(action: (T1, T2, T3) -> Action): (T1, T2, T3) -> Unit =
        { t1, t2, t3 -> dispatch(action(t1, t2, t3)) }

    /**
     * Creates a callback that constructs and dispatches an action from four parameters.
     *
     * @param action A function that creates an Action from four arguments.
     * @return A function that takes four arguments, constructs an action, and dispatches it.
     */
    public fun <T1, T2, T3, T4> relayOf(
        action: (T1, T2, T3, T4) -> Action,
    ): (T1, T2, T3, T4) -> Unit = { t1, t2, t3, t4 -> dispatch(action(t1, t2, t3, t4)) }

    /**
     * Creates a callback that constructs and dispatches an action from five parameters.
     *
     * @param action A function that creates an Action from five arguments.
     * @return A function that takes five arguments, constructs an action, and dispatches it.
     */
    public fun <T1, T2, T3, T4, T5> relayOf(
        action: (T1, T2, T3, T4, T5) -> Action,
    ): (T1, T2, T3, T4, T5) -> Unit = { t1, t2, t3, t4, t5 -> dispatch(action(t1, t2, t3, t4, t5)) }

    /**
     * Creates a callback that constructs and dispatches an action from six parameters.
     *
     * @param action A function that creates an Action from six arguments.
     * @return A function that takes six arguments, constructs an action, and dispatches it.
     */
    public fun <T1, T2, T3, T4, T5, T6> relayOf(
        action: (T1, T2, T3, T4, T5, T6) -> Action,
    ): (T1, T2, T3, T4, T5, T6) -> Unit = { t1, t2, t3, t4, t5, t6 ->
        dispatch(action(t1, t2, t3, t4, t5, t6))
    }

    public companion object
}
