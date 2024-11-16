package com.disposableemail.event.api

import reactor.core.publisher.Mono

interface EventBus {
    fun publish(event: Event): Mono<Void>
    fun eventBusName(): Mono<String>
}