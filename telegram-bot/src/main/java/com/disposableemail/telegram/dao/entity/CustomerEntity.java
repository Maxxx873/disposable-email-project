package com.disposableemail.telegram.dao.entity;

import com.disposableemail.telegram.bot.handler.BotState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers",
        uniqueConstraints = {@UniqueConstraint(columnNames = { "chat_id", "name" }, name = "ux_chat_id_name")})
public class CustomerEntity extends Auditable {

    @NotNull
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_state", nullable = false)
    private BotState botState;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    @JsonIgnoreProperties(value = { "customerEntity" }, allowSetters = true)
    private Set<AccountEntity> accounts;

    public static CustomerEntity getNewCustomer(Long chatId, String name) {
        return CustomerEntity.builder()
                .chatId(chatId)
                .name(name)
                .accounts(new HashSet<>())
                .botState(BotState.START)
                .build();
    }

    public void addAccount(AccountEntity accountEntity) {
        this.accounts.add(accountEntity);
        accountEntity.setCustomer(this);
    }

}
