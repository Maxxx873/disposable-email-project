package com.disposableemail.telegram.dao.repository;

import com.disposableemail.telegram.dao.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByAddress(String address);
    void deleteByAddress(String address);
}
