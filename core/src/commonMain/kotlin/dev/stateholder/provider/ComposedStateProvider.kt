package dev.stateholder.provider

import dev.stateholder.StateComposer
import dev.stateholder.StateContainer
import dev.stateholder.StateProvider

/**
 * A provider that encapsulates both initial state and state composition logic.
 *
 * Use this interface to create injectable, testable state composers that can be
 * passed to ViewModels or ScreenModels.
 *
 * Example using [FlowStateProvider]s:
 *
 * ```
 * class ShopStateComposer @Inject constructor(
 *     userStateProvider: UserStateProvider,
 *     cartStateProvider: CartStateProvider,
 * ) : ComposedStateProvider<ShopState> by composedStateProvider(
 *     initialState = ShopState(),
 *     composer = {
 *         userStateProvider into { copy(user = it) }
 *         cartStateProvider into { copy(cart = it) }
 *     }
 * )
 *
 * class ShopViewModel @Inject constructor(
 *     composer: ShopStateComposer,
 * ) : StateViewModel<ShopState>(composer)
 * ```
 *
 * Example using raw flows from repositories:
 *
 * ```
 * class DashboardStateComposer @Inject constructor(
 *     private val userRepository: UserRepository,
 *     private val analyticsRepository: AnalyticsRepository,
 * ) : ComposedStateProvider<DashboardState> by composedStateProvider(
 *     initialState = DashboardState(),
 * ) {
 *     override fun StateComposer<DashboardState>.compose() {
 *         userRepository.currentUser into { copy(user = it) }
 *         analyticsRepository.metrics into { copy(metrics = it) }
 *         analyticsRepository.recentActivity into { copy(activity = it) }
 *     }
 * }
 * ```
 *
 * @param State The type of state being composed.
 */
public interface ComposedStateProvider<State> : StateProvider<State> {
    /**
     * Composes the state by wiring up flows using the [StateComposer] DSL.
     *
     * This method is called when the provider is used to create a [StateContainer].
     */
    public fun StateComposer<State>.compose()
}

/**
 * Creates a [ComposedStateProvider] for use with delegation.
 *
 * This is useful for creating injectable state composers with minimal boilerplate.
 *
 * Example:
 *
 * ```
 * class ShopStateComposer @Inject constructor(
 *     private val userStateProvider: UserStateProvider,
 *     private val cartStateProvider: CartStateProvider,
 * ) : ComposedStateProvider<ShopState> by composedStateProvider(
 *     initialState = ShopState(),
 *     composer = {
 *         userStateProvider into { copy(user = it) }
 *         cartStateProvider into { copy(cart = it) }
 *     }
 * )
 * ```
 *
 * @param State The type of state.
 * @param initialState The initial state value returned by [ComposedStateProvider.provide].
 * @param composer The DSL for composing state from flows.
 * @return A [ComposedStateProvider] that can be used with delegation.
 */
public fun <State> composedStateProvider(
    initialState: State,
    composer: StateComposer<State>.() -> Unit = {},
): ComposedStateProvider<State> = object : ComposedStateProvider<State> {
    override fun provide(): State = initialState
    override fun StateComposer<State>.compose() = composer()
}

/**
 * Creates a [ComposedStateProvider] for use with delegation.
 *
 * This is useful for creating injectable state composers with minimal boilerplate.
 *
 * Example:
 *
 * ```
 * class ShopStateComposer @Inject constructor(
 *     private val shopStateProvider: ShopStateProvider,
 *     private val userStateProvider: UserStateProvider,
 *     private val cartStateProvider: CartStateProvider,
 * ) : ComposedStateProvider<ShopState> by composedStateProvider(
 *     provider = shopStateProvider,
 *     composer = {
 *         userStateProvider into { copy(user = it) }
 *         cartStateProvider into { copy(cart = it) }
 *     }
 * )
 * ```
 *
 * @param State The type of state.
 * @param provider A [StateProvider] that supplies the initial state.
 * @param composer The DSL for composing state from flows.
 * @return A [ComposedStateProvider] that can be used with delegation.
 */
public fun <State> composedStateProvider(
    provider: StateProvider<State>,
    composer: StateComposer<State>.() -> Unit = {},
): ComposedStateProvider<State> = object : ComposedStateProvider<State> {
    override fun provide(): State = provider.provide()
    override fun StateComposer<State>.compose() = composer()
}