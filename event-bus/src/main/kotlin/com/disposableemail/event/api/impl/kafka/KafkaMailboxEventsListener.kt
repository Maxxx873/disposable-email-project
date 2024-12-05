package com.disposableemail.event.api.impl.kafka

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class KafkaMailboxEventsListener(
    val consumerTemplate: ReactiveKafkaConsumerTemplate<String, String>
) : CommandLineRunner {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun run(vararg args: String?) {
        consumerTemplate
            .receive()
            .concatMap { receiverRecord ->
                Mono.just(receiverRecord)
                    .doOnNext { r -> log.info("Received email id=${r.key()} to ${r.value()}") }
                    .doOnError { log.error("Error during receiving process: ${it.localizedMessage}") }
                    .doFinally { receiverRecord.receiverOffset().acknowledge() }
            }.subscribe()
    }
}