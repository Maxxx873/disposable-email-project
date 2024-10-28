package com.disposableemail.core.service.facade

import com.disposableemail.core.dao.entity.AccountEntity
import com.disposableemail.core.exception.custom.AccountAuthServerRegistrationException
import com.disposableemail.core.exception.custom.AccountMailServerRegistrationException
import com.disposableemail.core.model.Credentials
import com.disposableemail.core.service.api.AccountService
import com.disposableemail.core.service.api.auth.AuthorizationServiceReactive
import com.disposableemail.core.service.api.mail.MailServerClientService
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import reactor.core.publisher.Mono

@Slf4j
@Service
class AccountFacadeImpl(
    private val accountService: AccountService,
    private val mailService: MailServerClientService,
    private val authService: AuthorizationServiceReactive,
) : AccountFacade {
    override fun createAccount(credentials: Credentials): Mono<AccountEntity> =
        mono {
            val mailServerResponse =
                async { mailService.createUser(credentials).awaitBodilessEntity() }
            val authServerResponse =
                async { authService.createUser(credentials).awaitSingle() }
            ResponseAggregate(
                authServerResponse = authServerResponse.await(),
                mailServerResponse = mailServerResponse.await()
            ).let {
                if (isAuthServerResponseCorrect(it)) {
                    throw AccountAuthServerRegistrationException(credentials)
                }
                if (isMailServerResponseCorrect(it)) {
                    throw AccountMailServerRegistrationException(credentials)
                }
                val accountEntity = async { accountService.createAccount(credentials).awaitSingleOrNull() }
                accountEntity.await()
            }
        }

    private fun isAuthServerResponseCorrect(response: ResponseAggregate) =
        response.authServerResponse.status != HttpStatus.CREATED.value()

    private fun isMailServerResponseCorrect(response: ResponseAggregate) =
        response.mailServerResponse.statusCode.value() != HttpStatus.NO_CONTENT.value()
}

data class ResponseAggregate(val authServerResponse: Response, val mailServerResponse: ResponseEntity<Void>)


