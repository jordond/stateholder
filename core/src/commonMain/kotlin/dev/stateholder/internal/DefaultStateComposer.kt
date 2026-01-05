package dev.stateholder.internal

import dev.stateholder.provider.FlowStateProvider
import dev.stateholder.StateComposer
import dev.stateholder.StateContainer
import dev.stateholder.StateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [StateComposer].
 */
internal class DefaultStateComposer<State>(
    private val scope: CoroutineScope,
    private val container: StateContainer<State>,
) : StateComposer<State> {
    override fun <T> slice(
        flow: Flow<T>,
        merge: suspend State.(T) -> State,
    ): Job = container.merge(flow, scope) { state, value ->
        state.merge(value)
    }

    override fun <T> slice(
        provider: FlowStateProvider<T>,
        merge: suspend State.(T) -> State,
    ): Job = slice(provider.states(), merge)

    override fun <T> slice(
        container: StateContainer<T>,
        merge: suspend State.(T) -> State,
    ): Job = slice(container.state, merge)

    override fun <T> slice(
        holder: StateHolder<T>,
        merge: suspend State.(T) -> State,
    ): Job = slice(holder.state, merge)

    override infix fun <T> Flow<T>.into(
        merge: suspend State.(T) -> State,
    ): Job = slice(this, merge)

    override infix fun <T> FlowStateProvider<T>.into(
        merge: suspend State.(T) -> State,
    ): Job = slice(this, merge)

    override infix fun <T> StateContainer<T>.into(
        merge: suspend State.(T) -> State,
    ): Job = slice(this, merge)

    override infix fun <T> StateHolder<T>.into(
        merge: suspend State.(T) -> State,
    ): Job = slice(this, merge)
}
