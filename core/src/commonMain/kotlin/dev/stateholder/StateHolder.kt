package dev.stateholder

import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes a [state] property that can be used to access the state of a [StateContainer].
 */
public interface StateHolder<State> {
    public val state: StateFlow<State>

    public companion object {
        /**
         * Create a state owner from the given [stateContainer].
         */
        internal fun <State> from(
            stateContainer: StateContainer<State>,
        ): StateHolder<State> = object : StateHolder<State> {
            override val state: StateFlow<State> get() = stateContainer.state
        }
    }
}

/**
 * Create a [StateHolder] from the given [StateContainer].
 *
 * This is primarily used when delegating to the [StateContainer.state] property.
 *
 * Example:
 *
 * ```
 * class MyModel(
 *    private val container: StateContainer<Boolean>
 * ) : StateHolder<Boolean> by container.asStateHolder() {
 *  // ...
 * }
 * ```
 */
public fun <State> StateContainer<State>.asStateHolder(): StateHolder<State> {
    return StateHolder.from(this)
}
