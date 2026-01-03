package dev.stateholder.extensions.viewmodel

import dev.stateholder.EventHolder
import dev.stateholder.StateContainer
import dev.stateholder.StateProvider
import dev.stateholder.stateContainer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

/**
 * A [StateViewModel] that also supports one-time UI events via [EventHolder].
 *
 * Extend this class when your ViewModel needs to emit one-time events to the UI layer,
 * such as showing snackbars, navigating, or displaying dialogs. Events are queued and
 * must be explicitly handled to be removed from the queue.
 *
 * Example:
 *
 * ```
 * data class FormState(val isLoading: Boolean = false)
 *
 * sealed interface FormEvent {
 *     data class ShowError(val message: String) : FormEvent
 *     data object NavigateToSuccess : FormEvent
 * }
 *
 * class FormViewModel : UiStateViewModel<FormState, FormEvent>(FormState()) {
 *     fun submit() {
 *         updateState { it.copy(isLoading = true) }
 *         // ... perform submission
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 *
 * // In your UI:
 * @Composable
 * fun FormScreen(viewModel: FormViewModel) {
 *     HandleEvents(viewModel) { event ->
 *         when (event) {
 *             is FormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
 *             FormEvent.NavigateToSuccess -> navigator.push(SuccessScreen)
 *         }
 *     }
 *     // ... rest of UI
 * }
 * ```
 *
 * @param State The type of state managed by this ViewModel.
 * @param Event The type of one-time events emitted by this ViewModel.
 * @param stateContainer The [StateContainer] used to manage state.
 */
public abstract class UiStateViewModel<State, Event>(
    stateContainer: StateContainer<State>,
) : StateViewModel<State>(stateContainer), EventHolder<Event> {

    /**
     * Creates a [UiStateViewModel] with state provided by a [StateProvider].
     *
     * @param stateProvider A provider that supplies the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(stateContainer(stateProvider))

    /**
     * Creates a [UiStateViewModel] with the given initial state.
     *
     * @param initialState The initial state value.
     */
    public constructor(initialState: State) : this(stateContainer(initialState))

    /**
     * Internal container for managing the event queue.
     */
    protected val eventContainer: StateContainer<PersistentList<Event>> =
        stateContainer(persistentListOf())

    public override val events: StateFlow<PersistentList<Event>> = eventContainer.state

    override fun handle(event: Event) {
        eventContainer.update { it.remove(event) }
    }

    /**
     * Emits a one-time [event] to be handled by the UI layer.
     *
     * The event is added to the queue and remains until [handle] is called.
     *
     * @param event The event to emit.
     */
    protected fun emit(event: Event) {
        eventContainer.update { it.add(event) }
    }
}