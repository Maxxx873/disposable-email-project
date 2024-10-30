package com.disposableemail.facade.api

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.model.Credentials
import reactor.core.publisher.Mono

interface AccountFacade {
    fun createAccount(credentials: Credentials): Mono<AccountEntity>
}