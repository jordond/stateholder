package dev.stateholder

import app.cash.turbine.test
import dev.stateholder.provider.composedStateProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StateContainerTest {
    data class ComposeTestState(
        val name: String = "",
        val count: Int = 0,
    )

    @Test
    fun shouldCreateStateContainerWithInitialState() = runTest {
        val container = stateContainer(123)

        container.state.test {
            awaitItem() shouldBe 123
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldCreateStateContainerWithStateProvider() = runTest {
        val provider = provideState(456)
        val container = stateContainer(provider)

        container.state.test {
            awaitItem() shouldBe 456
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldImplementGetValue() = runTest {
        val container = stateContainer(789)
        container.getValue(
            stateHolder = container.asStateHolder(),
            property = StateContainer<Int>::state,
        ).value shouldBe 789
    }

    @Test
    fun shouldMergeFlowToState() = runTest {
        val container = stateContainer(100)
        val flow = MutableStateFlow(50)

        val collectJob = flow.mergeWithState(container, this) { state, value ->
            state + value
        }

        container.state.test {
            awaitItem() shouldBe 100  // Initial state
            awaitItem() shouldBe 150  // After collect (100 + 50)

            flow.value = 75
            awaitItem() shouldBe 225  // After update (150 + 75)

            collectJob.cancel()
            flow.value = 25
            expectNoEvents()  // No updates after cancellation

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeStateContainer() = runTest {
        val container = stateContainer(100)
        val otherContainer = stateContainer(50)

        val mergeJob = container.merge(otherContainer, backgroundScope) { state, value ->
            state + value
        }

        container.state.test {
            awaitItem() shouldBe 100
            awaitItem() shouldBe 150

            // When otherContainer updates to 75 (50 + 25), state becomes 150 + 75 = 225
            otherContainer.update { it + 25 }
            awaitItem() shouldBe 225

            mergeJob.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMergeStateHolder() = runTest {
        val container = stateContainer(100)
        val otherContainer = stateContainer(30)
        val holder: StateHolder<Int> = otherContainer.asStateHolder()

        val mergeJob = container.merge(holder, backgroundScope) { state, value ->
            state + value
        }

        container.state.test {
            awaitItem() shouldBe 100
            awaitItem() shouldBe 130

            // When holder emits 50 (30 + 20), state becomes 130 + 50 = 180
            otherContainer.update { it + 20 }
            awaitItem() shouldBe 180

            mergeJob.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeWithScopeAndBlock() = runTest {
        val nameFlow = MutableStateFlow("test")
        val countFlow = MutableStateFlow(42)

        val container = stateContainer(ComposeTestState())

        container.compose(backgroundScope) {
            nameFlow into { copy(name = it) }
            countFlow into { copy(count = it) }
        }

        container.state.test {
            awaitItem() shouldBe ComposeTestState()

            val updates = mutableListOf<ComposeTestState>()
            repeat(2) { updates.add(awaitItem()) }
            updates.last() shouldBe ComposeTestState(name = "test", count = 42)

            nameFlow.value = "updated"
            awaitItem() shouldBe ComposeTestState(name = "updated", count = 42)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeWithComposedStateProvider() = runTest {
        val nameFlow = MutableStateFlow("from provider")

        val composer = composedStateProvider(
            initialState = ComposeTestState(count = 99),
            composer = {
                nameFlow into { copy(name = it) }
            },
        )

        val container = stateContainer(backgroundScope, composer)

        container.state.test {
            awaitItem() shouldBe ComposeTestState(count = 99)
            awaitItem() shouldBe ComposeTestState(name = "from provider", count = 99)

            nameFlow.value = "changed"
            awaitItem() shouldBe ComposeTestState(name = "changed", count = 99)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldComposeMultipleTimes() = runTest {
        val flow1 = MutableStateFlow("first")
        val flow2 = MutableStateFlow(10)

        val container = stateContainer(ComposeTestState())

        // Compose once
        container.compose(backgroundScope) {
            flow1 into { copy(name = it) }
        }

        // Compose again with different flows
        container.compose(backgroundScope) {
            flow2 into { copy(count = it) }
        }

        container.state.test {
            awaitItem() shouldBe ComposeTestState()

            val updates = mutableListOf<ComposeTestState>()
            repeat(2) { updates.add(awaitItem()) }
            updates.last() shouldBe ComposeTestState(name = "first", count = 10)

            flow1.value = "second"
            awaitItem() shouldBe ComposeTestState(name = "second", count = 10)

            flow2.value = 20
            awaitItem() shouldBe ComposeTestState(name = "second", count = 20)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldUpdateStateDirectly() = runTest {
        val container = stateContainer(ComposeTestState(name = "initial"))

        container.state.test {
            awaitItem() shouldBe ComposeTestState(name = "initial")

            container.update { it.copy(count = 100) }
            awaitItem() shouldBe ComposeTestState(name = "initial", count = 100)

            container.update { it.copy(name = "updated", count = 200) }
            awaitItem() shouldBe ComposeTestState(name = "updated", count = 200)

            cancelAndIgnoreRemainingEvents()
        }
    }
}