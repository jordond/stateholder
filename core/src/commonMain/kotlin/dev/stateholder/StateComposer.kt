package dev.stateholder

import dev.stateholder.provider.FlowStateProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * A DSL for composing state from multiple reactive sources.
 *
 * Use this to declaratively wire up flows that should update portions of your state.
 * Each [slice] or [into] call merges emissions from a source into the container's state.
 *
 * Example:
 *
 * ```
 * val container = stateContainer(scope, MyState()) {
 *     userRepository.currentUser into { copy(user = it) }
 *     cartRepository.cart into { copy(cart = it) }
 * }
 * ```
 *
 * @param State The type of state being composed.
 */
public interface StateComposer<State> {

    /**
     * Merges emissions from a [Flow] into the state.
     *
     * @param T The type of values emitted by the flow.
     * @param flow The flow to collect from.
     * @param merge A function that combines the current state with the emitted value.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public fun <T> slice(
        flow: Flow<T>,
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Merges emissions from a [dev.stateholder.provider.FlowStateProvider] into the state.
     *
     * @param T The type of values emitted by the provider.
     * @param provider The provider to collect from.
     * @param merge A function that combines the current state with the emitted value.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public fun <T> slice(
        provider: FlowStateProvider<T>,
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Merges state from another [StateContainer] into this state.
     *
     * @param T The type of state in the other container.
     * @param container The container to observe.
     * @param merge A function that combines the current state with the other container's state.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public fun <T> slice(
        container: StateContainer<T>,
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Merges state from a [StateHolder] into this state.
     *
     * @param T The type of state in the holder.
     * @param holder The holder to observe.
     * @param merge A function that combines the current state with the holder's state.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public fun <T> slice(
        holder: StateHolder<T>,
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Infix operator for merging a [Flow] into the state.
     *
     * Example:
     * ```
     * userRepository.currentUser into { copy(user = it) }
     * ```
     *
     * @param T The type of values emitted by the flow.
     * @param merge A function that combines the current state with the emitted value.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public infix fun <T> Flow<T>.into(
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Infix operator for merging a [FlowStateProvider] into the state.
     *
     * Example:
     * ```
     * userStateProvider into { copy(user = it) }
     * ```
     *
     * @param T The type of values emitted by the provider.
     * @param merge A function that combines the current state with the emitted value.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public infix fun <T> FlowStateProvider<T>.into(
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Infix operator for merging another [StateContainer] into the state.
     *
     * @param T The type of state in the other container.
     * @param merge A function that combines the current state with the other container's state.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public infix fun <T> StateContainer<T>.into(
        merge: suspend State.(T) -> State,
    ): Job

    /**
     * Infix operator for merging a [StateHolder] into the state.
     *
     * @param T The type of state in the holder.
     * @param merge A function that combines the current state with the holder's state.
     * @return A [Job] that can be cancelled to stop the merge.
     */
    public infix fun <T> StateHolder<T>.into(
        merge: suspend State.(T) -> State,
    ): Job
}
