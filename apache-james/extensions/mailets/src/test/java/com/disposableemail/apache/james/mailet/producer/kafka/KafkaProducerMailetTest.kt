package com.disposableemail.apache.james.mailet.producer.kafka

import org.apache.james.core.MailAddress
import org.apache.mailet.base.test.FakeMail
import org.apache.mailet.base.test.FakeMailetConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

@Disabled
class KafkaProducerMailetTest {

    lateinit var mailetConfig: FakeMailetConfig
    lateinit var mailet: KafkaProducerMailet

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        mailetConfig = FakeMailetConfig.builder()
            .mailetName("KafkaProducerMailet")
            .setProperty("bootstrapServer", "localhost:9092")
            .setProperty("topicName", "disposable-email-received")
            .setProperty("partitions", "2")
            .setProperty("replication", "1")
            .build()

        mailet = KafkaProducerMailet()
        mailet.init(mailetConfig)

    }

    @Test
    @Throws(MessagingException::class)
    fun mailetShouldNotCreateDocumentWhenMailIsEmpty() {
        val mimeMessage = MimeMessage(null, FileInputStream("src/test/resources/test_mail_html_no_attachments.eml"))
        val mail = FakeMail.defaultFakeMail()
        mail.recipients.add(MailAddress("test@gmail.com"))
        mail.recipients.add(MailAddress("test1@gmail.com"))
        mail.message = mimeMessage
        val mailet = KafkaProducerMailet()

        mailet.init()
        mailet.service(mail)
    }
}