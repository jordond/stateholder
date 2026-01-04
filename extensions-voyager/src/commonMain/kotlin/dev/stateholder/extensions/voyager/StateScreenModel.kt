package dev.stateholder.extensions.voyager

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.stateholder.StateComposer
import dev.stateholder.StateContainer
import dev.stateholder.StateHolder
import dev.stateholder.StateProvider
import dev.stateholder.compose
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider
import dev.stateholder.stateContainer
import kotlinx.coroutines.flow.StateFlow

/**
 * A Voyager [ScreenModel] base class that integrates with [StateContainer] for state management.
 *
 * Extend this class to create ScreenModels with built-in state management. The state is
 * exposed via the [StateHolder] interface, allowing UI components to collect state updates.
 *
 * There are several ways to construct a StateScreenModel:
 *
 * **Simple initial state:**
 * ```
 * class CounterScreenModel : StateScreenModel<CounterState>(CounterState())
 * ```
 *
 * **With inline composition:**
 * ```
 * class CounterScreenModel @Inject constructor(
 *     private val counterRepository: CounterRepository,
 * ) : StateScreenModel<CounterState>(
 *     initialState = CounterState(),
 *     composer = {
 *         counterRepository.count into { copy(count = it) }
 *     },
 * )
 * ```
 *
 * **With ComposedStateProvider (for dependency injection):**
 * ```
 * class CounterScreenModel @Inject constructor(
 *     composer: CounterStateComposer,
 * ) : StateScreenModel<CounterState>(composer)
 * ```
 *
 * **With a StateProvider:**
 * ```
 * class CounterScreenModel @Inject constructor(
 *     counterStateProvider: CounterStateProvider,
 *     private val counterRepository: CounterRepository,
 * ) : StateScreenModel<CounterState>(
 *     stateProvider = counterStateProvider,
 *     composer = {
 *         counterRepository.count into { copy(count = it) }
 *     },
 * )
 * ```
 *
 * You can also compose state in the `init` block using [composeState]:
 * ```
 * class CounterScreenModel @Inject constructor(
 *     private val counterRepository: CounterRepository,
 * ) : StateScreenModel<CounterState>(CounterState()) {
 *     init {
 *         composeState {
 *             counterRepository.count into { copy(count = it) }
 *         }
 *     }
 * }
 * ```
 *
 * @param State The type of state managed by this ScreenModel.
 * @param composer The provider that supplies initial state and composition logic.
 */
@Suppress("MemberVisibilityCanBePrivate")
public abstract class StateScreenModel<State>(
    composer: ComposedStateProvider<State>,
) : ScreenModel, StateHolder<State> {
    /**
     * Creates a [StateScreenModel] with the given initial state.
     *
     * Example:
     *
     * ```
     * class CounterScreenModel : StateScreenModel<CounterState>(CounterState())
     * ```
     *
     * @param initialState The initial state.
     */
    public constructor(initialState: State) : this(composedStateProvider(initialState))

    /**
     * Creates a [StateScreenModel] with the given initial state and composition logic.
     *
     * Example:
     *
     * ```
     * class CounterScreenModel @Inject constructor(
     *     private val counterRepository: CounterRepository,
     * ) : StateScreenModel<CounterState>(
     *     initialState = CounterState(),
     *     composer = {
     *         counterRepository.count into { copy(count = it) }
     *     },
     * )
     * ```
     *
     * @param initialState The initial state.
     * @param composer The DSL for composing state from flows.
     */
    public constructor(
        initialState: State,
        composer: StateComposer<State>.() -> Unit,
    ) : this(composedStateProvider(initialState, composer))

    /**
     * Creates a [StateScreenModel] with the given [StateProvider].
     *
     * Example:
     *
     * ```
     * class CounterScreenModel @Inject constructor(
     *     counterStateProvider: CounterStateProvider,
     * ) : StateScreenModel<CounterState>(counterStateProvider)
     * ```
     *
     * @param stateProvider The provider for the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(
        composedStateProvider(stateProvider)
    )

    /**
     * Creates a [StateScreenModel] with the given [StateProvider] and composition logic.
     *
     * Example:
     *
     * ```
     * class CounterScreenModel @Inject constructor(
     *     counterStateProvider: CounterStateProvider,
     *     private val counterRepository: CounterRepository,
     * ) : StateScreenModel<CounterState>(
     *     stateProvider = counterStateProvider,
     *     composer = {
     *         counterRepository.count into { copy(count = it) }
     *     },
     * )
     * ```
     *
     * @param stateProvider The provider for the initial state.
     * @param composer The DSL for composing state from flows.
     */
    public constructor(
        stateProvider: StateProvider<State>,
        composer: StateComposer<State>.() -> Unit,
    ) : this(composedStateProvider(stateProvider, composer))

    /**
     * The [StateContainer] used to manage state.
     */
    protected val stateContainer: StateContainer<State> = stateContainer(composer.provide())

    /**
     * @see StateHolder.state
     */
    override val state: StateFlow<State> = stateContainer.state

    init {
        stateContainer.compose(composer)
    }

    /**
     * Composes state from multiple reactive sources using the [StateComposer] DSL.
     *
     * Call this in your `init` block to wire up flows that should update portions of your state.
     *
     * Example:
     *
     * ```
     * init {
     *     composeState {
     *         userRepository.currentUser into { copy(user = it) }
     *         cartRepository.cart into { copy(cart = it) }
     *     }
     * }
     * ```
     *
     * @param composer The DSL for composing state from flows.
     */
    protected fun composeState(composer: StateComposer<State>.() -> Unit) {
        stateContainer.compose(screenModelScope, composer)
    }

    /**
     * Updates the current state using the provided transformation [block].
     *
     * @param block A function that transforms the current state into a new state.
     */
    protected fun updateState(block: (State) -> State) {
        stateContainer.update(block)
    }
}
