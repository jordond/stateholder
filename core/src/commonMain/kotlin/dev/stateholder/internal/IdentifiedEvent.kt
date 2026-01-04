package dev.stateholder.internal

import kotlinx.atomicfu.atomic

/**
 * Wraps an event with a unique identifier for correct handling of duplicate events.
 *
 * This solves the problem where two identical events (by equals()) could be
 * incorrectly removed when only one should be handled.
 *
 * @param E The type of the event.
 * @param id The unique identifier for this event instance.
 * @param event The wrapped event.
 */
internal data class IdentifiedEvent<E>(
    val id: Long,
    val event: E,
) {
    companion object {
        private val counter = atomic(0L)

        /**
         * Creates a new [IdentifiedEvent] with a unique ID.
         */
        fun <E> create(event: E): IdentifiedEvent<E> =
            IdentifiedEvent(counter.incrementAndGet(), event)
    }
}
