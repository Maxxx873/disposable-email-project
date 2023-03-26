package com.disposableemail.telegram.service;

import com.disposableemail.telegram.dao.entity.CustomerEntity;

import java.util.Optional;

public interface CustomerService {

    Optional<CustomerEntity> getByChatId(long chatId);

    Optional<CustomerEntity> save(CustomerEntity customerEntity);

}
