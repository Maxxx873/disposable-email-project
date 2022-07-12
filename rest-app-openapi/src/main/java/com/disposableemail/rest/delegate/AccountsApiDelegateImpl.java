package com.disposableemail.rest.delegate;

import com.disposableemail.model.Account;
import com.disposableemail.model.Credentials;
import com.disposableemail.model.ErrorResponse;
import com.disposableemail.rest.api.AccountsApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
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
