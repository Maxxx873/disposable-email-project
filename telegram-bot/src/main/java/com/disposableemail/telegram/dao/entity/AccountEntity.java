package com.disposableemail.telegram.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_accounts",
        uniqueConstraints = {@UniqueConstraint(columnNames = "address", name = "ux_address")})
public class AccountEntity extends Auditable {

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "domain")
    private String domain;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey=@ForeignKey(name = "fk_customer_id"))
    @ToString.Exclude
    @JsonIgnoreProperties(value = { "accounts" }, allowSetters = true)
    private CustomerEntity customer;

}
