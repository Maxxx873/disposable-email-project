package com.disposableemail.telegram.dao.entity;

import com.disposableemail.telegram.bot.handler.BotState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers", uniqueConstraints = {@UniqueConstraint(columnNames = "chat_id", name = "customers_unique_chatid_idx")})
public class CustomerEntity extends Auditable {

    @NotNull
    @Column(name = "chat_id", unique = true, nullable = false)
    private long chatId;

    @NotBlank
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_state", nullable = false)
    private BotState botState;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private List<AccountEntity> accounts;

    public static CustomerEntity getNewCustomer(Long chatId, String name) {
        return CustomerEntity.builder()
                .chatId(chatId)
                .name(name)
                .accounts(new ArrayList<>())
                .botState(BotState.START)
                .build();
    }

    public void addAccount(AccountEntity accountEntity) {
        this.accounts.add(accountEntity);
        accountEntity.setCustomer(this);
    }

    public void removeAccountByAddress(String address) {
        accounts.removeIf(accountEntity -> Objects.equals(accountEntity.getAddress(), address));
    }
}
