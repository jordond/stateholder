package dev.stateholder

import app.cash.turbine.test
import dev.stateholder.provider.flowStateProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StateComposerTest {
    data class TestState(
        val name: String = "",
        val count: Int = 0,
        val active: Boolean = false,
    )

    @Test
    fun shouldCreateContainerWithCorrectInitialState() = runTest {
        val container = stateContainer(backgroundScope, TestState(name = "initial")) {}

        container.state.test {
            awaitItem() shouldBe TestState(name = "initial")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeFlowUsingSlice() = runTest {
        val nameFlow = MutableStateFlow("Alice")

        val container = stateContainer(backgroundScope, TestState()) {
            slice(nameFlow) { copy(name = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState() // Initial
            awaitItem() shouldBe TestState(name = "Alice") // After flow emits

            nameFlow.value = "Bob"
            awaitItem() shouldBe TestState(name = "Bob")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeFlowUsingIntoInfix() = runTest {
        val countFlow = MutableStateFlow(42)

        val container = stateContainer(backgroundScope, TestState()) {
            countFlow into { copy(count = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(count = 42)

            countFlow.value = 100
            awaitItem() shouldBe TestState(count = 100)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeMultipleSlices() = runTest {
        val nameFlow = MutableStateFlow("Alice")
        val countFlow = MutableStateFlow(10)
        val activeFlow = MutableStateFlow(true)

        val container = stateContainer(backgroundScope, TestState()) {
            nameFlow into { copy(name = it) }
            countFlow into { copy(count = it) }
            activeFlow into { copy(active = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState() // Initial

            // The order of emissions may vary, so collect all updates
            val updates = mutableListOf<TestState>()
            repeat(3) { updates.add(awaitItem()) }

            // Final state should have all values composed
            updates.last() shouldBe TestState(name = "Alice", count = 10, active = true)

            // Update one flow
            nameFlow.value = "Bob"
            awaitItem() shouldBe TestState(name = "Bob", count = 10, active = true)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldCancelIndividualSliceJobs() = runTest {
        val nameFlow = MutableStateFlow("Alice")
        val countFlow = MutableStateFlow(10)

        lateinit var nameJob: kotlinx.coroutines.Job

        val container = stateContainer(backgroundScope, TestState()) {
            nameJob = nameFlow into { copy(name = it) }
            countFlow into { copy(count = it) }
        }

        container.state.test {
            skipItems(3) // Initial + 2 flow emissions

            // Cancel the name job
            nameJob.cancel()

            // Name updates should no longer affect state
            nameFlow.value = "Bob"

            // But count updates should still work
            countFlow.value = 20
            awaitItem() shouldBe TestState(name = "Alice", count = 20, active = false)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeFlowStateProvider() = runTest {
        val flow = MutableStateFlow("test")
        val provider = flowStateProvider(initialState = "initial", flow = flow)

        val container = stateContainer(backgroundScope, TestState()) {
            slice(provider) { copy(name = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(name = "test")

            flow.value = "updated"
            awaitItem() shouldBe TestState(name = "updated")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeFlowStateProviderUsingInto() = runTest {
        val flow = MutableStateFlow(99)
        val provider = flowStateProvider(initialState = 0, flow = flow)

        val container = stateContainer(backgroundScope, TestState()) {
            provider into { copy(count = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(count = 99)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeStateContainer() = runTest {
        val otherContainer = stateContainer(42)

        val container = stateContainer(backgroundScope, TestState()) {
            slice(otherContainer) { copy(count = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(count = 42)

            otherContainer.update { it + 10 }
            awaitItem() shouldBe TestState(count = 52)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeStateHolder() = runTest {
        val otherContainer = stateContainer(true)
        val holder: StateHolder<Boolean> = otherContainer.asStateHolder()

        val container = stateContainer(backgroundScope, TestState()) {
            holder into { copy(active = it) }
        }

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(active = true)

            otherContainer.update { !it }
            awaitItem() shouldBe TestState(active = false)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldWorkWithStateProvider() = runTest {
        val provider = provideState { TestState(name = "from provider") }

        val container = stateContainer(
            scope = backgroundScope,
            initialStateProvider = provider,
        ) {
            // No slices, just testing provider overload
        }

        container.state.test {
            awaitItem() shouldBe TestState(name = "from provider")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
