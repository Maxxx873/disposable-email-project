package com.disposableemail.core.dao.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A Domain entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "domain")
public class DomainEntity extends Auditable {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotEmpty(message = "Domain cannot be empty")
    @Size(max = 255, message = "Domain cannot be more than 255 characters")
    @Pattern(regexp = "^[^@/]*$", message = "Domain cannot contain '@' or '/' characters")
    private String domain;

    private Boolean isActive;
    private Boolean isPrivate;

}
