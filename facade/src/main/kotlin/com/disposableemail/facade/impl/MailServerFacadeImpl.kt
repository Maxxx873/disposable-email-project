package com.disposableemail.facade.impl

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.model.Credentials
import com.disposableemail.core.service.api.mail.MailServerClientService
import com.disposableemail.facade.api.MailServerFacade
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MailServerFacadeImpl(
    private val mailService: MailServerClientService
) : MailServerFacade {

    @Value("\${mail-server.mailbox}")
    lateinit var inbox: String

    override fun createUser(credentials: Credentials): Mono<AccountEntity> =
        mono {
            async { mailService.createUser(credentials).awaitSingle() }.await().let {
                async { mailService.createMailbox(credentials).awaitSingle() }.await().let {
                    async { mailService.updateQuotaSize(credentials).awaitSingle() }.await().let {
                        val quotaSize = async { mailService.getQuotaSize(credentials.address).awaitSingle() }
                        val mailBoxId = async { mailService.getMailboxId(credentials, inbox).awaitSingle() }
                        AccountEntity.createDefault().apply {
                            address = credentials.address
                            mailboxId = mailBoxId.await()
                            quota = quotaSize.await()
                        }
                    }
                }
            }
        }
}
