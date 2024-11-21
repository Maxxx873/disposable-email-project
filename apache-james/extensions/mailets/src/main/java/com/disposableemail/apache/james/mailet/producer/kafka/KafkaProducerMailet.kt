package com.disposableemail.apache.james.mailet.producer.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.mailet.Mail
import org.apache.mailet.base.GenericMailet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.*


class KafkaProducerMailet : GenericMailet() {

    private val logger: Logger = LoggerFactory.getLogger(KafkaProducerMailet::class.java)
    private lateinit var bootstrapServer: String
    private lateinit var topic: String
    private lateinit var partitions: List<Int>
    private var replication: Short = 0

    override fun init() {
        logger.info("${javaClass.simpleName} Initializing..")

        bootstrapServer = getInitParameter("bootstrapServer")
        topic = getInitParameter("topicName")
        partitions = (0..<getInitParameter("partitions").toInt()).toList()
        replication = getInitParameter("replication").toShort()
        val config = Properties().apply {
            this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        }

        AdminClient.create(config).use { admin ->
            val topicNames = admin.listTopics().names().get()
            if (topic !in topicNames) {
                createTopic(admin)
            }
        }
    }

    private fun createTopic(admin: AdminClient) {
        val newTopic = NewTopic(topic, partitions.size, replication).configs(emptyMap())
        admin.createTopics(listOf(newTopic)).apply {
            logger.info("Topic $topic not exists, created new")
        }
    }

    override fun service(mail: Mail?) {
        mail?.let {
            logger.info("Mail received: ${mail.message.messageID}")

            val senderOptions = SenderOptions.create<String, String>(
                mapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
                )
            )

            KafkaSender.create(senderOptions).send(Flux.fromIterable(it.recipients)
                .map { address ->
                    SenderRecord.create(
                        topic,
                        partitions.first(),
                        it.message.sentDate.time,
                        it.message.messageID,
                        address.asString(),
                        it.message.messageID
                    )
                }
            )
                .doOnError { e: Any -> logger.info("Send message to Kafka failed: $e") }
                .doOnNext { _: Any -> logger.info("Message sent to Kafka: " + it.message.messageID) }
                .subscribe()
        }
    }

    override fun getMailetInfo(): String {
        return "Disposable email Kafka producer mailet"
    }
}