package dev.stateholder.extensions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.stateholder.StateHolder
import dev.stateholder.StateOwner
import dev.stateholder.StateProvider
import dev.stateholder.stateContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Suppress("MemberVisibilityCanBePrivate")
public abstract class StateViewModel<State>(
    protected val stateHolder: StateHolder<State>,
) : ViewModel(), StateOwner<State> {

    public constructor(stateProvider: StateProvider<State>) : this(stateContainer(stateProvider))

    public constructor(initialState: State) : this(stateContainer(initialState))

    override val state: StateFlow<State> = stateHolder.state

    protected fun <T> Flow<T>.mergeState(
        scope: CoroutineScope = viewModelScope,
        block: suspend (state: State, value: T) -> State,
    ): Job {
        return stateHolder.addSource(this, scope, block)
    }

    protected fun <T> StateOwner<T>.mergeState(
        scope: CoroutineScope = viewModelScope,
        block: suspend (state: State, value: T) -> State,
    ): Job = state.mergeState(scope, block)

    protected fun updateState(block: (State) -> State) {
        stateHolder.update(block)
    }
}