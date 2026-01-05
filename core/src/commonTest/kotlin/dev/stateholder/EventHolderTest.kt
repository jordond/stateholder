package dev.stateholder

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EventHolderTest {
    sealed class TestEvent {
        data class ShowMessage(val message: String) : TestEvent()
        data object Navigate : TestEvent()
        data class Data(val value: Int) : TestEvent()
    }

    @Test
    fun shouldStartWithEmptyEventsList() = runTest {
        val holder = eventHolder<TestEvent>()

        holder.events.test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitSingleEvent() = runTest {
        val holder = eventHolder<TestEvent>()
        val event = TestEvent.ShowMessage("Hello")

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event)
            awaitItem() shouldBe persistentListOf(event)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitMultipleEventsWithVararg() = runTest {
        val holder = eventHolder<TestEvent>()
        val event1 = TestEvent.ShowMessage("Hello")
        val event2 = TestEvent.Navigate
        val event3 = TestEvent.Data(42)

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1, event2, event3)
            awaitItem() shouldBe persistentListOf(event1, event2, event3)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitMultipleEventsWithIterable() = runTest {
        val holder = eventHolder<TestEvent>()
        val events = listOf(
            TestEvent.ShowMessage("Hello"),
            TestEvent.Navigate,
            TestEvent.Data(42),
        )

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(events)
            awaitItem() shouldBe persistentListOf(
                TestEvent.ShowMessage("Hello"),
                TestEvent.Navigate,
                TestEvent.Data(42),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleSingleEvent() = runTest {
        val holder = eventHolder<TestEvent>()
        val event1 = TestEvent.ShowMessage("Hello")
        val event2 = TestEvent.Navigate

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1, event2)
            awaitItem() shouldBe persistentListOf(event1, event2)

            holder.handle(event1)
            awaitItem() shouldBe persistentListOf(event2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleAllEvents() = runTest {
        val holder = eventHolder<TestEvent>()
        val event1 = TestEvent.ShowMessage("Hello")
        val event2 = TestEvent.Navigate
        val event3 = TestEvent.Data(42)

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1, event2, event3)
            awaitItem() shouldBe persistentListOf(event1, event2, event3)

            holder.handleAll()
            awaitItem() shouldBe persistentListOf()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleDuplicateEventsByReferenceFirst() = runTest {
        val holder = eventHolder<TestEvent>()
        // Create two events that are equal by value but different by reference
        val event1 = TestEvent.ShowMessage("Same message")
        val event2 = TestEvent.ShowMessage("Same message")

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1)
            awaitItem() shouldBe persistentListOf(event1)

            holder.emit(event2)
            awaitItem() shouldBe persistentListOf(event1, event2)

            // Handle event2 specifically - should use reference equality first
            holder.handle(event2)
            val afterHandle = awaitItem()
            afterHandle.size shouldBe 1
            // event1 should still be there
            afterHandle.first() shouldBe TestEvent.ShowMessage("Same message")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleEventByValueEqualityWhenReferenceNotFound() = runTest {
        val holder = eventHolder<TestEvent>()
        val event = TestEvent.Data(100)

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event)
            awaitItem() shouldBe persistentListOf(event)

            // Create a new event with same value but different reference
            val eventCopy = TestEvent.Data(100)
            holder.handle(eventCopy)
            awaitItem() shouldBe persistentListOf()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldMaintainOrderWhenHandlingEvents() = runTest {
        val holder = eventHolder<TestEvent>()
        val event1 = TestEvent.Data(1)
        val event2 = TestEvent.Data(2)
        val event3 = TestEvent.Data(3)

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1, event2, event3)
            awaitItem() shouldBe persistentListOf(event1, event2, event3)

            // Handle the middle event
            holder.handle(event2)
            awaitItem() shouldBe persistentListOf(event1, event3)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleFirstMatchingEventOnly() = runTest {
        val holder = eventHolder<TestEvent>()
        // Emit two identical events
        val event = TestEvent.ShowMessage("Duplicate")

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event)
            holder.emit(event)
            skipItems(1) // Skip intermediate state
            val afterTwoEmits = awaitItem()
            afterTwoEmits.size shouldBe 2

            // Handle should remove only the first matching event
            holder.handle(event)
            val afterHandle = awaitItem()
            afterHandle.size shouldBe 1

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldDoNothingWhenHandlingNonExistentEvent() = runTest {
        val holder = eventHolder<TestEvent>()
        val event1 = TestEvent.ShowMessage("Hello")
        val nonExistentEvent = TestEvent.Navigate

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            holder.emit(event1)
            awaitItem() shouldBe persistentListOf(event1)

            // Try to handle an event that wasn't emitted
            holder.handle(nonExistentEvent)
            // Should still have the original event (no state change expected)
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSupportDelegationPattern() = runTest {
        // This simulates the delegation pattern: class MyClass : EventHolder<E> by eventHolder()
        class TestViewModel : EventHolder<TestEvent> by eventHolder() {
            fun doSomething() {
                emit(TestEvent.Navigate)
            }
        }

        val viewModel = TestViewModel()

        viewModel.events.test {
            awaitItem() shouldBe persistentListOf()

            viewModel.doSomething()
            awaitItem() shouldBe persistentListOf(TestEvent.Navigate)

            viewModel.handle(TestEvent.Navigate)
            awaitItem() shouldBe persistentListOf()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitAndHandleSequentiallyCorrectly() = runTest {
        val holder = eventHolder<TestEvent>()

        holder.events.test {
            awaitItem() shouldBe persistentListOf()

            // Emit, handle, emit pattern
            holder.emit(TestEvent.Data(1))
            awaitItem() shouldBe persistentListOf(TestEvent.Data(1))

            holder.handle(TestEvent.Data(1))
            awaitItem() shouldBe persistentListOf()

            holder.emit(TestEvent.Data(2))
            awaitItem() shouldBe persistentListOf(TestEvent.Data(2))

            holder.emit(TestEvent.Data(3))
            awaitItem() shouldBe persistentListOf(TestEvent.Data(2), TestEvent.Data(3))

            holder.handleAll()
            awaitItem() shouldBe persistentListOf()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
