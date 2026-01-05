package dev.stateholder.internal

import dev.stateholder.EventHolder
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Default implementation of [EventHolder] using [IdentifiedEvent] for correct duplicate handling.
 */
internal class DefaultEventHolder<Event> : EventHolder<Event> {
    private val _identifiedEvents =
        MutableStateFlow<PersistentList<IdentifiedEvent<Event>>>(persistentListOf())

    private val _events = MutableStateFlow<PersistentList<Event>>(persistentListOf())
    override val events: StateFlow<PersistentList<Event>> = _events.asStateFlow()

    override fun emit(event: Event) {
        _identifiedEvents.update { list ->
            list.add(IdentifiedEvent.create(event))
        }
        syncPublicEvents()
    }

    override fun emit(vararg events: Event) {
        emit(events.toList())
    }

    override fun emit(events: Iterable<Event>) {
        _identifiedEvents.update { list ->
            var newList = list
            events.forEach { event ->
                newList = newList.add(IdentifiedEvent.create(event))
            }
            newList
        }
        syncPublicEvents()
    }

    override fun handle(event: Event) {
        _identifiedEvents.update { list ->
            // Try reference equality first, then value equality
            val index = list.indexOfFirst { it.event === event }
                .takeIf { it >= 0 }
                ?: list.indexOfFirst { it.event == event }

            if (index >= 0) list.removeAt(index) else list
        }
        syncPublicEvents()
    }

    override fun handleAll() {
        _identifiedEvents.value = persistentListOf()
        syncPublicEvents()
    }

    private fun syncPublicEvents() {
        _events.value = _identifiedEvents.value.map { it.event }.toPersistentList()
    }
}