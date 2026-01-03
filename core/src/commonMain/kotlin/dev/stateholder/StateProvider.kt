package dev.stateholder

import kotlinx.coroutines.flow.Flow

/**
 * A provider of a state for a [StateContainer].
 *
 * Sometimes we can have a simple state object that does not rely on outside sources. In that case
 * you can just use [provideState], like so:
 *
 * ```
 * val stateContainer = StateContainer.create(provideState(42))
 * ```
 *
 * However sometimes we need to use a more complex state object that relies on outside sources. For
 * that we might need to inject in some dependencies and create the state object.
 *
 * ```
 * class MyRepo {
 *   fun getSomeData(): Int = 42
 * }
 *
 * class MyStateProvider @Inject constructor(repo: MyRepo): StateProvider<Int> {
 *     override fun provide() = repo.getSomeData()
 * }
 *
 * class MyModel @Inject constructor(stateProvider: MyStateProvider) : ViewModel() {
 *     private val container = stateContainer(stateProvider)
 * }
 * ```
 *
 * This provides a testable way to provide state for a [StateContainer].
 */
public fun interface StateProvider<State> {
    public fun provide(): State
}

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
 * Convenience function to create a simple [StateProvider] with a value.
 *
 * @param[State] The type of the state.
 * @param[state] The state to provide.
 * @return A [StateProvider] that provides the [state].
 */
public fun <State> provideState(state: State): StateProvider<State> = StateProvider { state }

/**
 * Convenience function to create a lazily evaluated [StateProvider].
 *
 * @param[State] The type of the state.
 * @param[block] The block that will be called to provide the state.
 * @return A [StateProvider] that provides the state returned by [block].
 */
public fun <State> provideState(
    block: () -> State,
): StateProvider<State> = StateProvider { block() }

/**
 * Convenience function to create a [StateProvider] from any [T] value.
 *
 * @param[T] The type of the state.
 * @return A [StateProvider] that provides the [T] value.
 */
public fun <T : Any> T.asStateProvider(): StateProvider<T> = StateProvider { this }
