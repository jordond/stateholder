package dev.stateholder.extensions.viewmodel

import dev.stateholder.EventHolder
import dev.stateholder.StateProvider
import dev.stateholder.eventHolder
import dev.stateholder.provider.ComposedStateProvider

/**
 * A [StateViewModel] that also supports one-time UI events via [EventHolder].
 *
 * Extend this class when your ViewModel needs to emit one-time events to the UI layer,
 * such as showing snackbars, navigating, or displaying dialogs. Events are queued and
 * must be explicitly handled to be removed from the queue.
 *
 * Example using composeState:
 *
 * ```
 * data class FormState(val isLoading: Boolean = false)
 *
 * sealed interface FormEvent {
 *     data class ShowError(val message: String) : FormEvent
 *     data object NavigateToSuccess : FormEvent
 * }
 *
 * class FormViewModel @Inject constructor(
 *     private val formRepository: FormRepository,
 * ) : UiStateViewModel<FormState, FormEvent>(FormState()) {
 *
 *     init {
 *         composeState {
 *             formRepository.formData into { copy(data = it) }
 *         }
 *     }
 *
 *     fun submit() {
 *         updateState { it.copy(isLoading = true) }
 *         // ... perform submission
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * Example using ComposedStateProvider:
 *
 * ```
 * class FormViewModel @Inject constructor(
 *     composer: FormStateComposer,
 * ) : UiStateViewModel<FormState, FormEvent>(composer) {
 *
 *     fun submit() {
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
 * @param initialState The initial state value.
 */
public abstract class UiStateViewModel<State, Event>(
    initialState: State,
) : StateViewModel<State>(initialState), EventHolder<Event> by eventHolder() {

    /**
     * Creates a [UiStateViewModel] with state provided by a [StateProvider].
     *
     * @param stateProvider A provider that supplies the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(stateProvider.provide())

    /**
     * Creates a [UiStateViewModel] with a [ComposedStateProvider].
     *
     * The composer supplies both the initial state and the composition logic.
     *
     * @param composer A provider that supplies the initial state and composition logic.
     */
    public constructor(composer: ComposedStateProvider<State>) : this(composer.provide()) {
        composeState { with(composer) { compose() } }
    }
}
