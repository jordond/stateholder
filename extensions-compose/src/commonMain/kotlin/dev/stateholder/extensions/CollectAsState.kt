package dev.stateholder.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.stateholder.StateHolder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects the [StateHolder.state] flow as Compose [State] with lifecycle awareness.
 *
 * This extension wraps [collectAsStateWithLifecycle] to automatically pause collection
 * when the lifecycle falls below [minActiveState], preventing unnecessary updates when
 * the UI is not visible.
 *
 * Example:
 *
 * ```
 * @Composable
 * fun MyScreen(viewModel: MyViewModel) {
 *     val state by viewModel.collectAsState()
 *
 *     Text(text = state.message)
 * }
 * ```
 *
 * @param lifecycleOwner The [LifecycleOwner] to use for lifecycle-aware collection.
 *   Defaults to [LocalLifecycleOwner].
 * @param minActiveState The minimum [Lifecycle.State] required to collect updates.
 *   Defaults to [Lifecycle.State.STARTED].
 * @param context Optional [CoroutineContext] to use for collection.
 * @return A Compose [State] containing the current value from the [StateHolder].
 */
@Composable
public fun <T> StateHolder<T>.collectAsState(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
): State<T> {
    return state.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner,
        minActiveState = minActiveState,
        context = context,
    )
}
