package dev.stateholder.extensions.voyager

import dev.stateholder.EventHolder
import dev.stateholder.StateComposer
import dev.stateholder.StateProvider
import dev.stateholder.eventHolder
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider

/**
 * A [StateScreenModel] that also supports one-time UI events via [EventHolder].
 *
 * Extend this class when your Voyager ScreenModel needs to emit one-time events to the UI layer,
 * such as showing snackbars, navigating, or displaying dialogs. Events are queued and
 * must be explicitly handled to be removed from the queue.
 *
 * There are several ways to construct a UiStateScreenModel:
 *
 * **Simple initial state:**
 * ```
 * class FormScreenModel : UiStateScreenModel<FormState, FormEvent>(FormState()) {
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **With inline composition:**
 * ```
 * class FormScreenModel @Inject constructor(
 *     private val formRepository: FormRepository,
 * ) : UiStateScreenModel<FormState, FormEvent>(
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
 * class FormScreenModel @Inject constructor(
 *     composer: FormStateComposer,
 * ) : UiStateScreenModel<FormState, FormEvent>(composer) {
 *     fun submit() {
 *         emit(FormEvent.NavigateToSuccess)
 *     }
 * }
 * ```
 *
 * **With a StateProvider:**
 * ```
 * class FormScreenModel @Inject constructor(
 *     formStateProvider: FormStateProvider,
 *     private val formRepository: FormRepository,
 * ) : UiStateScreenModel<FormState, FormEvent>(
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
 * **Handling events in your Screen:**
 * ```
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
 *     }
 * }
 * ```
 *
 * @param State The type of state managed by this ScreenModel.
 * @param Event The type of one-time events emitted by this ScreenModel.
 * @param composer The provider that supplies initial state and composition logic.
 */
public abstract class UiStateScreenModel<State, Event>(
    composer: ComposedStateProvider<State>,
) : StateScreenModel<State>(composer), EventHolder<Event> by eventHolder() {
    /**
     * Creates a [UiStateScreenModel] with the given initial state.
     *
     * Example:
     *
     * ```
     * class FormScreenModel : UiStateScreenModel<FormState, FormEvent>(FormState())
     * ```
     *
     * @param initialState The initial state.
     */
    public constructor(initialState: State) : this(composedStateProvider(initialState))

    /**
     * Creates a [UiStateScreenModel] with the given initial state and composition logic.
     *
     * Example:
     *
     * ```
     * class FormScreenModel @Inject constructor(
     *     private val formRepository: FormRepository,
     * ) : UiStateScreenModel<FormState, FormEvent>(
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
     * Creates a [UiStateScreenModel] with the given [StateProvider].
     *
     * Example:
     *
     * ```
     * class FormScreenModel @Inject constructor(
     *     formStateProvider: FormStateProvider,
     * ) : UiStateScreenModel<FormState, FormEvent>(formStateProvider)
     * ```
     *
     * @param stateProvider The provider for the initial state.
     */
    public constructor(stateProvider: StateProvider<State>) : this(
        composedStateProvider(stateProvider)
    )

    /**
     * Creates a [UiStateScreenModel] with the given [StateProvider] and composition logic.
     *
     * Example:
     *
     * ```
     * class FormScreenModel @Inject constructor(
     *     formStateProvider: FormStateProvider,
     *     private val formRepository: FormRepository,
     * ) : UiStateScreenModel<FormState, FormEvent>(
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
