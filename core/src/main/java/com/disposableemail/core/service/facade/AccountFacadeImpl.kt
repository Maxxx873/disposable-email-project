package com.disposableemail.core.service.facade

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.model.Credentials
import com.disposableemail.core.service.api.AccountService
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AccountFacadeImpl(
    private val accountService: AccountService,
) : AccountFacade {
    override fun createAccount(credentials: Credentials): Mono<AccountEntity> =
        mono {
            val accountEntity = async { accountService.createAccount(credentials).awaitSingle() }
            accountEntity.await()
        }
}