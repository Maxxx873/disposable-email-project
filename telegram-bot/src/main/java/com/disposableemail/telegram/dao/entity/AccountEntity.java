package com.disposableemail.telegram.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_accounts", indexes = {
        @Index(name = "idx_account_address", columnList = "address")
})
public class AccountEntity extends Auditable {

    @Column(name = "address", unique = true)
    private String address;

    @Column(name = "domain")
    private String domain;

    @Column(name = "password")
    private String password;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private CustomerEntity customer;

}
