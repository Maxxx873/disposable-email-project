package com.disposableemail.event.api

import com.disposableemail.core.model.Address
import java.time.Instant

interface MailboxEvents : Event {
    val address: Address

    data class MailReceived(
        override val eventId: Event.EventId = Event.EventId.Companion.random(),
        override val instant: Instant,
        override val address: Address,
    ) : MailboxEvents

    data class UsedSizeUpdated(
        override val eventId: Event.EventId = Event.EventId.Companion.random(),
        override val instant: Instant,
        override val address: Address,
    ) : MailboxEvents
}