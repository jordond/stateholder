package dev.stateholder.extensions.voyager

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.stateholder.StateContainer
import dev.stateholder.StateHolder
import dev.stateholder.StateProvider
import dev.stateholder.stateContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A Voyager [ScreenModel] base class that integrates with [StateContainer] for state management.
 *
 * Extend this class to create ScreenModels with built-in state management. The state is
 * exposed via the [StateHolder] interface, allowing UI components to collect state updates.
 *
 * Example:
 *
 * ```
 * data class CounterState(val count: Int = 0)
 *
 * class CounterScreenModel : StateScreenModel<CounterState>(CounterState()) {
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
 * @param stateContainer The [StateContainer] used to manage state.
 */
@Suppress("MemberVisibilityCanBePrivate")
public abstract class StateScreenModel<State>(
    protected val stateContainer: StateContainer<State>,
) : ScreenModel, StateHolder<State> {

    /**
     * Creates a [StateScreenModel] with state provided by a [StateProvider].
     *
     * @param stateProvider A provider that supplies the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(stateContainer(stateProvider))

    /**
     * Creates a [StateScreenModel] with the given initial state.
     *
     * @param initialState The initial state value.
     */
    public constructor(initialState: State) : this(stateContainer(initialState))

    override val state: StateFlow<State> = stateContainer.state

    /**
     * Merges emissions from this [Flow] into the current state.
     *
     * Each emission triggers [block] with the current state and emitted value,
     * and the returned state becomes the new state.
     *
     * @param scope The [CoroutineScope] to collect in. Defaults to [screenModelScope].
     * @param block A suspend function that combines the current state with the emitted value.
     * @return A [Job] that can be used to cancel the collection.
     */
    protected fun <T> Flow<T>.mergeState(
        scope: CoroutineScope = screenModelScope,
        block: suspend (state: State, value: T) -> State,
    ): Job {
        return stateContainer.merge(this, scope, block)
    }

    /**
     * Merges state from another [StateHolder] into this ScreenModel's state.
     *
     * @param scope The [CoroutineScope] to collect in. Defaults to [screenModelScope].
     * @param block A suspend function that combines the current state with the other holder's state.
     * @return A [Job] that can be used to cancel the collection.
     */
    protected fun <T> StateHolder<T>.mergeState(
        scope: CoroutineScope = screenModelScope,
        block: suspend (state: State, value: T) -> State,
    ): Job = state.mergeState(scope, block)

    /**
     * Updates the current state using the provided transformation [block].
     *
     * @param block A function that transforms the current state into a new state.
     */
    protected fun updateState(block: (State) -> State) {
        stateContainer.update(block)
    }
}