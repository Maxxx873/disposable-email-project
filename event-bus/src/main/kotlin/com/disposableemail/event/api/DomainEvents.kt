package com.disposableemail.event.api

import com.disposableemail.core.model.Domain
import java.time.Instant

interface DomainEvents : Event {
    val domain: Domain

    data class DomainCreation(
        override val eventId: Event.EventId = Event.EventId.Companion.random(),
        override val instant: Instant,
        override val domain: Domain,
    ) : DomainEvents

    data class DomainDeletion(
        override val eventId: Event.EventId = Event.EventId.Companion.random(),
        override val instant: Instant,
        override val domain: Domain,
    ) : DomainEvents
}