package dev.stateholder.dispatcher

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class DebounceDispatcherTest {
    @Test
    fun shouldDispatchFirstActionImmediately() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test")
            dispatchCount shouldBe 1
        }

    @Test
    fun shouldDebounceRepeatedActions() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test")
            dispatcher.dispatch("test")
            dispatcher.dispatch("test")

            dispatchCount shouldBe 1
        }

    @Test
    fun shouldDispatchDifferentActionsImmediately() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test1")
            dispatcher.dispatch("test2")
            dispatcher.dispatch("test3")

            dispatchCount shouldBe 3
        }

    @Test
    fun shouldDispatchExcludedActionsImmediately() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    exclude = { it == "excluded" },
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("excluded")
            dispatcher.dispatch("excluded")
            dispatcher.dispatch("excluded")

            dispatchCount shouldBe 3
        }

    @Test
    fun shouldDispatchAfterDebounceTime() =
        runTest {
            var lastDispatchedAction: String? = null
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 50,
                    block = { lastDispatchedAction = it },
                )

            dispatcher.dispatch("first")
            lastDispatchedAction shouldBe "first"

            // Simulate time passing
            advanceTimeBy(100.milliseconds)

            dispatcher.dispatch("second")
            lastDispatchedAction shouldBe "second"
        }

    @Test
    fun shouldUseDefaultDebounceTime() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test")
            dispatcher.dispatch("test")

            dispatchCount shouldBe 1
        }

    @Test
    fun shouldCreateWithFactoryFunction() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                Dispatcher<String>(
                    debounce = 100,
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test")
            dispatcher.dispatch("test")

            dispatchCount shouldBe 1
        }

    @Test
    fun shouldDebounceWithinWindow() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 5000,
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("test")
            dispatchCount shouldBe 1

            dispatcher.dispatch("test")
            dispatchCount shouldBe 1

            dispatcher.dispatch("different")
            dispatchCount shouldBe 2

            dispatcher.dispatch("different")
            dispatchCount shouldBe 2
        }

    @Test
    fun shouldDispatchManyDifferentActionsImmediately() =
        runTest {
            val dispatchedActions = mutableListOf<Int>()
            val dispatcher =
                DebounceDispatcher<Int>(
                    debounce = 5000,
                    block = { dispatchedActions.add(it) },
                )

            repeat(10) { i ->
                dispatcher.dispatch(i)
            }

            dispatchedActions shouldBe (0 until 10).toList()
        }

    @Test
    fun shouldCreateWithFactoryFunctionAndExclude() =
        runTest {
            var dispatchCount = 0
            val dispatcher =
                Dispatcher<String>(
                    debounce = 100,
                    exclude = { it == "skip-debounce" },
                    block = { dispatchCount++ },
                )

            dispatcher.dispatch("skip-debounce")
            dispatcher.dispatch("skip-debounce")
            dispatchCount shouldBe 2

            dispatcher.dispatch("normal")
            dispatcher.dispatch("normal")
            dispatchCount shouldBe 3
        }

    @Test
    fun shouldInvokeOperatorOnDebounceDispatcher() =
        runTest {
            var dispatchedAction: String? = null
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    block = { dispatchedAction = it },
                )

            dispatcher("test")
            dispatchedAction shouldBe "test"
        }

    @Test
    fun shouldRelayOnDebounceDispatcher() =
        runTest {
            var dispatchedAction: String? = null
            val dispatcher =
                DebounceDispatcher<String>(
                    debounce = 100,
                    block = { dispatchedAction = it },
                )

            val relay = dispatcher.relay("relayed")
            relay()
            dispatchedAction shouldBe "relayed"
        }
}
