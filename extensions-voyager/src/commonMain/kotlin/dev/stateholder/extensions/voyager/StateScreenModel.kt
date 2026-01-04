package dev.stateholder.extensions.voyager

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.stateholder.StateComposer
import dev.stateholder.StateContainer
import dev.stateholder.StateHolder
import dev.stateholder.StateProvider
import dev.stateholder.compose
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.stateContainer
import kotlinx.coroutines.flow.StateFlow

/**
 * A Voyager [ScreenModel] base class that integrates with [StateContainer] for state management.
 *
 * Extend this class to create ScreenModels with built-in state management. The state is
 * exposed via the [StateHolder] interface, allowing UI components to collect state updates.
 *
 * Example using composeState:
 *
 * ```
 * class CounterScreenModel @Inject constructor(
 *     private val counterRepository: CounterRepository,
 * ) : StateScreenModel<CounterState>(CounterState()) {
 *
 *     init {
 *         composeState {
 *             counterRepository.count into { copy(count = it) }
 *         }
 *     }
 *
 *     fun increment() {
 *         updateState { it.copy(count = it.count + 1) }
 *     }
 * }
 * ```
 *
 * Example using ComposedStateProvider:
 *
 * ```
 * class CounterScreenModel @Inject constructor(
 *     composer: CounterStateComposer,
 * ) : StateScreenModel<CounterState>(composer) {
 *
 *     fun increment() {
 *         updateState { it.copy(count = it.count + 1) }
 *     }
 * }
 *
 * // In your Screen:
 * class CounterScreen : Screen {
 *     @Composable
 *     override fun Content() {
 *         val screenModel = rememberScreenModel { CounterScreenModel() }
 *         val state by screenModel.collectAsState()
 *
 *         Button(onClick = { screenModel.increment() }) {
 *             Text("Count: ${state.count}")
 *         }
 *     }
 * }
 * ```
 *
 * @param State The type of state managed by this ScreenModel.
 * @param initialState The initial state value.
 */
@Suppress("MemberVisibilityCanBePrivate")
public abstract class StateScreenModel<State>(
    initialState: State,
) : ScreenModel, StateHolder<State> {

    /**
     * The [StateContainer] used to manage state.
     */
    protected val stateContainer: StateContainer<State> = stateContainer(initialState)

    override val state: StateFlow<State> = stateContainer.state

    /**
     * Creates a [StateScreenModel] with state provided by a [StateProvider].
     *
     * @param stateProvider A provider that supplies the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(stateProvider.provide())

    /**
     * Creates a [StateScreenModel] with a [ComposedStateProvider].
     *
     * The composer supplies both the initial state and the composition logic.
     *
     * @param composer A provider that supplies the initial state and composition logic.
     */
    public constructor(composer: ComposedStateProvider<State>) : this(composer.provide()) {
        composeState { with(composer) { compose() } }
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
