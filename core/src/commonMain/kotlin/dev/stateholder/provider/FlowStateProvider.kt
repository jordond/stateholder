package dev.stateholder.provider

import dev.stateholder.StateContainer
import dev.stateholder.StateProvider
import kotlinx.coroutines.flow.Flow

/**
 * A reactive provider of state for a [StateContainer].
 *
 * Unlike [StateProvider] which provides a single state value, this interface
 * provides both an initial state via [StateProvider.provide] and a [Flow] of state updates.
 *
 * ```
 * class MyFlowStateProvider @Inject constructor(repo: MyRepo) : FlowStateProvider<Int> {
 *     override fun states() = repo.observeData()
 * }
 *
 * class MyModel @Inject constructor(stateProvider: MyFlowStateProvider) : ViewModel() {
 *     private val container = stateContainer(stateProvider)
 * }
 * ```
 */
public interface FlowStateProvider<State> : StateProvider<State> {
    /**
     * Returns a [Flow] that emits state updates.
     */
    public fun states(): Flow<State>
}

/**
 * Creates a [FlowStateProvider] for use with delegation.
 *
 * This is useful for creating injectable [FlowStateProvider] implementations with minimal boilerplate.
 *
 * Example:
 *
 * ```
 * class UserStateProvider @Inject constructor(
 *     userRepository: UserRepository,
 * ) : FlowStateProvider<User?> by flowStateProvider(
 *     initialState = null,
 *     flow = userRepository.currentUser,
 * )
 * ```
 *
 * @param State The type of state.
 * @param initialState The initial state value returned by [FlowStateProvider.provide].
 * @param flow The flow of state updates returned by [FlowStateProvider.states].
 * @return A [FlowStateProvider] that can be used with delegation.
 */
public fun <State> flowStateProvider(
    initialState: State,
    flow: Flow<State>,
): FlowStateProvider<State> = object : FlowStateProvider<State> {
    override fun provide(): State = initialState
    override fun states(): Flow<State> = flow
}

/**
 * Creates a [FlowStateProvider] for use with delegation.
 *
 * This is useful for creating injectable [FlowStateProvider] implementations with minimal boilerplate.
 *
 * Example:
 *
 * ```
 * class UserStateProvider @Inject constructor(
 *     userRepository: UserRepository,
 * ) : FlowStateProvider<User?> by flowStateProvider(
 *     initialState = stateProvider { null },
 *     flow = userRepository.currentUser,
 * )
 * ```
 *
 * @param State The type of state.
 * @param provider A [StateProvider] that supplies the initial state.
 * @param flow The flow of state updates returned by [FlowStateProvider.states].
 * @return A [FlowStateProvider] that can be used with delegation.
 */
public fun <State> flowStateProvider(
    provider: StateProvider<State>,
    flow: Flow<State>,
): FlowStateProvider<State> = object : FlowStateProvider<State> {
    override fun provide(): State = provider.provide()
    override fun states(): Flow<State> = flow
}