package dev.stateholder

import dev.stateholder.internal.DefaultStateComposer
import dev.stateholder.internal.DefaultStateContainer
import dev.stateholder.provider.ComposedStateProvider
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
 * Create a [StateContainer] with the given [provider].
 *
 * @see [StateContainer]
 */
public fun <State> stateContainer(
    provider: StateProvider<State>,
): StateContainer<State> = StateContainer.create(provider)

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

/**
 * Composes state from multiple reactive sources using the [StateComposer] DSL.
 *
 * This allows you to declaratively wire up flows that should update portions of your state.
 * Can be called multiple times to add more compositions.
 *
 * Example:
 *
 * ```
 * val container = stateContainer(MyState())
 * container.compose(viewModelScope) {
 *     userRepository.currentUser into { copy(user = it) }
 *     cartRepository.cart into { copy(cart = it) }
 * }
 * ```
 *
 * @param scope The coroutine scope for collecting flows.
 * @param block The DSL for composing state from flows.
 */
public fun <State> StateContainer<State>.compose(
    scope: CoroutineScope,
    block: StateComposer<State>.() -> Unit,
) {
    DefaultStateComposer(scope, this).apply(block)
}

/**
 * Composes state from a [dev.stateholder.provider.ComposedStateProvider].
 *
 * @param composeProvider The provider that supplies initial state and composition logic.
 */
public fun <State> StateContainer<State>.compose(composeProvider: ComposedStateProvider<State>) {
    with(composeProvider) {
        this@compose.compose(this)
    }
}

/**
 * Create a [StateContainer] with the given [initialState] and compose it using the [StateComposer] DSL.
 *
 * The composer allows you to declaratively wire up flows that should update portions of your state.
 *
 * Example:
 *
 * ```
 * val container = stateContainer(viewModelScope, MyState()) {
 *     userRepository.currentUser into { copy(user = it) }
 *     cartRepository.cart into { copy(cart = it) }
 * }
 * ```
 *
 * @param State The type of state.
 * @param scope The coroutine scope for collecting flows.
 * @param initialState The initial state value.
 * @param composer The DSL for composing state from flows.
 * @return A [StateContainer] with the composed state.
 */
public fun <State> stateContainer(
    scope: CoroutineScope,
    initialState: State,
    composer: StateComposer<State>.() -> Unit,
): StateContainer<State> = stateContainer(initialState).also {
    it.compose(scope, composer)
}

/**
 * Create a [StateContainer] with state from [initialStateProvider] and compose it using the [StateComposer] DSL.
 *
 * @param State The type of state.
 * @param scope The coroutine scope for collecting flows.
 * @param initialStateProvider The provider for the initial state.
 * @param composer The DSL for composing state from flows.
 * @return A [StateContainer] with the composed state.
 */
public fun <State> stateContainer(
    scope: CoroutineScope,
    initialStateProvider: StateProvider<State>,
    composer: StateComposer<State>.() -> Unit,
): StateContainer<State> = stateContainer(scope, initialStateProvider.provide(), composer)

/**
 * Create a [StateContainer] from a [dev.stateholder.provider.ComposedStateProvider].
 *
 * The provider supplies both the initial state and the composition logic.
 *
 * Example:
 *
 * ```
 * class ShopStateComposer @Inject constructor(...) : ComposedStateProvider<ShopState> { ... }
 *
 * val container = stateContainer(viewModelScope, composer)
 * ```
 *
 * @param State The type of state.
 * @param scope The coroutine scope for collecting flows.
 * @param composer The provider that supplies initial state and composition logic.
 * @return A [StateContainer] with the composed state.
 */
public fun <State> stateContainer(
    scope: CoroutineScope,
    composer: ComposedStateProvider<State>,
): StateContainer<State> = stateContainer(composer.provide()).also {
    it.compose(scope) { with(composer) { compose() } }
}
