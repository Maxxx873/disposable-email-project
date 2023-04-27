package com.disposableemail.telegram.service;

import com.disposableemail.telegram.dao.entity.AccountEntity;

import java.util.Optional;

public interface AccountService {

    Optional<AccountEntity> findByAddress(String address);

    void delete(AccountEntity account);

    AccountEntity createAccount(AccountEntity accountEntity);
}
