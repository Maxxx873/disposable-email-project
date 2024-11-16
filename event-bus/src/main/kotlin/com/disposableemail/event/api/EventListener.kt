package com.disposableemail.event.api

import org.reactivestreams.Publisher

fun interface EventListener {
    fun event(event: Event): Publisher<Void>
}