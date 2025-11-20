package dev.stateholder.dispatcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

@Immutable
@Composable
public fun <Action> Dispatcher<Action>.rememberRelay(action: Action): () -> Unit =
    remember(action) { relay(action) }

@Immutable
@Composable
public fun <Action, T1> Dispatcher<Action>.rememberRelayOf(
    action: KFunction1<T1, Action>,
): (T1) -> Unit = remember(action) { { t1 -> dispatch(action(t1)) } }

@Immutable
@Composable
public fun <Action, T1, T2> Dispatcher<Action>.rememberRelayOf(
    action: KFunction2<T1, T2, Action>,
): (T1, T2) -> Unit = remember(action) { { t1, t2 -> dispatch(action(t1, t2)) } }

@Immutable
@Composable
public fun <Action, T1, T2, T3> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3) -> Action,
): (T1, T2, T3) -> Unit = remember(action) { { t1, t2, t3 -> dispatch(action(t1, t2, t3)) } }

@Immutable
@Composable
public fun <Action, T1, T2, T3, T4> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4) -> Action,
): (T1, T2, T3, T4) -> Unit =
    remember(action) { { t1, t2, t3, t4 -> dispatch(action(t1, t2, t3, t4)) } }

@Immutable
@Composable
public fun <Action, T1, T2, T3, T4, T5> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4, T5) -> Action,
): (T1, T2, T3, T4, T5) -> Unit =
    remember(action) { { t1, t2, t3, t4, t5 -> dispatch(action(t1, t2, t3, t4, t5)) } }

@Immutable
@Composable
public fun <Action, T1, T2, T3, T4, T5, T6> Dispatcher<Action>.rememberRelayOf(
    action: (T1, T2, T3, T4, T5, T6) -> Action,
): (T1, T2, T3, T4, T5, T6) -> Unit =
    remember(action) { { t1, t2, t3, t4, t5, t6 -> dispatch(action(t1, t2, t3, t4, t5, t6)) } }

@Immutable
@Composable
public fun <Action> rememberDispatcher(block: (Action) -> Unit): Dispatcher<Action> = remember {
    Dispatcher(block)
}

@Immutable
@Composable
public fun <Action> rememberDispatcher(
    debounce: Long,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = remember(debounce, exclude, block) { Dispatcher(debounce, exclude, block) }

@Immutable
@Composable
public fun <Action> rememberDebounceDispatcher(
    debounce: Long = 100,
    exclude: (Action) -> Boolean = { false },
    block: (Action) -> Unit,
): Dispatcher<Action> = rememberDispatcher(debounce, exclude, block)