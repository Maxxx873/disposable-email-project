package com.disposableemail.core.dao.entity;

import com.disposableemail.core.model.Credentials;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An Account entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account")
public class AccountEntity extends Auditable {

    @Id
    private String id;

    @Email
    @Indexed(unique = true)
    private String address;

    private String mailboxId;
    private Boolean isDisabled;
    private Boolean isDeleted;
    private Integer quota;
    private Integer used;

    public static AccountEntity createAccountEntityFromCredentials(Credentials credentials, String quotaSize) {
        return AccountEntity.builder()
                .address(credentials.getAddress())
                .isDeleted(false)
                .isDisabled(false)
                .used(0)
                .quota(Integer.parseInt(quotaSize))
                .build();
    }

    public static AccountEntity createDefault() {
        return AccountEntity.builder()
                .address("")
                .isDeleted(false)
                .isDisabled(false)
                .used(0)
                .quota(0)
                .build();
    }

}
