package com.disposableemail.core.event;

import com.disposableemail.core.dao.repository.DomainRepository;
import com.disposableemail.core.service.api.mail.MailServerClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventListener {

    @Value("${mail-server.name}")
    private String mailServerName;

    private final DomainRepository domainRepository;
    private final MailServerClientService mailServerClientService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("Application is ready");

        mailServerClientService.getDomains()
                .flatMap(domain -> domainRepository.findByDomain(domain.getDomain())
                        .switchIfEmpty(Mono.defer(() -> domainRepository.save(domain))))
                .doOnComplete(() -> log.info("All domains updated from | {}", mailServerName))
                .subscribe();
    }
}
