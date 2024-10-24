package com.disposableemail.core.event

import com.disposableemail.core.event.producer.EventProducer
import com.disposableemail.core.event.producer.impl.EventProducerRabbitMqImpl
import com.disposableemail.core.model.Credentials
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.test.util.ReflectionTestUtils

class EventProducerRabbitMqImplTest {

    private var rabbitTemplateMock: RabbitTemplate? = null
    private var eventProducer: EventProducer? = null

    @BeforeEach
    fun setUp() {
        this.rabbitTemplateMock = Mockito.mock<RabbitTemplate>(RabbitTemplate::class.java)
        this.eventProducer =
            EventProducerRabbitMqImpl(this.rabbitTemplateMock)
        ReflectionTestUtils.setField(eventProducer!!, "messagesExchange", "DisposableEmail-exchange-messages")
        ReflectionTestUtils.setField(eventProducer!!, "accountsExchange", "DisposableEmail-exchange-accounts")
        ReflectionTestUtils.setField(eventProducer!!, "domainsExchange", "DisposableEmail-exchange-domains")
        ReflectionTestUtils.setField(eventProducer!!, "messagesGettingRoutingKey", "getting-messages-key")
        ReflectionTestUtils.setField(eventProducer!!, "accountStartCreatingRoutingKey", "account-start-creating-key")
        ReflectionTestUtils.setField(eventProducer!!, "accountAuthDeletingRoutingKey", "account-auth-deleting-key")
        ReflectionTestUtils.setField(eventProducer!!, "accountMailDeletingRoutingKey", "account-mail-deleting-key")
        ReflectionTestUtils.setField(eventProducer!!, "accountDeletingRoutingKey", "account-deleting-key")
        ReflectionTestUtils.setField(eventProducer!!, "accountAuthConfirmRoutingKey", "account-auth-confirmation-key")
        ReflectionTestUtils.setField(
            eventProducer!!,
            "accountInMailServiceCreatingRoutingKey",
            "account-in-mail-service-creating-key"
        )
        ReflectionTestUtils.setField(
            eventProducer!!,
            "accountMailboxInMailServiceCreatingRoutingKey",
            "account-mailbox-creating-key"
        )
        ReflectionTestUtils.setField(
            eventProducer!!,
            "accountQuotaInMailServiceUpdatingRoutingKey",
            "account-quota-in-mail-service-updating-key"
        )
        ReflectionTestUtils.setField(eventProducer!!, "domainsCreatingRoutingKey", "domain-creating-key")
        ReflectionTestUtils.setField(eventProducer!!, "domainsDeletingRoutingKey", "domain-deleting-key")
    }

    @ParameterizedTest
    @MethodSource("events")
    fun testSendMessage(eventType: Event.Type, exchange: String, routingKey: String) {
        val testCredentials = Credentials("username", "password")
        val testEvent = Event(eventType, testCredentials)
        assertThatCode { this.eventProducer!!.send(testEvent) }.doesNotThrowAnyException()
        Mockito.verify<RabbitTemplate?>(this.rabbitTemplateMock)
            .convertAndSend(eq(exchange), eq(routingKey), eq(testCredentials))
    }

    companion object {
        @JvmStatic
        fun events() = listOf(
            Arguments.of(Event.Type.GETTING_MESSAGES, "DisposableEmail-exchange-messages", "getting-messages-key"),
            Arguments.of(
                Event.Type.START_CREATING_ACCOUNT,
                "DisposableEmail-exchange-accounts",
                "account-start-creating-key"
            ),
            Arguments.of(
                Event.Type.AUTH_REGISTER_CONFIRMATION,
                "DisposableEmail-exchange-accounts",
                "account-auth-confirmation-key"
            ),
            Arguments.of(
                Event.Type.ACCOUNT_CREATED_IN_MAIL_SERVICE,
                "DisposableEmail-exchange-accounts",
                "account-in-mail-service-creating-key"
            ),
            Arguments.of(
                Event.Type.MAILBOX_CREATED_IN_MAIL_SERVICE,
                "DisposableEmail-exchange-accounts",
                "account-mailbox-creating-key"
            ),
            Arguments.of(
                Event.Type.QUOTA_SIZE_UPDATED_IN_MAIL_SERVICE,
                "DisposableEmail-exchange-accounts",
                "account-quota-in-mail-service-updating-key"
            ),
            Arguments.of(
                Event.Type.AUTH_DELETING_ACCOUNT,
                "DisposableEmail-exchange-accounts",
                "account-auth-deleting-key"
            ),
            Arguments.of(
                Event.Type.MAIL_DELETING_ACCOUNT,
                "DisposableEmail-exchange-accounts",
                "account-mail-deleting-key"
            ),
            Arguments.of(
                Event.Type.DELETING_ACCOUNT,
                "DisposableEmail-exchange-accounts",
                "account-deleting-key"
            ),
            Arguments.of(
                Event.Type.DOMAIN_CREATED,
                "DisposableEmail-exchange-domains",
                "domain-creating-key"
            ),
            Arguments.of(
                Event.Type.DOMAIN_DELETED,
                "DisposableEmail-exchange-domains",
                "domain-deleting-key"
            ),
        )
    }
}