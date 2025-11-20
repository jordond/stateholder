package dev.stateholder.dispatcher

import androidx.compose.runtime.Immutable
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

@Immutable
public fun interface Dispatcher<Action> {
    public fun dispatch(action: Action)

    public operator fun invoke(action: Action) {
        dispatch(action)
    }

    public fun relay(action: Action): () -> Unit = { dispatch(action) }

    public fun <T1> relayOf(action: KFunction1<T1, Action>): (T1) -> Unit =
        { t1 -> dispatch(action(t1)) }

    public fun <T1, T2> relayOf(action: KFunction2<T1, T2, Action>): (T1, T2) -> Unit =
        { t1, t2 -> dispatch(action(t1, t2)) }

    public fun <T1, T2, T3> relayOf(action: (T1, T2, T3) -> Action): (T1, T2, T3) -> Unit =
        { t1, t2, t3 -> dispatch(action(t1, t2, t3)) }

    public fun <T1, T2, T3, T4> relayOf(
        action: (T1, T2, T3, T4) -> Action,
    ): (T1, T2, T3, T4) -> Unit = { t1, t2, t3, t4 -> dispatch(action(t1, t2, t3, t4)) }

    public fun <T1, T2, T3, T4, T5> relayOf(
        action: (T1, T2, T3, T4, T5) -> Action,
    ): (T1, T2, T3, T4, T5) -> Unit = { t1, t2, t3, t4, t5 -> dispatch(action(t1, t2, t3, t4, t5)) }

    public fun <T1, T2, T3, T4, T5, T6> relayOf(
        action: (T1, T2, T3, T4, T5, T6) -> Action,
    ): (T1, T2, T3, T4, T5, T6) -> Unit = { t1, t2, t3, t4, t5, t6 ->
        dispatch(action(t1, t2, t3, t4, t5, t6))
    }

    public companion object
}
