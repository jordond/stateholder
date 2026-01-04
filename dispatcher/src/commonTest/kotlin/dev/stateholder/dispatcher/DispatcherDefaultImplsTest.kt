package dev.stateholder.dispatcher

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Tests for the default implementations of [Dispatcher] interface methods.
 * These tests cover the cases where a custom implementation of [Dispatcher]
 * is used without overriding the default methods.
 */
class DispatcherDefaultImplsTest {
    /**
     * A minimal dispatcher that only implements dispatch() and relies on
     * default implementations for all other methods.
     */
    private class MinimalDispatcher<Action>(
        private val onDispatch: (Action) -> Unit,
    ) : Dispatcher<Action> {
        override fun dispatch(action: Action) {
            onDispatch(action)
        }
    }

    @Test
    fun shouldInvokeDefaultImplementation() =
        runTest {
            var dispatchedAction: String? = null
            val dispatcher: Dispatcher<String> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            dispatcher.invoke("test")
            dispatchedAction shouldBe "test"
        }

    @Test
    fun shouldRelayDefaultImplementation() =
        runTest {
            var dispatchedAction: String? = null
            val dispatcher: Dispatcher<String> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay = dispatcher.relay("relayed")
            relay()
            dispatchedAction shouldBe "relayed"
        }

    @Test
    fun shouldRelayOfSingleParamDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay = dispatcher.relayOf(TestAction::SingleParam)
            relay("param1")
            dispatchedAction shouldBe TestAction.SingleParam("param1")
        }

    @Test
    fun shouldRelayOfTwoParamsDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay = dispatcher.relayOf(TestAction::TwoParams)
            relay("text", 42)
            dispatchedAction shouldBe TestAction.TwoParams("text", 42)
        }

    @Test
    fun shouldRelayOfThreeParamsDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay =
                dispatcher.relayOf { a: String, b: Int, c: Boolean ->
                    TestAction.ThreeParams(a, b, c)
                }
            relay("text", 42, true)
            dispatchedAction shouldBe TestAction.ThreeParams("text", 42, true)
        }

    @Test
    fun shouldRelayOfFourParamsDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay =
                dispatcher.relayOf { a: String, b: Int, c: Boolean, d: Double ->
                    TestAction.FourParams(a, b, c, d)
                }
            relay("text", 42, true, 3.14)
            dispatchedAction shouldBe TestAction.FourParams("text", 42, true, 3.14)
        }

    @Test
    fun shouldRelayOfFiveParamsDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay =
                dispatcher.relayOf { a: String, b: Int, c: Boolean, d: Double, e: Char ->
                    TestAction.FiveParams(a, b, c, d, e)
                }
            relay("text", 42, true, 3.14, 'X')
            dispatchedAction shouldBe TestAction.FiveParams("text", 42, true, 3.14, 'X')
        }

    @Test
    fun shouldRelayOfSixParamsDefaultImplementation() =
        runTest {
            var dispatchedAction: TestAction? = null
            val dispatcher: Dispatcher<TestAction> =
                MinimalDispatcher { action ->
                    dispatchedAction = action
                }

            val relay =
                dispatcher.relayOf { a: String, b: Int, c: Boolean, d: Double, e: Char, f: String ->
                    TestAction.SixParams(a, b, c, d, e, f)
                }
            relay("text", 42, true, 3.14, 'X', "extra")
            dispatchedAction shouldBe TestAction.SixParams("text", 42, true, 3.14, 'X', "extra")
        }

    private sealed interface TestAction {
        data class SingleParam(
            val text: String,
        ) : TestAction

        data class TwoParams(
            val text: String,
            val number: Int,
        ) : TestAction

        data class ThreeParams(
            val text: String,
            val number: Int,
            val flag: Boolean,
        ) : TestAction

        data class FourParams(
            val text: String,
            val number: Int,
            val flag: Boolean,
            val decimal: Double,
        ) : TestAction

        data class FiveParams(
            val text: String,
            val number: Int,
            val flag: Boolean,
            val decimal: Double,
            val char: Char,
        ) : TestAction

        data class SixParams(
            val text: String,
            val number: Int,
            val flag: Boolean,
            val decimal: Double,
            val char: Char,
            val extra: String,
        ) : TestAction
    }
}
