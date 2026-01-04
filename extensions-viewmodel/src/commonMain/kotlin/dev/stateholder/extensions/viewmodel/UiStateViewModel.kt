package dev.stateholder.extensions.viewmodel

import dev.stateholder.EventHolder
import dev.stateholder.StateComposer
import dev.stateholder.StateProvider
import dev.stateholder.eventHolder
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider

/**
 * A [StateViewModel] that also supports one-time UI events via [EventHolder].
 *
 * Extend this class when your ViewModel needs to emit one-time events to the UI layer,
 * such as showing snackbars, navigating, or displaying dialogs. Events are queued and
 * must be explicitly handled to be removed from the queue.
 *
 * There are several ways to construct a UiStateViewModel:
 *
 * **Simple initial state:**
 * ```
 * class FormViewModel : UiStateViewModel<FormState, FormEvent>(FormState()) {
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **With inline composition:**
 * ```
 * class FormViewModel @Inject constructor(
 *     private val formRepository: FormRepository,
 * ) : UiStateViewModel<FormState, FormEvent>(
 *     initialState = FormState(),
 *     composer = {
 *         formRepository.formData into { copy(data = it) }
 *     },
 * ) {
 *     fun submit() {
 *         updateState { it.copy(isLoading = true) }
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **With ComposedStateProvider (for dependency injection):**
 * ```
 * class FormViewModel @Inject constructor(
 *     composer: FormStateComposer,
 * ) : UiStateViewModel<FormState, FormEvent>(composer) {
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **With a StateProvider:**
 * ```
 * class FormViewModel @Inject constructor(
 *     formStateProvider: FormStateProvider,
 *     private val formRepository: FormRepository,
 * ) : UiStateViewModel<FormState, FormEvent>(
 *     stateProvider = formStateProvider,
 *     composer = {
 *         formRepository.formData into { copy(data = it) }
 *     },
 * ) {
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **Handling events in your UI:**
 * ```
 * @Composable
 * fun FormScreen(viewModel: FormViewModel) {
 *     HandleEvents(viewModel) { event ->
 *         when (event) {
 *             is FormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
 *             FormEvent.NavigateToSuccess -> navigator.push(SuccessScreen)
 *         }
 *     }
 * }
 * ```
 *
 * @param State The type of state managed by this ViewModel.
 * @param Event The type of one-time events emitted by this ViewModel.
 * @param composer The provider that supplies initial state and composition logic.
 */
public abstract class UiStateViewModel<State, Event>(
    composer: ComposedStateProvider<State>,
) : StateViewModel<State>(composer), EventHolder<Event> by eventHolder() {
    /**
     * Creates a [UiStateViewModel] with the given initial state.
     *
     * Example:
     *
     * ```
     * class FormViewModel : UiStateViewModel<FormState, FormEvent>(FormState())
     * ```
     *
     * @param initialState The initial state.
     */
    public constructor(initialState: State) : this(composedStateProvider(initialState))

    /**
     * Creates a [UiStateViewModel] with the given initial state and composition logic.
     *
     * Example:
     *
     * ```
     * class FormViewModel @Inject constructor(
     *     private val formRepository: FormRepository,
     * ) : UiStateViewModel<FormState, FormEvent>(
     *     initialState = FormState(),
     *     composer = {
     *         formRepository.formData into { copy(data = it) }
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
     * Creates a [UiStateViewModel] with the given [StateProvider].
     *
     * Example:
     *
     * ```
     * class FormViewModel @Inject constructor(
     *     formStateProvider: FormStateProvider,
     * ) : UiStateViewModel<FormState, FormEvent>(formStateProvider)
     * ```
     *
     * @param stateProvider The provider for the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(
        composedStateProvider(stateProvider)
    )

    /**
     * Creates a [UiStateViewModel] with the given [StateProvider] and composition logic.
     *
     * Example:
     *
     * ```
     * class FormViewModel @Inject constructor(
     *     formStateProvider: FormStateProvider,
     *     private val formRepository: FormRepository,
     * ) : UiStateViewModel<FormState, FormEvent>(
     *     stateProvider = formStateProvider,
     *     composer = {
     *         formRepository.formData into { copy(data = it) }
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
}
