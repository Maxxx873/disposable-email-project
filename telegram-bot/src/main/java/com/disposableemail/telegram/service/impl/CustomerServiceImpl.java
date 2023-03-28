package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.dao.entity.CustomerEntity;
import com.disposableemail.telegram.dao.repository.CustomerRepository;
import com.disposableemail.telegram.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Cacheable(cacheNames = "customers", key = "#chatId")
    public Optional<CustomerEntity> getByChatId(long chatId) {
        log.info("Fetching customer by id | {}", chatId);
        return customerRepository.getByChatId(chatId);
    }

    @Override
    @CachePut(cacheNames = "customers", key = "#customerEntity.chatId")
    public CustomerEntity save(CustomerEntity customerEntity) {
        log.info("Saving customer | {}", customerEntity.getName());
        return customerRepository.save(customerEntity);
    }
}
