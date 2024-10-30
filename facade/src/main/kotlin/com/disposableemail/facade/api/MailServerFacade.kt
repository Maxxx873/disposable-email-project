package com.disposableemail.facade.api

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.model.Credentials
import reactor.core.publisher.Mono

interface MailServerFacade {
    fun createUser(credentials: Credentials): Mono<AccountEntity>
}