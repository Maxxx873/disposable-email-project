package com.disposableemail.rest;


import com.disposableemail.rest.api.AccountsApiDelegate;
import com.disposableemail.rest.model.Account;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsApiDelegateImpl implements AccountsApiDelegate {

    @Override
    public ResponseEntity<ErrorResponse> deleteAccountItem(String id) {
        return AccountsApiDelegate.super.deleteAccountItem(id);
    }

    @Override
    public ResponseEntity<Account> getAccountItem(String id) {
        return AccountsApiDelegate.super.getAccountItem(id);
    }

    @Override
    public ResponseEntity<Account> createAccountItem(Credentials credentials) {
        return AccountsApiDelegate.super.createAccountItem(credentials);
    }
}
