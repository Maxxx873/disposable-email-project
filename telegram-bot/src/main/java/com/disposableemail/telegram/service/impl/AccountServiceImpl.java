package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.repository.AccountRepository;
import com.disposableemail.telegram.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Cacheable(cacheNames = "accounts", key = "#address")
    public Optional<AccountEntity> findByAddress(String address) {
        log.info("Getting an Account by address | {}", address);
        return accountRepository.findByAddress(address);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "accounts", key="#account.address"),
            @CacheEvict(cacheNames = "customers", key="#account.customer.chatId")
    })
    public void delete(AccountEntity account) {
        log.info("Deleting an Account | {}", account.getAddress());
        accountRepository.delete(account);
    }

    @Override
    @CachePut(cacheNames = "accounts", key = "#accountEntity.address")
    public AccountEntity createAccount(AccountEntity accountEntity) {
        log.info("Creating an Account | {}", accountEntity.getAddress());
        return accountRepository.save(accountEntity);
    }
}
