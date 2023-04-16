package com.disposableemail.telegram.dao.mapper;

import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class AccountEntityMapper {

    @Autowired
    protected TextEncryptor encryptor;

    @Mapping(target = "password", expression = "java(encryptor.decrypt(accountEntity.getPassword()))")
    public abstract Credentials accountEntityToCredentials(AccountEntity accountEntity);
}

