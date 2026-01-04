package dev.stateholder

import app.cash.turbine.test
import dev.stateholder.provider.FlowStateProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
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
}