package com.disposableemail.event.api.impl.kafka

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.service.api.AccountService
import com.disposableemail.core.service.api.mail.MailServerClientService
import com.disposableemail.event.api.MailboxEvents
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

@Component
@ConditionalOnProperty(value = ["spring.kafka.topic"])
class KafkaMailboxEventsListener(
    val consumerTemplate: ReactiveKafkaConsumerTemplate<String, String>,
    val accountService: AccountService,
    val mailService: MailServerClientService
) : CommandLineRunner {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun run(vararg args: String?) {
        consumerTemplate
            .receiveAutoAck()
            .doOnNext { r -> log.info("Received email id=${r.key()} to ${r.value()}") }
            .doOnError { log.error("Error during receiving process: ${it.localizedMessage}") }
            .concatMap { receiverRecord ->
                updateUsedSize(mapToMailReceivedEvent(receiverRecord).address)
            }.subscribe()
    }

    fun mapToMailReceivedEvent(
        record: ConsumerRecord<String, String>
    ): MailboxEvents.MailReceived =
        MailboxEvents.MailReceived(
            instant = Instant.ofEpochMilli(record.timestamp()),
            address = record.value()
        )

    fun updateUsedSize(
        address: String,
    ): Mono<AccountEntity> =
        mono {
            val accountEntity = async { accountService.getAccountByAddress(address).awaitSingle() }.await()
            val usedSize = async { mailService.getUpdatableUsedSize(address, accountEntity.used).awaitSingle() }
            val updatedAccount = accountEntity.apply { used = usedSize.await() }
            async { accountService.updateAccount(updatedAccount).awaitSingle() }.await()
        }

}