package dev.stateholder

import dev.stateholder.internal.DefaultStateContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

/**
 * A container for managing state.
 *
 * The [state] property is the only property that should be exposed to the UI level. All updating
 * should be handled separately.
 *
 * @param[State] The type of the state.
 */
public interface StateContainer<State> {
    /**
     * The current state of type [State] exposed as a [StateFlow].
     */
    public val state: StateFlow<State>

    /**
     * One way subscription to update the state with another [flow].
     *
     * This is useful when you need to update the [state] based off of another [Flow]. The [flow]
     * will be collected and [block] will be invoked to map the [T] value from [flow] to
     * the [State] value.
     *
     * The collecting can be stopped by cancelling the returned [Job].
     *
     * @param[T] The type of the value from flow.
     * @param[flow] The flow to collect from and update state with.
     * @param[scope] The scope to use for collecting the flow.
     * @param[block] The function to map the [T] value from [flow] to the [State] value.
     * @return The [Job] of the collection.
     */
    public fun <T> merge(
        flow: Flow<T>,
        scope: CoroutineScope,
        block: suspend (State, T) -> State,
    ): Job

    /**
     * One way subscription to update the state with another [container].
     *
     * This is useful when you need to update the [state] based off of another [StateContainer]. The
     * [container] will be observed and [block] will be invoked to map the [T] value from
     * [container] to the [State] value.
     *
     * The observing can be stopped by cancelling the returned [Job].
     *
     * @param[T] The type of the value from the [container].
     * @param[container] The [StateContainer] to observe and update state with.
     * @param[scope] The scope to use for observing the [container].
     * @return The [Job] of the observation.
     */
    public fun <T> merge(
        container: StateContainer<T>,
        scope: CoroutineScope,
        block: suspend (State, T) -> State,
    ): Job

    /**
     * One way subscription to update the state with another [holder].
     *
     * This is useful when you need to update the [state] based off of another [StateHolder]. The
     * [holder] will be observed and [block] will be invoked to map the [T] value from
     * [holder] to the [State] value.
     *
     * The observing can be stopped by cancelling the returned [Job].
     *
     * @param[T] The type of the value from the [holder].
     * @param[holder] The [StateHolder] to observe and update state with.
     * @param[scope] The scope to use for observing the [holder].
     * @return The [Job] of the observation.
     */
    public fun <T> merge(
        holder: StateHolder<T>,
        scope: CoroutineScope,
        block: suspend (State, T) -> State,
    ): Job

    /**
     * Updates the [MutableStateFlow.value] atomically using the specified function of its value.
     * Function may be evaluated multiple times if the value is being concurrently updated.
     */
    public fun update(block: (State) -> State)

    /**
     * Allows using delegation to access the [StateContainer.state] property.
     *
     * Example:
     *
     * ```
     * val state: StateFlow<State> by stateContainer
     * ```
     */
    public operator fun getValue(
        stateHolder: StateHolder<State>,
        property: KProperty<*>,
    ): StateFlow<State> = state

    public companion object {
        /**
         * Create a [StateContainer] with the given [initialStateProvider].
         */
        internal fun <State> create(
            initialStateProvider: StateProvider<State>,
        ): StateContainer<State> = DefaultStateContainer(initialStateProvider)
    }
}

/**
 * Create a [StateContainer] with the given [initialStateProvider].
 *
 * @see [StateContainer]
 */
public fun <State> stateContainer(
    initialStateProvider: StateProvider<State>,
): StateContainer<State> = StateContainer.create(initialStateProvider)

/**
 * Create a [StateContainer] with the given [initialState].
 *
 * @see [StateContainer]
 */
public fun <State> stateContainer(
    initialState: State,
): StateContainer<State> = StateContainer.create(provideState(initialState))

/**
 * Merge this flow with the state container, updating the state based on the provided block.
 *
 * @param container The state container to merge with.
 * @param scope The coroutine scope for the merging operation.
 * @param block The block to update the state with the incoming value.
 * @return A job representing the merging operation.
 */
public fun <T, State> Flow<T>.mergeWithState(
    container: StateContainer<State>,
    scope: CoroutineScope,
    block: suspend (state: State, value: T) -> State,
): Job = container.merge(this, scope, block)
