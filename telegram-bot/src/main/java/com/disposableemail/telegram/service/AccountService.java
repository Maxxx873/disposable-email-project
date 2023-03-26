package com.disposableemail.telegram.service;

import com.disposableemail.telegram.dao.entity.AccountEntity;

import java.util.Optional;

public interface AccountService {

    Optional<AccountEntity> findByAddress(String address);
    void deleteByAddress(String address);

    AccountEntity createAccount(AccountEntity accountEntity);
}
