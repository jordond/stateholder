package dev.stateholder

import app.cash.turbine.test
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.FlowStateProvider
import dev.stateholder.provider.composedStateProvider
import dev.stateholder.provider.flowStateProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ComposedStateProviderTest {
    data class TestState(
        val name: String = "",
        val count: Int = 0,
        val items: List<String> = emptyList(),
    )

    @Test
    fun shouldProvideInitialState() = runTest {
        val composer = composedStateProvider(
            initialState = TestState(name = "initial", count = 42),
        )

        composer.provide() shouldBe TestState(name = "initial", count = 42)
    }

    @Test
    fun shouldProvideInitialStateFromStateProvider() = runTest {
        val stateProvider = provideState { TestState(name = "from provider") }

        val composer = composedStateProvider(
            provider = stateProvider,
        )

        composer.provide() shouldBe TestState(name = "from provider")
    }

    @Test
    fun shouldComposeWithFlows() = runTest {
        val nameFlow = MutableStateFlow("Alice")
        val countFlow = MutableStateFlow(100)

        val composer = composedStateProvider(
            initialState = TestState(),
            composer = {
                nameFlow into { copy(name = it) }
                countFlow into { copy(count = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState() // Initial

            // Collect updates from flows
            val updates = mutableListOf<TestState>()
            repeat(2) { updates.add(awaitItem()) }
            updates.last() shouldBe TestState(name = "Alice", count = 100)

            nameFlow.value = "Bob"
            awaitItem() shouldBe TestState(name = "Bob", count = 100)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeWithFlowStateProviders() = runTest {
        val nameFlow = MutableStateFlow("Charlie")
        val nameProvider = flowStateProvider("", nameFlow)

        val itemsFlow = MutableStateFlow(listOf("item1", "item2"))
        val itemsProvider = flowStateProvider(emptyList<String>(), itemsFlow)

        val composer = composedStateProvider(
            initialState = TestState(),
            composer = {
                nameProvider into { copy(name = it) }
                itemsProvider into { copy(items = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState() // Initial

            val updates = mutableListOf<TestState>()
            repeat(2) { updates.add(awaitItem()) }
            updates.last() shouldBe TestState(
                name = "Charlie",
                items = listOf("item1", "item2"),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSupportDelegationPattern() = runTest {
        val nameFlow = MutableStateFlow("Delegated")

        // Simulate: class MyComposer : ComposedStateProvider<TestState> by composedStateProvider(...)
        val composer: ComposedStateProvider<TestState> = composedStateProvider(
            initialState = TestState(count = 99),
            composer = {
                nameFlow into { copy(name = it) }
            },
        )

        composer.provide() shouldBe TestState(count = 99)

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState(count = 99)
            awaitItem() shouldBe TestState(name = "Delegated", count = 99)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSupportEmptyComposer() = runTest {
        val composer = composedStateProvider(
            initialState = TestState(name = "static"),
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState(name = "static")
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeWithStateContainers() = runTest {
        val otherContainer = stateContainer(42)

        val composer = composedStateProvider(
            initialState = TestState(),
            composer = {
                slice(otherContainer) { copy(count = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(count = 42)

            otherContainer.update { it + 10 }
            awaitItem() shouldBe TestState(count = 52)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeWithStateHolders() = runTest {
        val sourceContainer = stateContainer("from holder")
        val holder: StateHolder<String> = sourceContainer.asStateHolder()

        val composer = composedStateProvider(
            initialState = TestState(),
            composer = {
                holder into { copy(name = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState()
            awaitItem() shouldBe TestState(name = "from holder")

            sourceContainer.update { "updated" }
            awaitItem() shouldBe TestState(name = "updated")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldWorkWithComposedStateProviderInterface() = runTest {
        // Test implementing ComposedStateProvider interface directly
        val flow = MutableStateFlow("direct implementation")

        val composer = object : ComposedStateProvider<TestState> {
            override fun provide(): TestState = TestState(count = 1)

            override fun StateComposer<TestState>.compose() {
                flow into { copy(name = it) }
            }
        }

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState(count = 1)
            awaitItem() shouldBe TestState(name = "direct implementation", count = 1)

            flow.value = "changed"
            awaitItem() shouldBe TestState(name = "changed", count = 1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldCombineMultipleSourceTypes() = runTest {
        val flow = MutableStateFlow("flow value")
        val flowProvider: FlowStateProvider<Int> = flowStateProvider(0, MutableStateFlow(777))
        val container2 = stateContainer(listOf("a", "b"))

        val composer = composedStateProvider(
            initialState = TestState(),
            composer = {
                flow into { copy(name = it) }
                flowProvider into { copy(count = it) }
                container2 into { copy(items = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe TestState() // Initial

            // Collect all updates
            val updates = mutableListOf<TestState>()
            repeat(3) { updates.add(awaitItem()) }

            // Final state should have all values
            updates.last() shouldBe TestState(
                name = "flow value",
                count = 777,
                items = listOf("a", "b"),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
