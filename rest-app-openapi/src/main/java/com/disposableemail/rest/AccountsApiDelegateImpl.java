package com.disposableemail.rest;


import com.disposableemail.rest.api.AccountsApiDelegate;
import com.disposableemail.rest.model.Account;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.ErrorResponse;
import com.disposableemail.service.api.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsApiDelegateImpl implements AccountsApiDelegate {

    private final AuthorizationService authorizationService;

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
        log.info("createAccountItem({})", credentials.getAddress());
        var account = new Account();
        account.setAddress(authorizationService.createUser(credentials));
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }
}
