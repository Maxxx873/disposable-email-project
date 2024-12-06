package com.disposableemail.event.api

import java.time.Instant

interface MailboxEvents : Event {
    val address: String

    data class MailReceived(
        override val eventId: Event.EventId = Event.EventId.random(),
        override val instant: Instant,
        override val address: String,
    ) : MailboxEvents

    data class UsedSizeUpdated(
        override val eventId: Event.EventId = Event.EventId.random(),
        override val instant: Instant,
        override val address: String,
    ) : MailboxEvents
}