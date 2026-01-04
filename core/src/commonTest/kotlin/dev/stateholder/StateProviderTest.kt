package dev.stateholder

import app.cash.turbine.test
import dev.stateholder.provider.FlowStateProvider
import dev.stateholder.provider.flowStateProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StateProviderTest {
    @Test
    fun shouldCreateStateProviderWithInitialValue() = runTest {
        val provider = provideState(123)

        provider.provide() shouldBe 123
    }

    @Test
    fun shouldCreateStateProviderWithLazyInitialization() = runTest {
        var initializerCalled = false
        val provider = provideState {
            initializerCalled = true
            456
        }

        initializerCalled shouldBe false
        provider.provide() shouldBe 456
        initializerCalled shouldBe true
    }

    @Test
    fun shouldCreateAStateProviderFromAnyType() = runTest {
        val provider = "Foo".asStateProvider()
        provider.provide() shouldBe "Foo"
    }

    @Test
    fun shouldCreateStateProviderUsingStateProviderFunction() = runTest {
        val provider = stateProvider(789)
        provider.provide() shouldBe 789
    }

    @Test
    fun shouldCreateStateProviderUsingStateProviderFunctionWithLambda() = runTest {
        var called = false
        val provider = stateProvider {
            called = true
            "lazy value"
        }

        called shouldBe false
        provider.provide() shouldBe "lazy value"
        called shouldBe true
    }

    @Test
    fun shouldCreateFlowStateProviderWithDelegation() = runTest {
        val flow = MutableStateFlow("initial")
        val provider = flowStateProvider(
            initialState = "default",
            flow = flow,
        )

        provider.provide() shouldBe "default"

        provider.states().test {
            awaitItem() shouldBe "initial"

            flow.value = "updated"
            awaitItem() shouldBe "updated"

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSupportFlowStateProviderDelegationPattern() = runTest {
        val flow = MutableStateFlow(42)

        // Simulating the delegation pattern: class MyProvider : FlowStateProvider<Int> by flowStateProvider(...)
        val provider: FlowStateProvider<Int> = flowStateProvider(
            initialState = 0,
            flow = flow,
        )

        provider.provide() shouldBe 0

        provider.states().test {
            awaitItem() shouldBe 42
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldCreateFlowStateProviderWithStateProvider() = runTest {
        val stateProvider = provideState("from provider")
        val flow = MutableStateFlow("flow value")

        val provider = flowStateProvider(
            stateProvider,
            flow,
        )

        provider.provide() shouldBe "from provider"

        provider.states().test {
            awaitItem() shouldBe "flow value"
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldWorkWithFiniteFlows() = runTest {
        val finiteFlow = flowOf(1, 2, 3)

        val provider = flowStateProvider(
            initialState = 0,
            flow = finiteFlow,
        )

        provider.provide() shouldBe 0

        provider.states().test {
            awaitItem() shouldBe 1
            awaitItem() shouldBe 2
            awaitItem() shouldBe 3
            awaitComplete()
        }
    }

    @Test
    fun shouldWorkWithComplexStateTypes() = runTest {
        data class ComplexState(
            val id: Int,
            val name: String,
            val items: List<String>,
        )

        val flow = MutableStateFlow(ComplexState(1, "test", listOf("a", "b")))

        val provider = flowStateProvider(
            initialState = ComplexState(0, "", emptyList()),
            flow = flow,
        )

        provider.provide() shouldBe ComplexState(0, "", emptyList())

        provider.states().test {
            awaitItem() shouldBe ComplexState(1, "test", listOf("a", "b"))

            flow.value = ComplexState(2, "updated", listOf("c", "d", "e"))
            awaitItem() shouldBe ComplexState(2, "updated", listOf("c", "d", "e"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSupportMultipleCollectors() = runTest {
        val flow = MutableStateFlow("shared")

        val provider = flowStateProvider(
            initialState = "initial",
            flow = flow,
        )

        // First collector
        provider.states().test {
            awaitItem() shouldBe "shared"

            // Second collector
            provider.states().test {
                awaitItem() shouldBe "shared"

                flow.value = "updated"
                awaitItem() shouldBe "updated"

                cancelAndIgnoreRemainingEvents()
            }

            awaitItem() shouldBe "updated"
            cancelAndIgnoreRemainingEvents()
        }
    }
}