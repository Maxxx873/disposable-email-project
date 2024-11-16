package com.disposableemail.event.api

import java.time.Instant
import java.util.UUID

interface Event {
    val eventId: EventId
    val instant: Instant

    data class EventId(val id: UUID) {
        companion object {
            fun of(uuid: UUID): EventId = EventId(uuid)
            fun random(): EventId = EventId(UUID.randomUUID())
            fun of(serialized: String): EventId = of(UUID.fromString(serialized))
        }
    }
}