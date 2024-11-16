package com.disposableemail.event.api

import com.disposableemail.core.model.Address
import com.disposableemail.core.model.Credentials
import java.time.Instant

interface AccountEvents : Event {

    data class AccountCreation (
        override val eventId: Event.EventId,
        override val instant: Instant,
        val credentials: Credentials,
    ) : AccountEvents

    data class AccountDeletion (
        override val eventId: Event.EventId,
        override val instant: Instant,
        val accountAddress: Address,
    ) : AccountEvents
}