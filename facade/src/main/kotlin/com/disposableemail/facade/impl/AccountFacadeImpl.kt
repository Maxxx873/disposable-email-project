package com.disposableemail.facade.impl

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.model.Credentials
import com.disposableemail.core.service.api.AccountService
import com.disposableemail.core.service.api.auth.AuthorizationServiceReactive
import com.disposableemail.facade.api.AccountFacade
import com.disposableemail.facade.api.MailServerFacade
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AccountFacadeImpl(
    private val accountService: AccountService,
    private val mailService: MailServerFacade,
    private val authService: AuthorizationServiceReactive,
) : AccountFacade {
    override fun createAccount(credentials: Credentials): Mono<AccountEntity> =
        mono {
            val mailUserAccountEntity = async { mailService.createUser(credentials).awaitSingle() }
            async { authService.createUser(credentials).awaitSingle() }.await()
            async { accountService.createAccount(mailUserAccountEntity.await()).awaitSingle() }.await()
        }
}