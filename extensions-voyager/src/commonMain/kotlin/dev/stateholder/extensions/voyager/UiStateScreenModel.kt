package dev.stateholder.extensions.voyager

import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.EventHolder
import dev.stateholder.StateProvider
import dev.stateholder.eventHolder

/**
 * A [StateScreenModel] that also supports one-time UI events via [EventHolder].
 *
 * Extend this class when your Voyager ScreenModel needs to emit one-time events to the UI layer,
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
 * class FormScreenModel @Inject constructor(
 *     private val formRepository: FormRepository,
 * ) : UiStateScreenModel<FormState, FormEvent>(FormState()) {
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
 * class FormScreenModel @Inject constructor(
 *     composer: FormStateComposer,
 * ) : UiStateScreenModel<FormState, FormEvent>(composer) {
 *
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 *
 * // In your Screen:
 * class FormScreen : Screen {
 *     @Composable
 *     override fun Content() {
 *         val screenModel = rememberScreenModel { FormScreenModel() }
 *         val navigator = LocalNavigator.currentOrThrow
 *
 *         HandleEvents(screenModel) { event ->
 *             when (event) {
 *                 is FormEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
 *                 FormEvent.NavigateToSuccess -> navigator.push(SuccessScreen)
 *             }
 *         }
 *         // ... rest of UI
 *     }
 * }
 * ```
 *
 * @param State The type of state managed by this ScreenModel.
 * @param Event The type of one-time events emitted by this ScreenModel.
 * @param initialState The initial state value.
 */
public abstract class UiStateScreenModel<State, Event>(
    initialState: State,
) : StateScreenModel<State>(initialState), EventHolder<Event> by eventHolder() {

    /**
     * Creates a [UiStateScreenModel] with state provided by a [StateProvider].
     *
     * @param stateProvider A provider that supplies the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(stateProvider.provide())

    /**
     * Creates a [UiStateScreenModel] with a [ComposedStateProvider].
     *
     * The composer supplies both the initial state and the composition logic.
     *
     * @param composer A provider that supplies the initial state and composition logic.
     */
    public constructor(composer: ComposedStateProvider<State>) : this(composer.provide()) {
        composeState { with(composer) { compose() } }
    }
}
